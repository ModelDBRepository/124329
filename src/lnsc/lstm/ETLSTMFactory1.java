package lnsc.lstm;

import lnsc.*;

/**
 * <p> Factory for LSTM network using eligibility traces to find more rapidely
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
 *  @see ETLSTMNetwork1
 *  @see ETLSTMMemoryBlock1
 *
 * @author Francois Rivest
 * @version 1.1
 */

public class ETLSTMFactory1 extends LSTMFactory {

    /*********************************************************************/
    //Serial Version UID

    /** Serial version UID. */
    //static final long serialVersionUID = ;

    /*********************************************************************/
    //Private fields (memory block)

    /* Eligibility trace decay rate. */
    protected double m_Lambda = .8;

    /* Indicate whether traces are reset on opposite sign */
    protected boolean m_OppSignResetTraces = true;

    /*********************************************************************/
    //Constructors

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
     * @param    newLambda        Eligibility traces decay rate.
     * @param    newOppSignResetTraces   true to reset traces on opposite sign.
     */
    public ETLSTMFactory1(int newInputCount,
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
                          double newOutputWeightsLocalGradientFactor,
                          double newLambda,
                          boolean newOppSignResetTraces)
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
        m_OutputWeightsLocalGradientFactor = newOutputWeightsLocalGradientFactor;
        m_Lambda = newLambda;
        m_OppSignResetTraces = newOppSignResetTraces;
    }

    /*********************************************************************/
    //FunctionalUnitFactory interface implementation

    public FunctionalUnit createUnit() {
        ETLSTMNetwork1 newNet = new ETLSTMNetwork1(
            m_InputCount,
            m_BlockCount,
            m_CellperBlock,
            m_g,
            m_h,
            m_InputGate,
            m_ForgetGate,
            m_OutputGate,
            m_OutputCount,
            m_SampleOutput,
            m_GateToGate,
            m_BiasToOutput,
            m_InputToOutput,
            m_GateToOutput,
            m_Lambda,
            m_OppSignResetTraces);
        newNet.setOutputWeightsLocalGradientFactor(m_OutputWeightsLocalGradientFactor);
        initializeWeights(newNet);
        return newNet;
    }



}


