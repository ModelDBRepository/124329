package stimulusdelayreward;
import lnsc.page.*;
import lnsc.DataSet;

/** Experiment is a sequence of trial and inter-trial. A trial begin by the
 *  presentation of the stimuli and terminates on juice delivery (reward). An
 *  inter-trial begins after the juice delivery. (A trial is one time-step
 *  longuer tha its delay (for reward delivery).)
 *
 *  In this test version, there are only 5 trials, which I either shorter, or
 *  longuer than normal. Their order is chosen randomly.
 *
 *  Block details:
 *    - InterTrial
 *    - Short or Long trial
 *    - InterTrial
 *    - Short or Long trial
 *    - InterTrial
 *    - Short or Long trial
 *    - InterTrial
 *    - Short or Long trial
 *    - InterTrial
 *    - Short or Long trial
 *    - InterTrial
 *    - t=0, isFinal=true
 *  For Trials details, see ExperimentState.
 *
 *  The class getStates uses a combination of clone and doAction.
 *
 *  This class does not provided list of actions, since it is not obervable.
 *
 *  This is not observable, and thus returns an equivalent observable state.
 *
 * @author Francois Rivest
 * @version 1.0
 */


public class ExperimentTestState extends ExperimentState {

    /*********************************************************************/
    //Private fields (experiment constants)

    /** Short Fixed delay (in ms). */
    protected double m_ShortFixedDelay = 500;

    /** Long Fixed delay (in ms). */
    protected double m_LongFixedDelay = 1500;

    /** Indicates the maximum number of test trial in a block. */
    protected int m_MaxTestTrial = 5;

    /*********************************************************************/
    //Constructors

    /** First constructor to initialize an experiment (a run) that will
     *  begin with an inter-trial at t=0.
     *  @param   fixedDelay    Delay bewtween US onset and CS onset in ms.
     **/
    public ExperimentTestState(int fixedDelay) {
        super(fixedDelay);
    }

    /*********************************************************************/
    //Interface implementation

    public void doAction(Action a) {

        super.doAction(a);

        //Automatically stop after 5 trials and 1 extra-trial.
        if (m_CurrentTrial > m_MaxTestTrial) {
            m_Stimulus = 0;
            m_Reward = 0;
            this.m_IsFinal = true;
        }

    }


    /*********************************************************************/
    //Properties


    /*********************************************************************/
    //Helper

    /** Returns a fixed delay (either short or long trial delay).
     *  Also sets m_IsInLongTrial accordingly.
     * @return    A sampled inter-trial delay in ms.
     */
    double getTrialDelay()
    {
        if (Math.random()< .5) {
            return m_ShortFixedDelay;
        } else {
            return m_LongFixedDelay;
        }
    }


    /*********************************************************************/
    //toDataSet


}
