package lnsc.page;
import java.util.Enumeration;

/** Optional basis for class implementing {@link State} interface. Derived
 *  classes constructor must fill the protected variables. Add necessary state
 *  information and properties. This implementation uses {@link ActionGenerator}
 *  and {@link StateGenerator} to respond to {@link State#getActions} and
 *  {@link State#getNextStates} (and for State#getActionsEnumerator) calls. When
 *  states are not observable, it uses a (@link StateGenerator} with
 *  <code>null</code> action to generate the observable states for the agents.
 *  It does not  support local Do or Undo operation (within an instance do
 *  operation) nor clone operation (by default). State and Action generators
 *  properties should be passed to to local variable (isDeterministic,
 *  areActionsFixed, ActionCount), oservable state generator should be should
 *  be deterministic (return a single state).
 *
 * @author Francois Rivest
 * @version 1.1
 */


public abstract class AbstractStateWithGen extends AbstractState {

	/*********************************************************************/
	//Private fields

	/** Use to generate the list of possible actions. */
	protected ActionGenerator m_ActionGen;

	/** Use to generate the next state in response to an action. */
	protected StateGenerator m_StateGen;

	/** Use to generate the observable states for agents. */
	protected StateGenerator m_ObsStateGen;

	/*********************************************************************/
	//Interface implementation

	public Action[] getActions() {
		return m_ActionGen.getActions(this);
	}

	public Enumeration getActionsEnumerator() {
		return m_ActionGen.getActionsEnumerator(this);
	}

	public State[] getNextStates(Action a) {
		return m_StateGen.getStates(this, a);
	}

	public State[] getObservableStates() {
		if (m_IsObservable) {
			return new State[] {this};
		} else {
			return m_ObsStateGen.getStates(this, null);
		}
	}

}