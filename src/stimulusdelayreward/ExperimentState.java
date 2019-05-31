package stimulusdelayreward;
import lnsc.page.*;
import lnsc.DataSet;

/** Experiment is a sequence of trial and inter-trial. A trial begin by the
 *  presentation of the stimuli and terminates on juice delivery (reward). An
 *  inter-trial begins after the juice delivery. (A trial is one time-step
 *  longuer tha its delay (for reward delivery).)
 *
 *  Trial details:
 *    - At t = 0
 *        -> Present stimulus
 *    - At t in [1, trial_delay-1] (in setps)
 *        -> No signal
 *    - At t = trial_delay (in steps)
 *        -> Present reward
 *    - At t = trial_delay+1
 *        -> == inter-trial at t=0
 *
 *  Inter-trial details
 *    - At t in [0, intertrial_delay-1]
 *        -> No signal
 *    - At t = intertrial_delay
 *        -> == trial at t=0
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

public class ExperimentState extends AbstractState {

    /*********************************************************************/
    //Public constants

    /************** m_TrialType constants **********************/

    public final static int ITI = 0;   //inter-trial interval
    public final static int CS_ONLY = 1;   //delay is 0
    public final static int US_ONLY = 2;   //delay is 0
    public final static int FIX_CSUS = 4;     //normal trace train/delay given by fixed delay
    public final static int SHORT_CSUS = 8;   //shorter trace delay
    public final static int LONG_CSUS = 16;   //longuer trace delay
    public final static int MISS = LONG_CSUS + CS_ONLY; //longuer trace delay, but no US
    public final static int PI_FIX_CSUS = FIX_CSUS + 32;     //PI non-trace
    public final static int PI_SHORT_CSUS = SHORT_CSUS + 32; //PI non-trace
    public final static int PI_LONG_CSUS = LONG_CSUS + 32;   //PI non-trace


    /*********************************************************************/
    //Private fields (experiment constants)

    /** Time per step (time/step) (in ms). */
    protected double m_TimePerStep = 200;

    /** Minimum inter-trial delay (in ms). */
    protected double m_MinInterTrialDelay = 4000;

    /** Maximum inter-trial delay (in ms). */
    protected double m_MaxInterTrialDelay = 6000;

    /** Fixed delay (in ms). */
    protected double m_FixedDelay = 1000;

    /*********************************************************************/
    //Private fields (current state)

    /** Global Current step. (in steps, never reseted) */
    protected int m_Step;

    /** Stimulus signal. */
    protected double m_Stimulus;

    /** Reward signal. */
    protected double m_Reward;

    /** Current step in the current trial or inter-trial. */
    protected int m_CurrentStep;

    /** Current trial or inter-trial delay length (in steps). */
    protected int m_CurrentDelay;

    /** Indicates whether it is during a trial or an inter-trial.
     * (Trial begins on first state presenting stimulus,
     *  inter-trial begin on first state after last state presenting reward.)
     */
    protected boolean m_IsInTrial;

    /** Current trial (0 until first pattern of first trial, etc...). */
    protected int m_CurrentTrial;

    /** Type of trial */
    protected int m_CurrentTrialType;

    /*********************************************************************/
    //Constructors

    /** First constructor to initialize an experiment (a run) that will
     *  begin with an inter-trial at t=0.
     * @param   fixedDelay    Delay between US onset and CS onset in ms.
     **/
    public ExperimentState(int fixedDelay) {

        //Generic State properties
        m_IsObservable = false;
        m_AreActionsFixed = true;
        m_ActionCount = 0;
        m_IsCloneable = true;
        m_SupportsDo = true;

        //Experimental parameters
        m_CurrentDelay = (int) (getInterTrialDelay() / m_TimePerStep);
        m_IsInTrial = false;
        m_FixedDelay = fixedDelay;
    }

    /*********************************************************************/
    //Interface implementation

    public State[] getNextStates(Action a)
    {
        ExperimentState state = (ExperimentState) this.clone();
        state.doAction(a);
        return new State[] {state};
    }

    public void doAction(Action a) {

        //Increase step counters
        m_Step++;
        m_CurrentStep++;
        int fixedDelay = (int) (getFixedDelay() / m_TimePerStep);

        //If the state shift from inter-trial to trial or vice-versa
        if ((m_IsInTrial & (m_CurrentStep == (m_CurrentDelay+1))) ||
            (!m_IsInTrial & (m_CurrentStep == m_CurrentDelay))) {
            //Make the shift
            m_IsInTrial = !m_IsInTrial;
            m_CurrentStep = 0;
            //Select a new delay according to the new state
            if (m_IsInTrial) {
                m_CurrentDelay = (int) (getTrialDelay() / m_TimePerStep);
                m_CurrentTrial++;
                //Check trial type
                if (m_CurrentDelay < fixedDelay) {
                    m_CurrentTrialType = SHORT_CSUS;
                } else if (m_CurrentDelay > fixedDelay) {
                    m_CurrentTrialType = LONG_CSUS;
                } else {
                    m_CurrentTrialType = FIX_CSUS;
                }
            } else {
                m_CurrentDelay = (int) (getInterTrialDelay() / m_TimePerStep);
                m_CurrentTrialType = ITI;
            }
        }

        //Update signals
        m_Stimulus = 0; m_Reward = 0;
        if (m_IsInTrial) {
            if (m_CurrentStep == 0) {
                m_Stimulus = 1;
            } else if (m_CurrentStep == m_CurrentDelay) {
                m_Reward = 1;
            }
        }

    }


       public State[] getObservableStates() {
        return new State[] {new MonkeyObservableState(this)};
    }

    public Action[] getActions() {
       throw new java.lang.UnsupportedOperationException("Method getActions() not implemented.");
    }


    /*********************************************************************/
    //Properties


    /*********************************************************************/
    //Helper

    /** Generates a random delay (based on inter-trial delay limits, uniformly distributed).
     * @return    A sampled inter-trial delay in ms.
     */
    double getInterTrialDelay()
    {
        double diff = m_MaxInterTrialDelay - m_MinInterTrialDelay;
        return m_MinInterTrialDelay + Math.random() * diff;
    }

    /** Returns a fixed delay (trial delay).
     * @return    The training delay in ms.
     */
    double getTrialDelay()
    {
        return m_FixedDelay;
    }

    /** Returns a fixed delay (training delay).
     * @return    The training delay in ms.
     */
    double getFixedDelay()
    {
        return m_FixedDelay;
    }

    /*********************************************************************/
    //toDataSet

    public final static String STEP = "Step";
    public final static String STIMULUS = "Stimulus";
    public final static String REWARD = "Reward";
    public final static String CURRENT_STEP = "CurrentStep";
    public final static String CURRENT_DELAY = "CurrentDelay";
    public final static String IS_IN_TRIAL = "IsInTrial";
    public final static String CURRENT_TRIAL = "CurrentTrial";
    public final static String CURRENT_TRIALTYPE = "CurrentTrialType";


    public DataSet toDataSet() {
        return new DataSet(new String[] {STEP,
                                         STIMULUS,
                                         REWARD,
                                         CURRENT_STEP,
                                         CURRENT_DELAY,
                                         IS_IN_TRIAL,
                                         CURRENT_TRIAL,
                                         CURRENT_TRIALTYPE},
                           new Object[] {new Integer(m_Step),
                                         new Double(m_Stimulus),
                                         new Double(m_Reward),
                                         new Integer(m_CurrentStep),
                                         new Integer(m_CurrentDelay),
                                         new Boolean(m_IsInTrial),
                                         new Integer(m_CurrentTrial),
                                         new Integer(m_CurrentTrialType)});
    }




}
