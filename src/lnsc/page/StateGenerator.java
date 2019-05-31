package lnsc.page;

/** Generates next (or observable) states.
 *
 * @author Francois Rivest
 * @version 1.1
 */

public interface StateGenerator {


	/** Returns the list of possibly resulting state or the resulting state.
	 *  Or, on <code>null</code> action, in an partially observable environment,
	 * the (list) of observable states for agent(s).
	 *  @return    The (list) of (possibly) resulting/oberved state.
	 */
	public State[] getStates(State s, Action a);

	/** Indicates whether only the resulting state is returned or whether a list
	 *  of possible next states is returned. (Does not necessarly tells whether
	 *  the environment is really deterministic or not!)
	 *  @return      true if their is only one resulting state, false otherwise.
	 */
	public boolean isDeterministic();

}