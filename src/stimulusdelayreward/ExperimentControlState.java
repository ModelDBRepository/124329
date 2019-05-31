package stimulusdelayreward;
import lnsc.page.*;
import lnsc.DataSet;

/** Experiment is a sequence of trial and inter-trial. A trial is a single
 *  presentation of a CS or a US (on time-step). Inter-trials delay avg 6s
 *
 *  Trial details:
 *    - At t = 0
 *        -> Present stimulus or reward chosen randomly
 *    - At t = 1
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


public class ExperimentControlState extends ExperimentState {

    /*********************************************************************/
    //Constructors

    /** First constructor to initialize an experiment (a run) that will
     *  begin with an inter-trial at t=0.
     * @param   fixedDelay    Delay bewtween US onset and CS onset in ms.
     **/
    public ExperimentControlState(int fixedDelay) {
        super(fixedDelay);
        //adapt delays to maintain about 10 trials per minute
        m_MinInterTrialDelay = 5000;
        m_MinInterTrialDelay = 7000;
    }

    /*********************************************************************/
	//Interface implementation
    public void doAction(Action a) {

        //Increase step counters
        m_Step++;
        m_CurrentStep++;

        //If the state shift from inter-trial to trial or vice-versa
        if ((m_IsInTrial & (m_CurrentStep > 0)) ||
            (!m_IsInTrial & (m_CurrentStep == m_CurrentDelay))) {
            //Make the shift
            m_IsInTrial = !m_IsInTrial;
            m_CurrentStep = 0;
            //Select a new delay according to the new state
            if (m_IsInTrial) {
                m_CurrentDelay = 0;
                m_CurrentTrial++;
            } else {
                m_CurrentDelay = (int) (getInterTrialDelay() / m_TimePerStep);
            }
        }

        //Update signals
        m_Stimulus = 0; m_Reward = 0;
        m_CurrentTrialType = ITI;
        if (m_IsInTrial) {
            if (Math.random() < .5) {
                m_Stimulus = 1;
                m_CurrentTrialType = CS_ONLY;
            } else {
                m_Reward = 1;
                m_CurrentTrialType = US_ONLY;
            }
        }

    }

}