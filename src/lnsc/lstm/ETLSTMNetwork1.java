package lnsc.lstm;

import lnsc.*;

/**
 * <p> LSTM network using eligibility traces to find more rapidely
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
 * <p> Use factories to make it. </p>
 *
 * @see ETLSTMMemoryBlock1
 * @see ETLSTMFactory1
 *
 * @author Francois Rivest
 * @version 1.1
 */

public class ETLSTMNetwork1 extends FastLSTMNetwork {

    /*********************************************************************/
    //Serial Version UID

    /** Serial version UID. */
    static final long serialVersionUID = 5026839378043968864L;

    /*********************************************************************/
    //Constructors

    /** Construct an LSTM network.
     * @param    newInputCount    Number of input to the network.
     * @param    newBlockCount    Number of LSTM memory block.
     * @param    newCellperBlock  Number of memory cells per block.
     * @param    newg             Input squashing function (g)
     * @param    newh             Output squashing function (h)
     * @param    newInputGate     Input gate function
     * @param    newForgetGate    Forget gate function
     * @param    newOutputGate    Output gate function
     * @param    newOutputCount   Number of output of the network
     * @param    newSampleOutput  Sample of an output function (should have one
     *                            input and one output, default LogisticUnit(1,0))
     * @param    newGateToGate    Connects block gates to block (default false)
     * @param    newBiasToOutput  Connects bias to output layer (default true)
     * @param    newInputToOutput Connects input to output layer (default true)
     * @param    newGateToOutput  Connects block gates to output layer (default true)
     * @param    newLambda        Eligibility traces decay rate.
     * @param    newOppSignResetTraces   true to reset traces on opposite sign.
     */
    public ETLSTMNetwork1(int newInputCount,
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
                          boolean newGateToOutput,
                          double newLambda,
                          boolean newOppSignResetTraces) {

        super(newInputCount,
              newBlockCount, newCellperBlock,
              newg, newh,
              newInputGate, newForgetGate, newOutputGate,
              newOutputCount, newSampleOutput,
              newGateToGate, newBiasToOutput, newInputToOutput, newGateToOutput);

        //Use new memory blocks (almost copied, just used a different constructor)
        for (int i=0; i<newBlockCount; i++)
        {
            m_MemoryBlocks[i] = new ETLSTMMemoryBlock1(
                1+newInputCount + newBlockCount*(newCellperBlock+(newGateToGate?3:0)), newCellperBlock,
                newg, newh,
                newInputGate, newForgetGate, newOutputGate,
                newLambda, newOppSignResetTraces);
        }

    }



    /*********************************************************************/
    //toString method

    public String toString()
    {

        //Inherited
        String ret = super.toString() + "\n";
        ret += "Class: ETLSTMNetwork1\n";

        //Return
        return ret;
    }


}
