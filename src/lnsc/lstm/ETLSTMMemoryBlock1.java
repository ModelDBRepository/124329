package lnsc.lstm;

import lnsc.*;
import lnsc.pmvf.*;

/**
 * <p> LSTM memory block using eligibility traces to find more rapidely
 * association in time space. </p>
 *
 * <P> Special memory blocks are used. These blocks maintain memory traces for
 * their input and used them in derivatives instead of using raw input. Traces
 * are build as if input where bound between [-1, 1]. For a given input x_t
 * it traces e_t = bound(lamdba(e_t) + x_t,[-1,1]), unless e_t and x_t have
 * opposite sign and OppSignResetTraces is true, in whic cases e_t = bound(x_t).
 * This is a formed of bounded cumulated traces.  </P>
 *
 * <p> The implementation is totally transparent to the network, since it is
 * the memoryblock parameters derivative that are properly constructed. </p>
 *
 * <p> It is unclear whether each gate should have a different trace decay rate
 * and whether the peepwhole should have trace at all. Right now, they are
 * all the same.<p>
 *
 * <p> Also note that gradient are defined recursively, so traces may be wrong. <p>
 *
 * <p> Not to be used by itself </p>
 *
 * @see ETLSTMNetwork1
 * @author Francois Rivest
 * @version 1.1
 */

public class ETLSTMMemoryBlock1 extends FastLSTMMemoryBlock {

    /*********************************************************************/
    //Serial Version UID

    /** Serial version UID. */
    static final long serialVersionUID = 1663434558712183194L;

    /*********************************************************************/
    //Private fields (architecture, see properties)

    /* Eligibility trace decay rate. */
    protected double m_Lambda = .8;

    /* Indicate whether traces are reset on opposite sign */
    protected boolean m_OppSignResetTraces = true;

    /*********************************************************************/
    //Private fields (transient active/previous states, see also reset)

    /** Input eligibility traces. */
    protected transient double[] m_InputeTraces;

    /** Memory cell eligibility traces. */
    protected transient double[] m_CelleTraces;

    /*********************************************************************/
    //Constructors

    /** Construct an ETLSTM memory blocks with its input side weights.
    * @param    newInputCount    Number of feeding units.
    * @param    newCellCount     Number of memory cells.
    * @param    newg             Input squashing function (g)
    * @param    newh             Output squashing function (h)
    * @param    newInputGate     Input gate function
    * @param    newForgetGate    Forget gate function
    * @param    newOutputGate    Output gate function
    * @param    newLambda        Eligibility traces decay rate.
    * @param    newOppSignResetTraces   true to reset traces on opposite sign.
    **/
    public ETLSTMMemoryBlock1(int newInputCount, int newCellCount,
                              FunctionalUnit newg, FunctionalUnit newh,
                              FunctionalUnit newInputGate, FunctionalUnit newForgetGate, FunctionalUnit newOutputGate,
                              double newLambda, boolean newOppSignResetTraces)
    {
        //Use existing struture
        super(newInputCount, newCellCount,
              newg, newh,
              newInputGate, newForgetGate, newOutputGate);

        //Set Lambda & traces mode
        m_Lambda = newLambda;
        m_OppSignResetTraces = newOppSignResetTraces;
    }

    /*********************************************************************/
    //Helpers

    /** Bounds value between -1 and 1.
     */
    protected double bound(double x)
    {
        return Math.max(Math.min(x,1),-1);
    }

    /** Function comparing two values:, if sign are opposite, it returns 0;
     * if signs are both negative, or 0 and negative, it returns -1;
     * if signs are both positive, or 0 and positive (or 2 0's), it returns 1.
     */
    protected int getSign(double a, double b)
    {
       //opposite sign
       if (a*b < 0) {return 0;}
       //one or two negative
       if ((a < 0) || (b < 0)) {return -1;}
       //zero, one or two positive
       return 1;
    }

