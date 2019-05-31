package lnsc.lstm;

import lnsc.*;
import lnsc.pmvf.*;


/** <P> Memory blocks for Long Short-Term Memory (LSMT) network. </P>
 *
 * <P> Implements Gers, Schraudolph & Schmiduber 2002 (from Journal of Machine
 * Learning Research 3:115-143) LSTM memory block. See also Gers, Schmidhuber,
 * & Cummins 2000 (from Neural Computation, 12(10) 2451-2471) and Hochreiter &
 * Schmidhuber 1997 (from Neural Computation 9(8):1735-1780). </P>
 *
 * <P> This memory block a given number of memory cells, one input gate, one
 * forget gate and one output gate (optional). It also includes all weights
 * coming from a possible input layer, recurrent weights from it own output,
 * recurrent weights from other blocks and local peepwhole weights. The input
 * of the block is the vector of all activations connecting to it (unweighted).
 * The output of the block is given by the memory cell outputs augmented
 * with the input, forget and output gate (in that order). Because weights are
 * internal parameters, only derivatives to parameters is available.</P>
 *
 * <P> Squashing function g() and h() and gate functions should be provided
 * (single input and output, differentiable and stateless). </P>
 *
 * <P> The input vector for a block should look like [1, x_1(t), ..., x_n_in(t),
 * y_1(t-1), ... y_n_rec(t-1)] where x_i(t) is a current input and y_j(t-1) is
 * the activation of a hidden unit having a recurrent link into the block.
 * Note that the first input should be a constant value of 1 (BiasUnit). <P>
 *
 * <P> The output vector for a block looks like [z_1(t), ... z_n_cell(t),
 * z_in(t), z_fgt(t), z_out(t)]. <P>
 *
 * <P> Parameters are all the weights mentionned above. Derivative to parameters
 * represent the derivative of each output z_k(t) with respect to each weights
 * based on the papers formulas. Note that derivatives from outputs z_in(t),
 * z_fgt(t) and z_out(t) (the gates) with respect to parameters are zeroed since
 * the derivatives for the gate weights passes by the z_k(t) paths and because
 * these gate outputs are usually not connected to next (output) layer. (Even
 * when they are, this is not considered a source of error signal.) <P>
 *
 * <P> Memory cells internal states are available for read out through the
 * {@link LSTMDataNames#LSTM_INTERNAL_STATES} keyword. </P>
 *
 * <P> Cloning is done through serialization, transient states are therefore
 * transient to cloning too (e.g. reseted in clones). <P>
 *
 * <P> Avoid using this class directly or deriving it unless you really know
 * what you do. Use Factories instead as much as possible. </P>
 *
 *  @see FastLSTMNetwork
 *  @see AbstractLSTMFactory
 *  @see LSTMFactory
 *  @see LSTMDataNames#LSTM_INTERNAL_STATES
 *
 *  @author Francois Rivest
 *  @version 1.0
 *  @since 1.0
 */


public class FastLSTMMemoryBlock extends AbstractFunctionalUnit2 {

    /*********************************************************************/
    //Serial Version UID

    /** Serial version UID. */
    static final long serialVersionUID = -2933646280520228108L;

   /*********************************************************************/
   //Private fields (architecture, see properties)

   /** Number of memory cells in the block. */
   protected int m_MemoryCellCount;

   /** Input gate processing function (in). */
   protected FunctionalUnit m_InputGate;

   /** Forget gate processing function (fgt). */
   protected FunctionalUnit m_ForgetGate;

   /** Output gate processing function (out). */
   protected FunctionalUnit m_OutputGate;

   /** First processing function (g). */
   protected FunctionalUnit m_g;

   /** Second processing function (h). */
   protected FunctionalUnit m_h;

   /*********************************************************************/
   //Private fields (weights, see parameters)

   /** Weights from the feeding units to the memory cells [MemoryCellCount][InputCount]. */
   protected double[][] m_MemoryCellWeights;

   /** Weights from the feeding units to the input gate [InputCount]. */
   protected double[] m_InputGateWeights;

   /** Weights from the feeding units to the forget gate [InputCount]. */
   protected double[] m_ForgetGateWeights;

   /** Weights from the feeding units to the output gate [InputCount]. */
   protected double[] m_OutputGateWeights;

