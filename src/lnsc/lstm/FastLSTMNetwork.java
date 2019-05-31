package lnsc.lstm;

import lnsc.*;
import lnsc.pmvf.*;

/** <P> Long Short-Term Memory (LSMT) network. </P>
 *
 * <P> Implements Gers, Schraudolph & Schmiduber 2002 (from Journal of Machine
 * Learning Research 3:115-143) LSTM network. See also Gers, Schmidhuber, &
 * Cummins 2000 (from Neural Computation, 12(10) 2451-2471) and Hochreiter &
 * Schmidhuber 1997 (from Neural Computation 9(8):1735-1780).  </P>
 *
 * <P> This network memory reads the inputs, pass them through a layer of memory
 * blocks, and weights memory blocks output before applying a layer of output
 * units (usually sigmoidal). Eventually it may support hidden units (trainable
 * using truncated gradient). The memory blocks are fully recurrently connected.
 * The input are expendend with a bias unit to feed the memory block. The
 * parameters are all the weights feeding the memory block (foward and
 * recurrent) and the weights feeding the output layer. Output layer can be feed
 * by everything (bias, input, memory block outputs, memory block gates).
 * Gradient from network output to all the weights is provided as in the paper.
 * Only derivatives to these parameters is available.</P>
 *
 * <P> Parameters are all the weights of the memory blocks and all the weights
 * of the output layer. Derivative to parameters represent the derivative of
 * each output z_k(t) with respect to each weights based on the papers formulas.
 * <P>
 *
 * <P> Memory cells internal states, outputs, and memory block gates are
 * available through keywords in {@link LSTMDataNames}. </P>
 *
 * <P> Cloning is done through serialization, transient states are therefore
 * transient to cloning too (e.g. reseted in clones). <P>
 *
 * <P> Avoid using this class directly or deriving it unless you really know
 * what you do. Use Factories instead as much as possible. </P>
 *
 *  @see FastLSTMMemoryBlock
 *  @see AbstractLSTMFactory
 *  @see LSTMFactory
 *  @see LSTMDataNames
 *
 *  @author Francois Rivest
 *  @version 1.0
 *  @since 1.0
 */


public class FastLSTMNetwork extends AbstractFunctionalUnit2 {

    /*********************************************************************/
    //Serial Version UID

    /** Serial version UID. */
    static final long serialVersionUID = -5612768043320548291L;

    /*********************************************************************/
    //Private fields (architecture, see properties)

    /** Indicates whether bias should be connected to the output layer. */
    protected boolean m_BiasToOutput;

    /** Indicates whether input should be connected to the output layer. */
    protected boolean m_InputToOutput;

    /** Indicates whether gates of memory block should be connected to
     * the output layer. */
    protected boolean m_GateToOutput;

    /** Indicates whether gates of memory block should be recurrently connected
     * to memory blocks. */
    protected boolean m_GateToGate;

    /** Number of memory blocks. */
    protected int m_MemoryBlockCount;

    /** Memory blocks. */
    protected FastLSTMMemoryBlock[] m_MemoryBlocks;

    /** Output layer. */
    protected FastSingleLayerNeuralNetwork m_OutputLayer;

    /** Output layer local gradient factor. */
    protected double m_OutputWeightsLocalGradientFactor = 1.0;

    /** Public debug info output. */
    public boolean m_Debug = false;

    /*********************************************************************/
    //Private fields (transient active/previous states, see also reset)

    /** Previous output of memory blocks [MemoryBlockCount][(MemoryBlock)OutputCount]. */
    protected transient double[][] m_PrevMemoryBlocksOutput;

    /*********************************************************************/
    //Constructors

