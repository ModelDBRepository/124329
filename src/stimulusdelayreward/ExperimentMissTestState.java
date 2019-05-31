package stimulusdelayreward;
import lnsc.page.*;
import lnsc.DataSet;

/** Experiment is a sequence of trial and inter-trial. A trial begin by the
 *  presentation of the stimuli and terminates on juice delivery (reward). An
 *  inter-trial begins after the juice delivery. (A trial is one time-step
 *  longuer tha its delay (for reward delivery).)
 *
 *  In this test version, there are only 5 trials, which I either shorter, or
 *  Miss trials. Their order is chosen randomly. They are build up to be
 *  equivalent to test block, but in late trials, there are no US (hence miss).
 *
 *  Block details:
 *    - InterTrial
 *    - Short or Miss trial
 *    - InterTrial
 *    - Short or Miss trial
 *    - InterTrial
 *    - Short or Miss trial
 *    - InterTrial
 *    - Short or Miss trial
 *    - InterTrial
 *    - Short or Miss trial
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


public class ExperimentMissTestState extends ExperimentTestState {

    /*********************************************************************/
    //Constructors

    /** First constructor to initialize an experiment (a run) that will
     *  begin with an inter-trial at t=0.
     *  @param   fixedDelay    Delay bewtween US onset and CS onset in ms.
     **/
    public ExperimentMissTestState(int fixedDelay) {
        super(fixedDelay);
    }

    /*********************************************************************/
    //Interface implementation

    public void doAction(Action a) {

        //*** From ExperimentState.doAction

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
                    m_CurrentTrialType = MISS; //Changes in MissTest
                } //Changes in MissTest:No normal trials
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
            } else if ((m_CurrentStep == m_CurrentDelay)      //Changes in MissTest
                    && (m_CurrentTrialType == SHORT_CSUS)) {  //From Exp..TestState
                m_Reward = 1;
            }
        }

        //*** End ExperimentState.doAction

        //Automatically stop after 5 trials and 1 extra-trial.
        if (m_CurrentTrial > m_MaxTestTrial) {
            m_Stimulus = 0;
            m_Reward = 0;
            this.m_IsFinal = true;
        }

    }



}