   /** Weights from the memory cell states to the input gate [MemoryCellCount]. */
   protected double[] m_InputGatePeepholeWeights;

   /** Weights from the memory cell states to the forget gate [MemoryCellCount]. */
   protected double[] m_ForgetGatePeepholeWeights;

   /** Weights from the memory cell states to the output gate [MemoryCellCount]. */
   protected double[] m_OutputGatePeepholeWeights;

   /*********************************************************************/
   //Private fields (transient active/previous states, see also reset)

   /** Internal CEC state value. */
   protected transient double[] m_PreviousState;

   /** Partial derivative from previous state down to forget gate weights. [MemoryCellCount][InputCount]. */
   protected transient double[][] m_PrevCsDer2ForgetWeights;

   /** Partial derivative from previous state down to forget gate peephole weights. [MemoryCellCount][MemoryCellCount]. */
   protected transient double[][] m_PrevCsDer2ForgetPeepholeWeights;

   /** Partial derivative from previous state down to input gate weights. [MemoryCellCount][InputCount]. */
   protected transient double[][] m_PrevCsDer2InputWeights;

   /** Partial derivative from previous state down to input gate peephole weights. [MemoryCellCount][MemoryCellCount]. */
   protected transient double[][] m_PrevCsDer2InputPeepholeWeights;

   /** Partial derivative from previous state down to memory cell weights. [MemoryCellCount][InputCount]. */
   protected transient double[][] m_PrevCsDer2MemoryCellWeights;

   /*********************************************************************/
   //Constructors

   /** Construct an LSTM memory blocks with its input side weights.
    * @param    newInputCount    Number of feeding units.
    * @param    newCellCount     Number of memory cells.
    * @param    newg             Input squashing function (g)
    * @param    newh             Output squashing function (h)
    * @param    newInputGate     Input gate function
    * @param    newForgetGate    Forget gate function
    * @param    newOutputGate    Output gate function
    **/
   protected FastLSTMMemoryBlock(int newInputCount,
                                 int newCellCount,
                                 FunctionalUnit newg,
                                 FunctionalUnit newh,
                                 FunctionalUnit newInputGate,
                                 FunctionalUnit newForgetGate,
                                 FunctionalUnit newOutputGate)
   {
       //Argument check
       if (newInputCount < 1) {
           throw new IllegalArgumentException("Number of feeding units must be strickly positive!");
       }
       if (newCellCount < 1) {
           throw new IllegalArgumentException("Number of memory cells must be strickly positive!");
       }
       checkFunction(newg, "Input squashing function (g)");
       checkFunction(newh, "Output squashing function (h)");
       checkFunction(newInputGate, "Input gate");
       checkFunction(newForgetGate, "Forget gate");
       checkFunction(newOutputGate, "Output gate");

       //Set functions
       m_g = newg;
       m_h = newh;
       m_InputGate = newInputGate;
       m_ForgetGate = newForgetGate;
       m_OutputGate = newOutputGate;

       //FunctionalUnit2 properties
       m_MemoryCellCount = newCellCount;
       m_InputCount = newInputCount;
       m_OutputCount = newCellCount + 3;
       m_ParameterCount = m_InputCount*(m_MemoryCellCount+3) + m_MemoryCellCount*3;
       m_IsDifferentiable = false;
       m_IsTwiceDifferentiable = false;
       m_IsParameterDifferentiable = true;
       m_IsParameterTwiceDifferentiable = false;

       //Weights initialisation
       m_MemoryCellWeights = new double[m_MemoryCellCount][m_InputCount];
       m_InputGateWeights = new double[m_InputCount];
       m_ForgetGateWeights = new double[m_InputCount];
       m_OutputGateWeights = new double[m_InputCount];
       m_InputGatePeepholeWeights = new double[m_MemoryCellCount];
       m_ForgetGatePeepholeWeights = new double[m_MemoryCellCount];
       m_OutputGatePeepholeWeights = new double[m_MemoryCellCount];

       //Transient states initialisation
       reset();
   }

   /*********************************************************************/
   //Helper

   protected void checkFunction(FunctionalUnit f, String label)
   {
       if ((f.getInputCount() != 1) || (f.getOutputCount() != 1) ||
           !f.isStateless() || !f.isDifferentiable()) {
           throw new IllegalArgumentException(label + " should be simple, differentiable and stateless!");
       }
   }