    /** Construct an LSTM network.
     * @param    newInputCount    Number of input to the network.
     * @param    newBlockCount    Number of LSTM memory block.
     * @param    newCellperBlock  Number of memory cells per block.
     * @param    newg             Memory cell input squashing function (g)
     * @param    newh             Memory cell output squashing function (h)
     * @param    newInputGate     Memory block input gate function
     * @param    newForgetGate    Memory block forget gate function
     * @param    newOutputGate    Memory block output gate function
     * @param    newOutputCount   Number of output of the network
     * @param    newSampleOutput  Sample of an output function (should have one
     *                            input and one output, default LogisticUnit(1,0))
     * @param    newGateToGate    Connects block gates to block (default false)
     * @param    newBiasToOutput  Connects bias to output layer (default true)
     * @param    newInputToOutput Connects input to output layer (default true)
     * @param    newGateToOutput  Connects block gates to output layer (default true)
     */
    protected FastLSTMNetwork(int newInputCount,
                              int newBlockCount,
                              int newCellperBlock,
                              FunctionalUnit newg,
                              FunctionalUnit newh,
                              FunctionalUnit newInputGate,
                              FunctionalUnit newForgetGate,
                              FunctionalUnit newOutputGate,
                              int newOutputCount,
                              FunctionalUnit newSampleOutput,
                              boolean newGateToGate,
                              boolean newBiasToOutput,
                              boolean newInputToOutput,
                              boolean newGateToOutput)
   {

        //Argument check
        if (newInputCount < 0) {
            throw new IllegalArgumentException(
                "Number of input must be non negative!");
        }
        if (newBlockCount < 0) {
            throw new IllegalArgumentException(
                "Number of memory block must be non negative!");
        }
        if (!newSampleOutput.isDifferentiable()) {
            throw new IllegalArgumentException(
                "Sample output function must be differentiable!");
        }

        //FunctionalUnit2 properties
        m_InputCount = newInputCount;
        m_OutputCount = newOutputCount;
        m_ParameterCount = 0;//below
        m_IsDifferentiable = false;
        m_IsTwiceDifferentiable = false;
        m_IsParameterDifferentiable = true;
        m_IsParameterTwiceDifferentiable = false;

        //Other properties
        m_BiasToOutput = newBiasToOutput;
        m_InputToOutput = newInputToOutput;
        m_GateToOutput = newGateToOutput;
        m_GateToGate = newGateToGate;
        m_MemoryBlockCount = newBlockCount;

        //Memory blocks
        m_MemoryBlocks = new FastLSTMMemoryBlock[newBlockCount];
        for (int i=0; i<newBlockCount; i++)
        {
            //diffs between the two constructors here
            m_MemoryBlocks[i] = new FastLSTMMemoryBlock(
                1+newInputCount + newBlockCount*(newCellperBlock+(newGateToGate?3:0)),
                newCellperBlock, newg, newh, newInputGate, newForgetGate, newOutputGate);
        }

        //Output layer
        m_OutputLayer = new FastSingleLayerNeuralNetwork(
            (newBiasToOutput?1:0) + (newInputToOutput?newInputCount:0) +
            newBlockCount * (newCellperBlock+(newGateToOutput?3:0)),
            false, newOutputCount, newSampleOutput);

        //Parameter count
        for (int i=0; i<m_MemoryBlockCount; i++)
        {
            m_ParameterCount += m_MemoryBlocks[i].getParameterCount();
        }
        m_ParameterCount += m_OutputLayer.getParameterCount();

        //Transient states initialisation
        reset();

    }

    /*********************************************************************/
    //Properties

    /** Sets a factor used when computing the gradient to the output weights.
     * This factor does not affect the calculation of the gradient of any other
     * weights but the output layer weights. The gradient of these weights are
     * often 2 order of magnitude larger than the gradient of the other weights,
     * allowing more pression on the bias and input to output connections than
     * on any internal weights. Original setting is factor=1, but suggested
     * setting by Rivest is factor=1E-2. This encouraged search on internal
     * weights adjustement.
     */
    protected void setOutputWeightsLocalGradientFactor(double newFactor)
    {
        m_OutputWeightsLocalGradientFactor = newFactor;
    }

