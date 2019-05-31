package stimulusdelayreward;

import lnsc.page.*;


/** This class allows automatic conversion of MonkeyObservableState into up to
 *  3 signals (bias, CS, and US, in that order).
 *
 * @author Francois Rivest
 * @version 1.0
 */

public class FlexibleSignalStateRepresentation extends AbstractStateRepresentation {

    /*********************************************************************/
    //Serial Version UID

    /** Serial version UID. */
    static final long serialVersionUID = -3888532411431484943L;

    /*********************************************************************/
    //Private fields

    /** Indicates whether there is a bias signal. */
    boolean m_Bias;

    /** Indicates whether there is a CS signal. */
    boolean m_CS;

    /** Indicates whether there is a US (reward) signal. */
    boolean m_US;

    /*********************************************************************/
	//Constructors

    /** Creates a flexible signal representation that may contain any of bias,
     * cs and us (in that order) depending on the flags below.
     * @param  bias    true for the representation to have a bias signal of 1
     * @param  cs      true for the representation to contain the CS signal
     * @param  us      true for the representation to contain the US signal
     */
    public FlexibleSignalStateRepresentation(boolean bias, boolean cs, boolean us)
    {
        m_OutputCount = (bias?1:0) + (cs?1:0) + (us?1:0);
        m_Bias = bias;
        m_CS = cs;
        m_US = us;
    }

    public double[] getRepresentation(State s) {

        //Make space, load state
        MonkeyObservableState state = (MonkeyObservableState) s;
        double[] ret = new double[m_OutputCount];
        int i = 0;

        //Fill signal
        if (m_Bias) {
            ret[i] = 1;
            i++;
        }
        if (m_CS) {
            ret[i] = state.getStimulusSignal();
            i++;
        }
        if (m_US) {
            ret[i] = state.getRewardSignal();
            i++;
        }

        //Return
        return ret;
    }

}