   /*********************************************************************/
   //Properties

   /*********************************************************************/
   //FunctionalUnit2 interface implementation

   public void reset()
   {
       m_PreviousState = new double[m_MemoryCellCount];
       m_PrevCsDer2ForgetWeights = new double[m_MemoryCellCount][m_InputCount];
       m_PrevCsDer2ForgetPeepholeWeights = new double[m_MemoryCellCount][m_MemoryCellCount];
       m_PrevCsDer2InputWeights = new double[m_MemoryCellCount][m_InputCount];
       m_PrevCsDer2InputPeepholeWeights = new double[m_MemoryCellCount][m_MemoryCellCount];
       m_PrevCsDer2MemoryCellWeights = new double[m_MemoryCellCount][m_InputCount];
   }

   public FunctionalUnit2.ProcessPatternResult2 processPattern
   (
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

       //*** Foward pass

       //*Input gate activation (step 1a)
       double netInputGate =
           LinearAlgebra.weightedSum(m_InputGateWeights, inputPattern) +
           LinearAlgebra.weightedSum(m_InputGatePeepholeWeights, m_PreviousState);
       FunctionalUnit.ProcessPatternResult inputGate =
           m_InputGate.processPattern(new double[] {netInputGate}, computeParameterDerivative, false);

       //*Forget gate activation (step 1b)
       double netForgetGate =
           LinearAlgebra.weightedSum(m_ForgetGateWeights, inputPattern) +
           LinearAlgebra.weightedSum(m_ForgetGatePeepholeWeights, m_PreviousState);
       FunctionalUnit.ProcessPatternResult forgetGate =
           m_ForgetGate.processPattern(new double[] {netForgetGate}, computeParameterDerivative, false);

       //*Memory cell activation (step 1c)
       double[] netMemoryCell_1st =
           LinearAlgebra.multMatrixVector(m_MemoryCellWeights, inputPattern);
       FunctionalUnit.ProcessPatternResult[] memoryCell_1st = //g's
           new FunctionalUnit.ProcessPatternResult[m_MemoryCellCount];
       for (int i=0; i<m_MemoryCellCount; i++)
       {
           memoryCell_1st[i] =
               m_g.processPattern(new double[] {netMemoryCell_1st[i]}, computeParameterDerivative, false);
       }
       double[] currentState = new double[m_MemoryCellCount];
       for (int i=0; i<m_MemoryCellCount; i++)
       {
           currentState[i] =
               inputGate.outputPattern[0] * memoryCell_1st[i].outputPattern[0] +
               forgetGate.outputPattern[0] * m_PreviousState[i];
       }

       //*Output gate activation (step 2a)
       double netOutputGate =
           LinearAlgebra.weightedSum(m_OutputGateWeights, inputPattern) +
           LinearAlgebra.weightedSum(m_OutputGatePeepholeWeights, currentState);
       FunctionalUnit.ProcessPatternResult outputGate =
           m_OutputGate.processPattern(new double[] {netOutputGate}, computeParameterDerivative, false);

       //*Memory cell output activation (step 2b)
       double[] netMemoryCell_2nd = currentState;
       FunctionalUnit.ProcessPatternResult[] memoryCell_2nd = //h's
           new FunctionalUnit.ProcessPatternResult[m_MemoryCellCount];
       for (int i=0; i<m_MemoryCellCount; i++)
       {
           memoryCell_2nd[i] =
               m_h.processPattern(new double[] {netMemoryCell_2nd[i]}, computeParameterDerivative, false);
       }
       double[] output = new double[m_MemoryCellCount];
       for (int i = 0; i < m_MemoryCellCount; i++)
       {
           output[i] =
               outputGate.outputPattern[0] * memoryCell_2nd[i].outputPattern[0];
       }

       //*Construct output vector
       for (int i=0; i<m_MemoryCellCount; i++)
       {
           ret.outputPattern[i] = output[i];
       }
       ret.outputPattern[m_MemoryCellCount+0] = inputGate.outputPattern[0];
       ret.outputPattern[m_MemoryCellCount+1] = forgetGate.outputPattern[0];
       ret.outputPattern[m_MemoryCellCount+2] = outputGate.outputPattern[0];

       //*Save internal state on request
        if (DataNames.isMember(LSTMDataNames.LSTM_INTERNAL_STATES, recordList)) {
            ret.extraData = new DataSet();
            ret.extraData.setData(LSTMDataNames.LSTM_INTERNAL_STATES,
                                  LinearAlgebra.copyVector(currentState));
        }

       //*** Derivative computation
       if (computeParameterDerivative) {

           //*Derivative from block output with respect to output gate weights

           //D outputPattern[i] /D netOutputGate =
           //    memoryCell_2nd[i] * outputGate.derivative
           double[] der2netOutputGate = new double[m_MemoryCellCount];
           for (int i=0; i<m_MemoryCellCount; i++)
           {
               der2netOutputGate[i] =
                   outputGate.derivative[0][0] * memoryCell_2nd[i].outputPattern[0];
           }

           //D outputPattern[i] /D outputGateWeights[j] =
           //    der2netOutputGate[i] * inputPattern[j]
           double[][] der2OutputGateWeights = LinearAlgebra.multVectorVector(der2netOutputGate, inputPattern);

           //D outputPattern[i] /D outputGatePeeholeWeights[j] =
           //    der2netOutputGate[i] * currentState[j]
           double[][] der2OutputGatePeepholeWeights = LinearAlgebra.multVectorVector(der2netOutputGate, currentState);

           //*Derivative from block output with respect to current state

           //D outputPattern[i] /D currentState[i] =
           //    outputGate * hprime[i]
           double[] der2CurrentState = new double[m_MemoryCellCount];
           for (int i=0; i<m_MemoryCellCount; i++)
           {
               der2CurrentState[i] =
                   outputGate.outputPattern[0] * memoryCell_2nd[i].derivative[0][0];
           }

           //*Derivative from current state to forget gate weights

           //D currentState[i] /D netForgetGate =
           //    m_PreviousState[i] * forgetGate.derivative
           double[] csder2NetForgetGate =
               LinearAlgebra.multScalarVector(forgetGate.derivative[0][0], m_PreviousState);

           //D currentState[i] /D forgetGateWeights[j] =
           //    csder2NetForgetGate[i] * inputPattern[j] +
           //    prevCsDer2ForgetWeights[i][j] * forgetGate
           double[][] csder2ForgetGateWeights =
               LinearAlgebra.addMatrices(
                   LinearAlgebra.multVectorVector(csder2NetForgetGate, inputPattern),
                   LinearAlgebra.multScalarMatrix(forgetGate.outputPattern[0], m_PrevCsDer2ForgetWeights));

           //D currentState[i] /D forgetGatePeepholeWeights[j] =
           //    csder2NetForgetGate[i] * previousState[j] +
           //    prevCsDer2ForgetPeepholeWeights[i][j] * forgetGate
           double[][] csder2ForgetGatePeepholeWeights =
               LinearAlgebra.addMatrices(
                   LinearAlgebra.multVectorVector(csder2NetForgetGate, m_PreviousState),
                   LinearAlgebra.multScalarMatrix(forgetGate.outputPattern[0], m_PrevCsDer2ForgetPeepholeWeights));

           //*Derivative from block output with respect to forget gate weights

           //D outputPattern[i] /D forgetGateWeights[j] =
           //    der2CurrentState[i] * csder2ForgetGateWeights[i][j]
           double[][] der2ForgetGateWeights = new double[m_MemoryCellCount][];
           for (int i=0; i<m_MemoryCellCount; i++)
           {
               der2ForgetGateWeights[i] =
                   LinearAlgebra.multScalarVector(der2CurrentState[i], csder2ForgetGateWeights[i]);
           }

          //D outputPattern[i] /D forgetGatePeepholeWeights[j] =
          //    der2CurrentState[i] * csder2ForgetGatePeepholeWeights[i][j]
          double[][] der2ForgetGatePeepholeWeights = new double[m_MemoryCellCount][];
          for (int i=0; i<m_MemoryCellCount; i++)
          {
              der2ForgetGatePeepholeWeights[i] =
                  LinearAlgebra.multScalarVector(der2CurrentState[i], csder2ForgetGatePeepholeWeights[i]);
          }

           //*Derivative from current state to input gate weights

           //D currentState[i] /D netInputGate =
           //    memoryCell_1st[i] * inputGate.derivative
           double[] csder2NetInputGate = new double[m_MemoryCellCount];
           for (int i=0; i<m_MemoryCellCount; i++)
           {
               csder2NetInputGate[i] =
                   memoryCell_1st[i].outputPattern[0] * inputGate.derivative[0][0];
           }

           //D currentState[i] /D inputGateWeights[j] =
           //    csder2NetInputGate[i] * inputPattern[j] +
           //    prevCsDer2InputWeights[i][j] * forgetGate
           double[][] csder2InputGateWeights =
               LinearAlgebra.addMatrices(
                  LinearAlgebra.multVectorVector(csder2NetInputGate, inputPattern),
                  LinearAlgebra.multScalarMatrix(forgetGate.outputPattern[0], m_PrevCsDer2InputWeights));

           //D currentState[i] /D inputGatePeepholeWeights[j] =
           //    csder2NetInputGate[i] * previousState[j] +
           //    prevCsDer2InputPeepholeWeights[i][j] * forgetGate
           double[][] csder2InputGatePeepholeWeights =
               LinearAlgebra.addMatrices(
                   LinearAlgebra.multVectorVector(csder2NetInputGate, m_PreviousState),
                   LinearAlgebra.multScalarMatrix(forgetGate.outputPattern[0], m_PrevCsDer2InputPeepholeWeights));

           //*Derivative from block output with respect to input gate weights

           //D outputPattern[i] /D inputGateWeights[j] =
           //    der2CurrentState[i] * csder2InputGateWeights[i][j]
           double[][] der2InputGateWeights = new double[m_MemoryCellCount][];
           for (int i=0; i<m_MemoryCellCount; i++)
           {
               der2InputGateWeights[i] =
                   LinearAlgebra.multScalarVector(der2CurrentState[i], csder2InputGateWeights[i]);
           }

           //D outputPattern[i] /D inputGatePeepholeWeights[j] =
           //    der2CurrentState[i] * csder2InputGatePeepholeWeights[j]
           double[][] der2InputGatePeepholeWeights = new double[m_MemoryCellCount][];
           for (int i=0; i<m_MemoryCellCount; i++)
           {
               der2InputGatePeepholeWeights[i] =
                   LinearAlgebra.multScalarVector(der2CurrentState[i], csder2InputGatePeepholeWeights[i]);
           }

           //*Derivative from current state to memory cell weights

           //D currentState[i] /D netMemoryCell_1st =
           //    inputGate * memoryCell_1st[i].derivative
           double[] csder2NetMemoryCell_1st = new double[m_MemoryCellCount];
           for (int i=0; i<m_MemoryCellCount; i++)
           {
               csder2NetMemoryCell_1st[i] =
                  inputGate.outputPattern[0] * memoryCell_1st[i].derivative[0][0];
           }

           //D currentState[i] /D memoryCellWeights[j] =
           //    csder2NetMemoryCell_1st[i] * inputPattern[j] +
           //    prevCsDer2MemoryCellWeights[i][j] * forgetGate
           double[][] csder2MemoryCellWeights =
               LinearAlgebra.addMatrices(
                   LinearAlgebra.multVectorVector(csder2NetMemoryCell_1st, inputPattern),
                   LinearAlgebra.multScalarMatrix(forgetGate.outputPattern[0], m_PrevCsDer2MemoryCellWeights));

           //*Derivative from block output with respect to memory cell weights

           //D outputPattern[i] /D memoryCellWeights[i][j] =
           //    der2CurrentState[i] * csder2MemoryCellWeights[i][j]
           double[][] der2MemoryCellWeights = new double[m_MemoryCellCount][];
           for (int i=0; i<m_MemoryCellCount; i++)
           {
               der2MemoryCellWeights[i] =
                   LinearAlgebra.multScalarVector(der2CurrentState[i], csder2MemoryCellWeights[i]);
           }

           //*Construct parameters derivative

            //Parameters are constructed as follows:
            //    Weights for memory cell 1 folowed by
            //    ...
            //    Weights for memory cell N followed by
            //    Weights to input gate (followed by peephole its weights) followed by
            //    Weights to forget gate (followed by peephole its weights) followed by
            //    Weights to output gate (followed by peephole its weights)

            //Only memory cells derivatives are returned
            for (int i=0; i<m_MemoryCellCount; i++)
            {
                //der2MemoryCellWeights[i] applies only to derivative from its own output, other are zeroed
                LinearAlgebra.overwriteSubVector(i*m_InputCount, der2MemoryCellWeights[i], ret.parameterDerivative[i]);
            }
            //From memory cells to gate weights derivatives
            LinearAlgebra.overwriteSubMatrix(0, m_InputCount*(m_MemoryCellCount+0)+m_MemoryCellCount*(0), der2InputGateWeights, ret.parameterDerivative);
            LinearAlgebra.overwriteSubMatrix(0, m_InputCount*(m_MemoryCellCount+1)+m_MemoryCellCount*(0), der2InputGatePeepholeWeights, ret.parameterDerivative);
            LinearAlgebra.overwriteSubMatrix(0, m_InputCount*(m_MemoryCellCount+1)+m_MemoryCellCount*(1), der2ForgetGateWeights, ret.parameterDerivative);
            LinearAlgebra.overwriteSubMatrix(0, m_InputCount*(m_MemoryCellCount+2)+m_MemoryCellCount*(1), der2ForgetGatePeepholeWeights, ret.parameterDerivative);
            LinearAlgebra.overwriteSubMatrix(0, m_InputCount*(m_MemoryCellCount+2)+m_MemoryCellCount*(2), der2OutputGateWeights, ret.parameterDerivative);
            LinearAlgebra.overwriteSubMatrix(0, m_InputCount*(m_MemoryCellCount+3)+m_MemoryCellCount*(2), der2OutputGatePeepholeWeights, ret.parameterDerivative);


       //*** Finalisation & clean up

           //*Backup gradient state values
           m_PrevCsDer2ForgetWeights = csder2ForgetGateWeights;
           m_PrevCsDer2ForgetPeepholeWeights = csder2ForgetGatePeepholeWeights;
           m_PrevCsDer2InputWeights = csder2InputGateWeights;
           m_PrevCsDer2InputPeepholeWeights = csder2InputGatePeepholeWeights;;
           m_PrevCsDer2MemoryCellWeights = csder2MemoryCellWeights;

       }

       //*Back up state values
       m_PreviousState = currentState;

       //*Return values
       return ret;
   }

