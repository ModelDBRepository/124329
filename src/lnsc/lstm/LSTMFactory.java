package lnsc.lstm;

import lnsc.*;

/** <P> Basis to write a Long Short-Term Memory (LSMT) network factory. </P>
 *
 * <P> Basis for Gers, Schraudolph & Schmiduber 2002 (from Journal of Machine
 * Learning Research 3:115-143) LSTM network. See also Gers, Schmidhuber, &
 * Cummins 2000 (from Neural Computation, 12(10) 2451-2471) and Hochreiter &
 * Schmidhuber 1997 (from Neural Computation 9(8):1735-1780). </P>
 *
 * <P> This factory comes with some connectivity and activation options. All
 * weights are initialized with value in the range [-.1,.1]. </P>
 *
 *  @see FastLSTMNetwork
 *  @see AbstractLSTMFactory
 *
 * @author Francois Rivest
 * @version 1.0
 */

public class LSTMFactory extends AbstractLSTMFactory {

    /*********************************************************************/
    //Serial Version UID

    /** Serial version UID. */
    //static final long serialVersionUID = ;

    /*********************************************************************/
    //Private fields (memory block)

    /** Initial weights range. */
    protected double[] m_WeightsRange = new double[] {-.1, .1};

    /*********************************************************************/
    //Constructors

    /** Default for inheritance.
     */
    protected LSTMFactory() {}

    /** Construct an LSTM network factory.
     * @param    newInputCount    Number of input to the network.
     * @param    newBlockCount    Number of memory block.
     * @param    newCellperBlock  Number of memory cells per block.
     * @param    newSquashInput   true to squash input to cell (default)
     *                            false without (use identity instead)
     * @param    newSquashOutput  true to squash output of cell as in older papers
     *                            false without squashing (default)
     * @param    newOutputCount   Number of output of the network
     * @param    newSampleOutput  Sample of an output function (should have one
     *                            input and one output, default LogisticUnit(1,0))
     * @param    newGateToGate    Connects block gates to block (default false)
     * @param    newBiasToOutput  Connects bias to output layer (default true)
     * @param    newInputToOutput Connects input to output layer (default true)
     * @param    newGateToOutput  Connects block gates to output layer (default true)
     */
    public LSTMFactory(int newInputCount,
                       int newBlockCount,
                       int newCellperBlock,
                       boolean newSquashInput,
                       boolean newSquashOutput,
                       int newOutputCount,
                       FunctionalUnit newSampleOutput,
                       boolean newGateToGate,
                       boolean newBiasToOutput,
                       boolean newInputToOutput,
                       boolean newGateToOutput)
    {
        m_InputCount = newInputCount;
        m_BlockCount = newBlockCount;
        m_CellperBlock = newCellperBlock;
        m_g = newSquashInput ? (FunctionalUnit) new LogisticUnit(2,-1) : (FunctionalUnit) new LinearUnit();
        m_h = newSquashOutput ? (FunctionalUnit) new LogisticUnit(2,-1) : (FunctionalUnit) new LinearUnit();
        m_OutputCount = newOutputCount;
        m_SampleOutput = newSampleOutput;
        m_GateToGate = newGateToGate;
        m_BiasToOutput = newBiasToOutput;
        m_InputToOutput = newInputToOutput;
        m_GateToOutput = newGateToOutput;
    }

    /** Construct an LSTM network factory.
     * @param    newInputCount    Number of input to the network.
     * @param    newBlockCount    Number of memory block.
     * @param    newCellperBlock  Number of memory cells per block.
     * @param    newSquashInput   true to squash input to cell (default)
     *                            false without (use identity instead)
     * @param    newSquashOutput  true to squash output of cell as in older papers
     *                            false without squashing (default)
     * @param    newOutputCount   Number of output of the network
     * @param    newSampleOutput  Sample of an output function (should have one
     *                            input and one output, default LogisticUnit(1,0))
     * @param    newGateToGate    Connects block gates to block (default false)
     * @param    newBiasToOutput  Connects bias to output layer (default true)
     * @param    newInputToOutput Connects input to output layer (default true)
     * @param    newGateToOutput  Connects block gates to output layer (default true)
     * @param    newOutputWeightsLocalGradientFactor  Scales the gradien of the
     *                                                output weigths internally.
     */
    public LSTMFactory(int newInputCount,
                       int newBlockCount,
                       int newCellperBlock,
                       boolean newSquashInput,
                       boolean newSquashOutput,
                       int newOutputCount,
                       FunctionalUnit newSampleOutput,
                       boolean newGateToGate,
                       boolean newBiasToOutput,
                       boolean newInputToOutput,
                       boolean newGateToOutput,
                       double newOutputWeightsLocalGradientFactor)
    {
        this(newInputCount,
             newBlockCount,
             newCellperBlock,
             newSquashInput,
             newSquashOutput,
             newOutputCount,
             newSampleOutput,
             newGateToGate,
             newBiasToOutput,
             newInputToOutput,
             newGateToOutput);
        m_OutputWeightsLocalGradientFactor = newOutputWeightsLocalGradientFactor;
    }

    /*********************************************************************/
    //Helpers

    /** Returns a random weight inside the weights range of the network.
     *  @return     A random weights value.
     */
    protected double randomWeight()
    {
        double dist = m_WeightsRange[1] - m_WeightsRange[0];
        return Math.random()*dist + m_WeightsRange[0];
    }

    /** Initialize a weights vector randomly using the weights range uniformly.
     *  @param      w             The weights matrix to initialize.
     */
    protected void randomWeights(double[] w)
    {
        for (int i=0; i<w.length; i++)
        {
            w[i] = randomWeight();
        }
    }

    /** Initialize a weights matrix randomly using the weights range uniformly.
     *  @param      w             The weights matrix to initialize.
     */
    protected void randomWeights(double[][] w)
    {
        for (int i=0; i<w.length; i++)
        {
           randomWeights(w[i]);
        }
    }

    /*********************************************************************/
    //AbstractLSTMFactory interface implementation

    protected void initializeWeights(FastLSTMNetwork newNet) {
        //Memory blocks weights
        for (int i=0; i<newNet.m_MemoryBlocks.length; i++)
        {
            //Gates weights
            randomWeights(newNet.m_MemoryBlocks[i].m_InputGateWeights);
            randomWeights(newNet.m_MemoryBlocks[i].m_InputGatePeepholeWeights);
            randomWeights(newNet.m_MemoryBlocks[i].m_ForgetGateWeights);
            randomWeights(newNet.m_MemoryBlocks[i].m_ForgetGatePeepholeWeights);
            randomWeights(newNet.m_MemoryBlocks[i].m_OutputGateWeights);
            randomWeights(newNet.m_MemoryBlocks[i].m_OutputGatePeepholeWeights);
            //Memory cells weights
            randomWeights(newNet.m_MemoryBlocks[i].m_MemoryCellWeights);
        }
        //Output layer weights
        randomWeights(newNet.m_OutputLayer.getWeights());
    }

}