    /** Compute the input eligibility traces, given the new input pattern. */
    protected void computeInputeTraces(double[] inputPattern)
    {
        for (int i=0; i<m_InputCount; i++)
        {
            int sign = getSign(inputPattern[i], m_InputeTraces[i]);
            //if opposite sign, & reset traces, reset traces
            if ((sign == 0) && m_OppSignResetTraces) {
                m_InputeTraces[i] = bound(inputPattern[i]);
            //if both same sign
            } else {
                m_InputeTraces[i] = bound(m_InputeTraces[i]*m_Lambda + inputPattern[i]);
            }
        }
    }

    /** Compute the memory cell eligibility traces, given their new activation. */
    protected void computeCelleTraces(double[] memoryCell)
    {
        for (int i=0; i<m_MemoryCellCount; i++)
        {
            int sign = getSign(memoryCell[i], m_InputeTraces[i]);
            //if opposite sign, reset trace
            if ((sign == 0) && m_OppSignResetTraces) {
                m_CelleTraces[i] = bound(memoryCell[i]);
            //if both same sign
            } else {
                m_CelleTraces[i] = bound(m_CelleTraces[i]*m_Lambda + memoryCell[i]);
            }
        }
    }


    /*********************************************************************/
    //FunctionalUnit2 interface implementation

    public void reset()
    {
        //Use existing struture
        super.reset();

        //Reset traces
        m_InputeTraces = new double[m_InputCount];
        m_CelleTraces = new double[m_MemoryCellCount];
    }

    //This is mostly copied except few lines marked /***ET***/
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

            //Input eTraces are updated before any input processing /***ET***/
            computeInputeTraces(inputPattern); /***ET***/
            //Because Input and Forget gate depends on previous memory/***ET***/
            //cell traces and the output gate depends on the new  /***ET***/
            //current trace, save a copy here.  /***ET***/
            double[] prevCelleTraces = LinearAlgebra.copyVector(m_CelleTraces); /***ET***/
            computeCelleTraces(currentState); /***ET***/

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
            double[][] der2OutputGateWeights = LinearAlgebra.multVectorVector(der2netOutputGate, m_InputeTraces); /***ET***/

            //D outputPattern[i] /D outputGatePeeholeWeights[j] =
            //    der2netOutputGate[i] * currentState[j]
            double[][] der2OutputGatePeepholeWeights = LinearAlgebra.multVectorVector(der2netOutputGate, m_CelleTraces); /***ET***/

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
                    LinearAlgebra.multVectorVector(csder2NetForgetGate, m_InputeTraces), /***ET***/
                    LinearAlgebra.multScalarMatrix(forgetGate.outputPattern[0], m_PrevCsDer2ForgetWeights));

            //D currentState[i] /D forgetGatePeepholeWeights[j] =
            //    csder2NetForgetGate[i] * previousState[j] +
            //    prevCsDer2ForgetPeepholeWeights[i][j] * forgetGate
            double[][] csder2ForgetGatePeepholeWeights =
                LinearAlgebra.addMatrices(
                    LinearAlgebra.multVectorVector(csder2NetForgetGate, prevCelleTraces), /***ET***/
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
                   LinearAlgebra.multVectorVector(csder2NetInputGate, m_InputeTraces), /***ET***/
                   LinearAlgebra.multScalarMatrix(forgetGate.outputPattern[0], m_PrevCsDer2InputWeights));

            //D currentState[i] /D inputGatePeepholeWeights[j] =
            //    csder2NetInputGate[i] * previousState[j] +
            //    prevCsDer2InputPeepholeWeights[i][j] * forgetGate
            double[][] csder2InputGatePeepholeWeights =
                LinearAlgebra.addMatrices(
                    LinearAlgebra.multVectorVector(csder2NetInputGate, prevCelleTraces), /***ET***/
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
                    LinearAlgebra.multVectorVector(csder2NetMemoryCell_1st, m_InputeTraces), /***ET***/
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

    /*********************************************************************/
    //toString method

    public String toString()
    {

        //Inherited
        String ret = super.toString() + "\n";
        ret += "Class: ETLSTMMemoryBlock1\n";

        //Other
        ret += "\tLambda = " + m_Lambda;
        ret += "\tOppSignResetTrace = " + m_OppSignResetTraces;

        //Return
        return ret;
    }

}