   public double[] getParameters()
   {
       //Parameters are constructed as follows:
       //    Weights for memory cell 1 folowed by
       //    ...
       //    Weights for memory cell N followed by
       //    Weights to input gate (followed by its peephole weights) followed by
       //    Weights to forget gate (followed by its peephole weights) followed by
       //    Weights to output gate (followed by its peephole weights)

       double[] mem = LinearAlgebra.concatenateRows(m_MemoryCellWeights);
       double[] in = LinearAlgebra.concatenateVectors(m_InputGateWeights, m_InputGatePeepholeWeights);
       double[] fgt = LinearAlgebra.concatenateVectors(m_ForgetGateWeights, m_ForgetGatePeepholeWeights);
       double[] out = LinearAlgebra.concatenateVectors(m_OutputGateWeights, m_OutputGatePeepholeWeights);
       return LinearAlgebra.concatenateVectors(mem,
                  LinearAlgebra.concatenateVectors(in,
                      LinearAlgebra.concatenateVectors(fgt, out)));
   }

   public void setParameters(double[] parameters)
   {

       //Parameters check
       if (!LinearAlgebra.isVector(parameters, m_ParameterCount)) {
           throw new IllegalArgumentException("parameters is of the wrong size!");
       }

       //Parameters are constructed as follows:
       //    Weights for memory cell 1 folowed by
       //    ...
       //    Weights for memory cell N followed by
       //    Weights to input gate (followed by its peephole weights) followed by
       //    Weights to forget gate (followed by its peephole weights) followed by
       //    Weights to output gate (followed by its peephole weights)

       //Extract
       double[][] mem = LinearAlgebra.cutInRows(LinearAlgebra.extractVector(parameters, 0, m_MemoryCellCount*m_InputCount-1), m_InputCount);
       double[] in = LinearAlgebra.extractVector(parameters, m_InputCount*(m_MemoryCellCount+0)+m_MemoryCellCount*(0), m_InputCount*(m_MemoryCellCount+1)+m_MemoryCellCount*(0)-1);
       double[] phin = LinearAlgebra.extractVector(parameters, m_InputCount*(m_MemoryCellCount+1)+m_MemoryCellCount*(0), m_InputCount*(m_MemoryCellCount+1)+m_MemoryCellCount*(1)-1);
       double[] fgt = LinearAlgebra.extractVector(parameters, m_InputCount*(m_MemoryCellCount+1)+m_MemoryCellCount*(1), m_InputCount*(m_MemoryCellCount+2)+m_MemoryCellCount*(1)-1);
       double[] phfgt = LinearAlgebra.extractVector(parameters, m_InputCount*(m_MemoryCellCount+2)+m_MemoryCellCount*(1), m_InputCount*(m_MemoryCellCount+2)+m_MemoryCellCount*(2)-1);
       double[] out = LinearAlgebra.extractVector(parameters, m_InputCount*(m_MemoryCellCount+2)+m_MemoryCellCount*(2), m_InputCount*(m_MemoryCellCount+3)+m_MemoryCellCount*(2)-1);
       double[] phout = LinearAlgebra.extractVector(parameters, m_InputCount*(m_MemoryCellCount+3)+m_MemoryCellCount*(2), m_InputCount*(m_MemoryCellCount+3)+m_MemoryCellCount*(3)-1);

       //Save
       m_MemoryCellWeights = mem;
       m_InputGateWeights = in;
       m_InputGatePeepholeWeights = phin;
       m_ForgetGateWeights = fgt;
       m_ForgetGatePeepholeWeights = phfgt;
       m_OutputGateWeights = out;
       m_OutputGatePeepholeWeights = phout;

   }

