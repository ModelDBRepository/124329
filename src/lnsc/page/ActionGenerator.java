package lnsc.page;
import java.util.Enumeration;

/** Generates list of possible actions.
 *
 * @author Francois Rivest
 * @version 1.1
 */


public interface ActionGenerator {

	/** Returns the list of available action from the current state.
	 *  @return    The list of available action.
	 */
	public Action[] getActions(State s);

	/** Returns an iterator that list the available actions from the current state.
	 *  @return    The actions enumerator.
	 */
	public Enumeration getActionsEnumerator(State s);

	/** Indicates whether the list of actions is fixed (always the same) or not.
	 * @return        true if the list of action is independent of the state,
	 *                false otherwise
	 */
	public boolean areActionsFixed();

	/** Indicates how many actions it generates. For fixed it should return the
	 *  number of actions, for unfixed, it should return -1.
	 *  @return        Number of actions.
	 */
	public int getActionCount();


}