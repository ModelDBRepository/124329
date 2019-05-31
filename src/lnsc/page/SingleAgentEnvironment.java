package lnsc.page;

/** Run a single agent environement for a given number of step or until success.
 *
 * <P>The reward given to the agent after each action can be either, based on
 * the action cost (-), the resulting state value (+), or given values for
 * normal and goal-leading (an action leading to a final state) action, or any
 * addition of these options.</P>
 *
 * <P>The state should generate a single state in returns of an action (hence
 * technically have isDeterministic = true), not multiple like in a search
 * procedure. It should also return a single observable state on request
 * (wich may be the state itself) and not many (since there is a single agent.
 * </P>
 *
 * <P>Agents may requires the observable state to provide actions in a specific
 * format. But the observable state does not need to implement getNextStates,
 * or getObservableStates, nor does the State need to implement getActions or
 * getActionsEnumerator.</P>
 *
 * <P> If there are obervers, a toDataSet description of each state is sent
 * to them initially, and after the processing of every action (e.i. after
 * {@link State#getNextStates}).</P>
 *
 * @author Francois Rivest
 * @version 1.1
 */


public class SingleAgentEnvironment extends AbstractObservableEnvironment {

	/*********************************************************************/
	//Private fields

	/** Normal action reward. */
	protected double m_Reward = 0.0;

	/** Successful (goal-leading) action reward. */
	protected double m_SuccessReward = 0.0;

	/** Indicates to use action-cost as reward instead of other values. */
	protected boolean m_ActionCostReward = false;

	/** Indicates to use state-value as reward instead of other values. */
	protected boolean m_StateValueReward = false;

	/** Maximum number of steps. */
	protected int m_MaxSteps = 100000;

	/** Indicates whether or not to output dots every 1000 steps.
	 * Default is true.*/
	public boolean showDots = true;

	/*********************************************************************/
	//Constructors

	/** Constructs a server for a single agent with specific rewards.
	 * @param   newReward          Normal action reward.
	 * @param   newSuccessReward   Successful (goal-leading) action reward.
	 * @param   newMaxSteps        Maximum number of steps.
	 */
	public SingleAgentEnvironment(double newReward,
		                          double newSuccessReward,
                    			  int newMaxSteps)
	{
		m_Reward = newReward;
		m_SuccessReward = newSuccessReward;
		m_MaxSteps = newMaxSteps;
	}

	/** Constructs a server for a single agent with action or state based reward.
	 * @param   costReward         Reward -= Action.Cost (if true)
	 * @param   valueReward        Reward += ResultingState.Value (if true)
	 * @param   newMaxSteps        Maximum number of steps.
	 */
	public SingleAgentEnvironment(boolean costReward,
		                          boolean valueReward,
                     			  int newMaxSteps)
	{
		m_ActionCostReward = costReward;
		m_StateValueReward = valueReward;
		m_MaxSteps = newMaxSteps;
	}

	/** Constructs a server for a single agent specifying all reward details.
	 * @param   costReward         Reward -= Action.Cost (if true)
	 * @param   valueReward        Reward += ResultingState.Value (if true)
	 * @param   newReward          Normal action reward.
	 * @param   newSuccessReward   Successful (goal-leading) action reward.
	 * @param   newMaxSteps        Maximum number of steps.
	 */
	public SingleAgentEnvironment(boolean costReward,
		                          boolean valueReward,
								  double newReward,
								  double newSuccessReward,
								  int newMaxSteps)
	{
		m_ActionCostReward = costReward;
		m_StateValueReward = valueReward;
		m_Reward = newReward;
		m_SuccessReward = newSuccessReward;
		m_MaxSteps = newMaxSteps;
	}

	/*********************************************************************/
	//

	/** Run an agent on the task once.
	  * @param     agent     The agent to run on the task.
	  * @param     initState Optional initial state.
	  * @return    Number of step required, total action cost, total reward. */
	public double[] go(Agent agent, State initState)
	{
	   int actionCount = 0;
	   double actionCost = 0;
	   double totalReward = 0;

	   //initial state
	   State s = initState;
	   State os = s.getObservableStates()[0];
	   setChanged();
	   notifyObservers(s.toDataSet());
	   agent.newEpisode(os);

	   //until its done
	   while (!s.isFinal() & actionCount<m_MaxSteps)
	   {

		   //request action
		   Action a = agent.requestAction(os);
		   actionCount++;
		   actionCost += a.getCost();
		   s = s.getNextStates(a)[0];
		   os = s.getObservableStates()[0];
		   setChanged();
		   notifyObservers(s.toDataSet());

		   //return reward
		   double r = 0;
		   if (m_ActionCostReward) {
			   r -= a.getCost();
		   }
		   if (m_StateValueReward) {
			   r += s.getValue();
		   }
		   if (!s.isFinal()) {
			   r += m_Reward;
		   } else {
			   r += m_SuccessReward;
		   }
		   agent.returnReward(os, r);
		   totalReward += r;

		   //dots
		   if (showDots && (actionCount % 1000 == 0)) {System.out.print(".");}

	   }

	   //done
	   agent.endEpisode(os);

	   return new double[] {actionCount, actionCost, totalReward};
   }


}