   /*********************************************************************/
   //toString method

   public String toString()
   {

       //Inherited
       String ret = super.toString() + "\n";
       ret += "Class: FASTLSTMMemoryBlock\n";

       //Structure information
       ret += "\tMemoryCellCount = " + m_MemoryCellCount + "\n";
       ret += "\tFunction g() = \n";
       ret += Tools.tabText(m_g.toString(),2) + "\n";
       ret += "\tFunction h() = \n";
       ret += Tools.tabText(m_h.toString(),2) + "\n";
       ret += "\tInput gate = \n";
       ret += Tools.tabText(m_InputGate.toString(),2) + "\n";
       ret += "\tForget gate = \n";
       ret += Tools.tabText(m_ForgetGate.toString(),2) + "\n";
       ret += "\tOutput gate = \n";
       ret += Tools.tabText(m_OutputGate.toString(),2) + "\n";

       //Weights structure
       ret += "\tMemoryCellWeights = \n";
       ret += Tools.tabText(LinearAlgebra.toString(m_MemoryCellWeights),2) + "\n";
       ret += "\tInputGateWeights = \n";
       ret += Tools.tabText(LinearAlgebra.toString(m_InputGateWeights),2) + "\n";
       ret += "\tInputGatePeepholeWeights = \n";
       ret += Tools.tabText(LinearAlgebra.toString(m_InputGatePeepholeWeights),2) + "\n";
       ret += "\tForgetGateWeights = \n";
       ret += Tools.tabText(LinearAlgebra.toString(m_ForgetGateWeights),2) + "\n";
       ret += "\tForgetGatePeepholeWeights = \n";
       ret += Tools.tabText(LinearAlgebra.toString(m_ForgetGatePeepholeWeights),2) + "\n";
       ret += "\tOutputGateWeights = \n";
       ret += Tools.tabText(LinearAlgebra.toString(m_OutputGateWeights),2) + "\n";
       ret += "\tOutputGatePeepholeWeights = \n";
       ret += Tools.tabText(LinearAlgebra.toString(m_OutputGatePeepholeWeights),2);

       //State value
       if (m_PreviousState != null) {
           ret += "\n\tMemoryCellState = \n";
           ret += Tools.tabText(LinearAlgebra.toString(m_PreviousState), 2);
       }

       //Return
       return ret;
   }

   /*********************************************************************/
   //Cloneable/Serializable interface implementation

   public Object clone() {
       return Tools.copyObject(this);
   }

}