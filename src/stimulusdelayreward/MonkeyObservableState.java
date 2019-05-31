package stimulusdelayreward;
import lnsc.page.*;
import lnsc.DataSet;


/** This class serves as interface between complete ExperimentState and Monkey
 *  agents. Monkeys can only observed the stimulis signal and juice delivery
 *  (or tasting) signal.
 *
 *  Because this class only serves as observable state, it does not implement
 *  getStates or getObservableStates methods.
 *
 *  The class also provides the agent with a fixed list of action (a single
 *  NullAction action since no action are required here).
 *
 * @author Francois Rivest
 * @version 1.0
 */

public class MonkeyObservableState extends AbstractState {

    /*********************************************************************/
    //Private fields

    /** There are no actions in this setup. */
    protected Action[] m_Actions = new Action[] {new NoAction()};

    /** Stimulus signal. */
    protected double m_Stimulus;

    /** Reward (juice) signal. */
    protected double m_Reward;


    /*********************************************************************/
    //Constructors

    /** Construct the state that the monkey observes from an experimental state. */
    public MonkeyObservableState(ExperimentState state) {

        //Generic State properties
        m_AreActionsFixed = false;
        m_ActionCount = 1;

        //State properties
        m_Stimulus = state.m_Stimulus;
        m_Reward = state.m_Reward;

    }

    public State[] getNextStates(Action a) {
        throw new java.lang.UnsupportedOperationException("Method getStates() not implemented.");
    }

    public State[] getObservableStates() {
        throw new java.lang.UnsupportedOperationException("Method getObservableStates() not implemented.");
    }

    public Action[] getActions() {
        return m_Actions;
    }

    /*********************************************************************/
    //Properties

    /** Stimulus signal. */
    public double getStimulusSignal() {return m_Stimulus;}

    /** Reward (juice) signal. */
    public double getRewardSignal() {return m_Reward;}

    /*********************************************************************/
    //toDataSet

    public DataSet toDataSet() {
        return new DataSet(new String[] {"Simulus",
                                         "Reward"},
                           new Object[] {new Double(m_Stimulus),
                                         new Double(m_Reward)});
    }



}