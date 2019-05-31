package lnsc.page;
import java.util.Enumeration;
import lnsc.DataSet;

/** Defines a state as generally understand in RL and other AI problems.
 *
 * @author Francois Rivest
 * @version 1.1
 */

public interface State extends Cloneable {

	/*********************************************************************/
	//Methods

	/** Returns the list of available action from the current state.
	 *  @return    The list of available action.
	 */
	public Action[] getActions();

	/** Returns an iterator that list the available actions from the current state.
	 *  @return    The actions enumerator.
	 */
	public Enumeration getActionsEnumerator();

	/** Returns the list state of observable states for the agents.
	 *  @return    The list of observable states.
	 */
	public State[] getObservableStates();

	/** Returns the list state following the current state under a given action.
	 *  @param     a     The action to apply.
	 *  @return    The list of available action.
	 */
	public State[] getNextStates(Action a);

	/** Do an action.
	 *  @param      Action to be done.
	 */
	public void doAction(Action a);

	/** Undo last action done.
	 */
	public void undoAction();

	/*********************************************************************/
	//Properties

	/** Value associated to the state.
	 *  @return      value of the sate.
	 */
	public double getValue();

	/** Indicates whether of not a state is observable by agents. (Has nothing
	 * to do with java.util.Observable!)
	 *  @return      true us the state is fully obervable by agents.
	 */
	public boolean isObservable();

	/** Indicates whether or not a state is final (generally a goal state).
	 *  @return     true if it is a final state (end of episode), false otherwise.
	 */
	public boolean isFinal();

	/** Indicates whether the state is valid or not.
	 *  @return     true if it  is valid, false otherwise.
	 */
	public boolean isValid();

	/** Probability of the state, for non-deterministic StateGenerators only.
	 *  @return       state probability.
	 */
	public double getProbability();

	/** Indicates whether or not do action is supported locally.
	 *  @return     true if the do function is implemented.
	 */
	public boolean supportsDo();

	/** Indicates whether or not undo action is supported.
	 *  @return     true if the undo function is implemented.
	 */
	public boolean supportsUndo();

	/** Indicates whether or not the state can be copied
	 *  @return     true if the clone function is implemented.
	 */
	public boolean isCloneable();

	/** Indicates whether only the resulting state is returned or whether a list
	 *  of possible next states is returned. (Does not necessarly tells whether
	 *  the environment is really deterministic or not!)
	 *  @return      true if their is only one resulting state, false otherwise.
	 */
	public boolean isDeterministic();

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

	/*********************************************************************/
	//toDataSet interface

	/** Similar to the toString method, but return state content in the form of
	 *  a DataSet. Can be null.
	 * @return      A DataSet containing a description of the State.
	 */
	public DataSet toDataSet();

	/*********************************************************************/
	//Cloeanble interface

	public Object clone();

}