    /*********************************************************************/
    //FunctionalUnit2 interface implementation

    public void reset()
    {
        m_PrevMemoryBlocksOutput = new double[m_MemoryBlockCount][];
        for (int i=0; i<m_MemoryBlockCount; i++)
        {
            m_MemoryBlocks[i].reset();
            m_PrevMemoryBlocksOutput[i] = new double[m_MemoryBlocks[i].getOutputCount()];
        }
    }

    public FunctionalUnit2.ProcessPatternResult2 processPattern(
        double[] inputPattern,
        boolean computeDerivative,
        boolean computeSecondDerivative,
        boolean computeParameterDerivative,
        boolean computeParameterSecondDerivative,
        String[] recordList)
    {

        //*** Preprocessing
        FunctionalUnit2.ProcessPatternResult2 ret =
            preProcessPattern(inputPattern,
                               computeDerivative,
                               computeSecondDerivative,
                               computeParameterDerivative,
                               computeParameterSecondDerivative,
                               recordList);

        //*** Forward pass

        //*Memory blocks input vector
        //The input vector to the memory blocks is the concatenation of:
        //1, input pattern, block[0].prevOutput, .... block[n-1].prevOutput.
        //
        //Make space
        int count = 1 + m_InputCount;
        for (int i=0; i<m_MemoryBlockCount; i++)
        {
            count += m_MemoryBlocks[i].getOutputCount()-(m_GateToGate?0:3);
        }
        double[] blockInput = new double[count];
        int index = 0;
        //Bias
        blockInput[index] = 1.0;
        index++;
        //Input
        LinearAlgebra.overwriteSubVector(index, inputPattern, blockInput);
        index += m_InputCount;
        //Memory blocks
        for (int i=0; i<m_MemoryBlockCount; i++)
        {
            if (m_GateToGate) {
                LinearAlgebra.overwriteSubVector(index,
                                                 m_PrevMemoryBlocksOutput[i],
                                                 blockInput);
                index += m_MemoryBlocks[i].getOutputCount();
            } else {
                LinearAlgebra.overwriteSubVector(index,
                                                 m_MemoryBlocks[i].getOutputCount()-3,
                                                 m_PrevMemoryBlocksOutput[i],
                                                 blockInput);
                index += m_MemoryBlocks[i].getOutputCount()-3;
            }
        }
        //Debug
        if (m_Debug) {
            System.out.println("---------------------------------------------");
            System.out.println("Bias: " + 1);
            System.out.println("Input: " + LinearAlgebra.toString(inputPattern));
            for (int i=0; i<m_MemoryBlockCount; i++)
            {
                System.out.println("Previous Memory Block " + i + " Output: "
                                   + LinearAlgebra.toString(m_PrevMemoryBlocksOutput[i]));
            }
            System.out.println("Gate2Gate: " + m_GateToGate);
            System.out.println("Resulting Memory Block Input: " + LinearAlgebra.toString(blockInput));
        }


        //*Process memory blocks
        FunctionalUnit2.ProcessPatternResult2 memoryBlocks[] = new FunctionalUnit2.ProcessPatternResult2[m_MemoryBlockCount];
        for (int i=0; i<m_MemoryBlockCount; i++)
        {
            memoryBlocks[i] = m_MemoryBlocks[i].processPattern(blockInput, false, false, computeParameterDerivative, false, recordList);
        }

        //*Save internal state on request
         ret.extraData = new DataSet();
         if (DataNames.isMember(LSTMDataNames.LSTM_INTERNAL_STATES, recordList)) {
             double[][] int_states = new double[m_MemoryBlockCount][];
             for (int i=0; i<m_MemoryBlockCount; i++)
             {
                 int_states[i] = (double[]) memoryBlocks[i].extraData.getData(LSTMDataNames.LSTM_INTERNAL_STATES);
             }
             ret.extraData.setData(LSTMDataNames.LSTM_INTERNAL_STATES, LinearAlgebra.concatenateRows(int_states));
         }
         if (DataNames.isMember(LSTMDataNames.LSTM_INTERNAL_ACTIVATIONS, recordList)) {
             double[][] int_acts = new double[m_MemoryBlockCount][];
             for (int i=0; i<m_MemoryBlockCount; i++)
             {
                 int_acts[i] = LinearAlgebra.extractVector(memoryBlocks[i].outputPattern, 0, memoryBlocks[i].outputPattern.length-4);
             }
             ret.extraData.setData(LSTMDataNames.LSTM_INTERNAL_ACTIVATIONS, LinearAlgebra.concatenateRows(int_acts));
         }
         if (DataNames.isMember(LSTMDataNames.LSTM_INPUT_GATES, recordList)) {
             double[] gates = new double[m_MemoryBlockCount];
             for (int i=0; i<m_MemoryBlockCount; i++)
             {
                 gates[i] = memoryBlocks[i].outputPattern[memoryBlocks[i].outputPattern.length-3];
             }
             ret.extraData.setData(LSTMDataNames.LSTM_INPUT_GATES, gates);
         }
         if (DataNames.isMember(LSTMDataNames.LSTM_FORGET_GATES, recordList)) {
             double[] gates = new double[m_MemoryBlockCount];
             for (int i=0; i<m_MemoryBlockCount; i++)
             {
                 gates[i] = memoryBlocks[i].outputPattern[memoryBlocks[i].outputPattern.length-2];
             }
             ret.extraData.setData(LSTMDataNames.LSTM_FORGET_GATES, gates);
         }
         if (DataNames.isMember(LSTMDataNames.LSTM_OUTPUT_GATES, recordList)) {
             double[] gates = new double[m_MemoryBlockCount];
             for (int i=0; i<m_MemoryBlockCount; i++)
             {
                 gates[i] = memoryBlocks[i].outputPattern[memoryBlocks[i].outputPattern.length-1];
             }
             ret.extraData.setData(LSTMDataNames.LSTM_OUTPUT_GATES, gates);
         }
         if (ret.extraData.getDataCount() == 0) {ret.extraData = null;}


        //*Output layer input vector
        //The input vector to the memory block is the concatenation of:
        //1, inputPattern, block[0].output, ..., block[n-1].output.
        //But, some may be removed based on the 3 xxxToOutput switch.
        //
        //Make space
        count = (m_BiasToOutput?1:0) + (m_InputToOutput?m_InputCount:0);
        for (int i=0; i<m_MemoryBlockCount; i++)
        {
            count += m_MemoryBlocks[i].getOutputCount()-(m_GateToOutput?0:3);
        }
        double[] outputInput = new double[count];
        index = 0;
        //Bias
        if (m_BiasToOutput) {
            outputInput[index] = 1.0;
            index++;
        }
        //Input
        if (m_InputToOutput) {
            LinearAlgebra.overwriteSubVector(index, inputPattern, outputInput);
            index += m_InputCount;
        }
        //Memory blocks
        for (int i=0; i<m_MemoryBlockCount; i++)
        {
            if (m_GateToOutput) {
                LinearAlgebra.overwriteSubVector(index,
                                                 memoryBlocks[i].outputPattern,
                                                 outputInput);
                index += m_MemoryBlocks[i].getOutputCount();
            } else {
                LinearAlgebra.overwriteSubVector(index,
                                                 m_MemoryBlocks[i].getOutputCount()-3,
                                                 memoryBlocks[i].outputPattern,
                                                 outputInput);
                index += m_MemoryBlocks[i].getOutputCount()-3;
            }
        }
        //Debug
        if (m_Debug) {
            System.out.println("---------------------------------------------");
            System.out.println("Bias: " + 1);
            System.out.println("Input: " + LinearAlgebra.toString(inputPattern));
            for (int i=0; i<m_MemoryBlockCount; i++)
            {
                System.out.println("Memory Block " + i + " Output: "
                                   + LinearAlgebra.toString(memoryBlocks[i].outputPattern));
            }
            System.out.println("Bias2Output: " + m_BiasToOutput);
            System.out.println("Input2Output: " + m_InputToOutput);
            System.out.println("Gate2Output: " + m_GateToOutput);
            System.out.println("Resulting Output Layer Input: " + LinearAlgebra.toString(outputInput));
        }

        //*Output layer process
        FunctionalUnit2.ProcessPatternResult2 output = m_OutputLayer.processPattern(outputInput, computeParameterDerivative, false, computeParameterDerivative, false, recordList);

        //*Construct output vector
        LinearAlgebra.overwriteSubVector(0, output.outputPattern, ret.outputPattern);
        //Debug
        if (m_Debug) {
            System.out.println("---------------------------------------------");
            System.out.println("Output Layer Weights " + LinearAlgebra.toString(m_OutputLayer.getWeights()));
            System.out.println("Output Layer weighted sum " + LinearAlgebra.toString(LinearAlgebra.multMatrixVector(m_OutputLayer.getWeights(), outputInput)));
            System.out.println("Output Layer output: " + LinearAlgebra.toString(output.outputPattern));
            System.out.println("Resulting Network Output: " + LinearAlgebra.toString(ret.outputPattern));
        }

        //*** Derivative computation
        if (computeParameterDerivative) {

            //*Derivative from network output to output layer weights

            //D output[i]/ D weights[j]
            double[][] der2OutputWeights = output.parameterDerivative;
            //Debug
            if (m_Debug) {
                System.out.println(
                    "---------------------------------------------");
                System.out.println(
                    "Derivative from Output Layer to Ouput Layer Weights: "
                    + LinearAlgebra.toString(output.parameterDerivative));
            }

            //*Derivative from network output to memory blocks weights

            //D output[i] /D block[j]weights[k] =
            //    D output[i] /D block[j]output[l] *
            //    D block[j]output[l] / block[j]param[k]
            double[][][] der2BlocksWeights = new double[m_MemoryBlockCount][][];
            //output derivative index
            int start = (m_BiasToOutput?1:0) + (m_InputToOutput?m_InputCount:0);
            int decount = m_GateToOutput?0:3;
            //for each memory block
            for (int j=0; j<m_MemoryBlockCount; j++)
            {
                //extract output derivative sub matrix linking memory block j
                double[][] der2BlockOutput = LinearAlgebra.extractColumns(
                    output.derivative,
                    start,
                    m_MemoryBlocks[j].getOutputCount()-3);
                //remove gate output derivative from block parameter derivative
                double[][] blockOutputDer2BlockParam = LinearAlgebra.extractRows(
                    memoryBlocks[j].parameterDerivative,
                    0,
                    m_MemoryBlocks[j].getOutputCount()-3);
                der2BlocksWeights[j] = LinearAlgebra.multMatrixMatrix(der2BlockOutput, blockOutputDer2BlockParam);
                //update output derivative index
                start += m_MemoryBlocks[j].getOutputCount()-decount;
            }
            //Debug
            if (m_Debug) {
                System.out.println("---------------------------------------------");
                System.out.println("Derivative from Output Layer: "
                                   + LinearAlgebra.toString(output.derivative));
                for (int i=0; i<m_MemoryBlockCount; i++)
                {
                    System.out.println("Derivative from Memory Block " + i + " to Memory Block " + i + " Weights "
                                       + LinearAlgebra.toString(memoryBlocks[i].parameterDerivative));
                }
                for (int i=0; i<m_MemoryBlockCount; i++)
                {
                    System.out.println(
                        "Derivative from Output Layer to Memory Block " + i + " Weights: "
                        + LinearAlgebra.toString(der2BlocksWeights[i]));
                }

            }

            //*Construct parameters derivative

            //Parameters are constructed as follows:
            //    Weights for memory block 1 folowed by
            //    ...
            //    Weights for memory block N followed by
            //    Weights for output layer
            start = 0;
            for (int i=0; i<m_MemoryBlockCount; i++)
            {
                LinearAlgebra.overwriteSubMatrix(0, start, der2BlocksWeights[i], ret.parameterDerivative);
                start += m_MemoryBlocks[i].getParameterCount();
            }
            LinearAlgebra.overwriteSubMatrix(0, start,
              LinearAlgebra.multScalarMatrix(m_OutputWeightsLocalGradientFactor, //Changes in 16Feb06
                 der2OutputWeights), ret.parameterDerivative);
            //Debug
            if (m_Debug) {
                System.out.println("---------------------------------------------");
                System.out.println("OutputWeightsLocalGradientFactor: "
                                   + m_OutputWeightsLocalGradientFactor);
                System.out.println("Derivative from Output to all Weights: "
                                   + LinearAlgebra.toString(ret.parameterDerivative));
            }

        }

        //*** Finalisation & clean up

        //*Back up state values
        for (int i=0; i<m_MemoryBlockCount; i++)
        {
            m_PrevMemoryBlocksOutput[i] = memoryBlocks[i].outputPattern;
        }

        //*Return values
        return ret;
    }

