package lnsc.page;
import lnsc.DataSet;
import java.io.Serializable;

/** Defines an agent as generally understand in RL and other AI problems.
 *
 *  <p>Agents are assumed serializable! Episode state information should be
 *  transient!</p>
 *
 * @author Francois Rivest
 * @version 1.3
 */

public interface Agent extends Serializable {

	/** Used to tell the agent that a new episode is beginning.
	 *  @param          newState            First stae of the episode.
	 */
	public void newEpisode(State newState);

	/** Used by the State space (or environment) to request an action from
	 *  the agent. In that case the agent should returns its desired action.
	 *  @param          currentState        The current state of the agent.
	 *  @return         The action to be done.
	 */
	public Action requestAction(State currentState);

	/** Used by the state space (or environement) to return the result from the
	 *  last action done thought RequestActions.
	 *  @param          resultState          Resulting state from last action.
	 *  @param          reward               Resulting reward from last action.
	 */
	public void returnReward(State resultState, double reward);

	/** Used by the state space (or environement) to tell the agent that the
	 *  episode is terminated.
	 *  @param          finalState           Final state of the episode.
	 */
	public void endEpisode(State finalState);

	/*********************************************************************/
	//Properties

	/** Sets the agent in evaluation mode, that is, no training should append.
	 *  @param  newEvalMode     true to set it into evaluation mode.
	 *                          false to set it into normal possibly adaptive mode.
	*/
	public void setEvalMode(boolean newEvalMode);

	/** Indicates whether the agent is in evaluation mode.
	 *  @return     true if it is in evaluation mode.
	 *              false if it is in normal possibly adaptive mode.
	 */
	public boolean getEvalMode();

	/** Indicates whether the agent can be placed in eval mode.
	 *  @return     true if the agent can be placed in eval mode.
	 *              false if the agent is always adapting anyway.
	 */
	public boolean isEvaluable();

	/** Indicates whether the agent is static (never adapts, no learning) or
	 *  adaptives (the agents learn).
	 *  @return     true if the agent can learns or adapt when eval mode is true,
	 *              false if the agent never adapts (i.e. always in eval mode).
	 */
	public boolean isAdaptive();

	/*********************************************************************/
	//toDataSet interface

	/** Similar to the toString method, but return state content in the form of
	 *  a DataSet. Can be null.
	 * @return      A DataSet containing a description of the State.
	 */
	public DataSet toDataSet();


}