    public double[] getParameters()
    {
        //Parameters are constructed as follows:
        //    Weights for memory block 1 folowed by
        //    ...
        //    Weights for memory block N followed by
        //    Weights for output layer

        double[] ret = new double[0];
        for (int i=0; i<m_MemoryBlockCount; i++)
        {
            ret = LinearAlgebra.concatenateVectors(ret, m_MemoryBlocks[i].getParameters());
        }
        ret = LinearAlgebra.concatenateVectors(ret, m_OutputLayer.getParameters());

        return ret;
    }

    public void setParameters(double[] parameters)
    {

        //Parameters check
        if (!LinearAlgebra.isVector(parameters, m_ParameterCount)) {
            throw new IllegalArgumentException("parameters is of the wrong size!");
        }

        //Parameters are constructed as follows:
        //    Weights for memory block 1 folowed by
        //    ...
        //    Weights for memory block N followed by
        //    Weights for output layer
        int start = 0;
        for (int i=0; i<m_MemoryBlockCount; i++)
        {
            m_MemoryBlocks[i].setParameters(LinearAlgebra.extractVector(parameters, start, start + m_MemoryBlocks[i].getParameterCount()-1));
            start += m_MemoryBlocks[i].getParameterCount();
        }
        m_OutputLayer.setParameters(LinearAlgebra.extractVector(parameters, start, parameters.length-1));

    }


    /*********************************************************************/
    //toString method

    public String toString()
    {

        //Inherited
        String ret = super.toString() + "\n";
        ret += "Class: FASTLSTMNetwork\n";

        //Structure information
        ret += "\tMemoryBlockCount = " + m_MemoryBlockCount + "\n";
        ret += "\tGateToOutput = " + m_GateToOutput + "\n";
        ret += "\tBiasToOutput = " + m_BiasToOutput + "\n";
        ret += "\tInputToOutput = " + m_InputToOutput + "\n";
        ret += "\tGateToOutput = " + m_GateToOutput + "\n";

        //Internal blocks information
        for (int i=0; i<m_MemoryBlockCount; i++)
        {
            ret += "\tMemoryBlocks[" + i + "] = \n";
            ret += Tools.tabText(m_MemoryBlocks[i].toString(),2) + "\n";
        }
        ret += "\tOutputLayer = \n";
        ret += Tools.tabText(m_OutputLayer.toString(),2) + "\n";

        //Extra parameter
        ret += "\tOutputWeightsLocalGradientFactor = " + m_OutputWeightsLocalGradientFactor;

        //Return
        return ret;
    }

    /*********************************************************************/
    //Cloneable/Serializable interface implementation

    public Object clone() {
        return Tools.copyObject(this);
    }


}