package grsnc.binb;

import lnsc.*;
import lnsc.page.*;
import java.util.Random;

/**
 * <p>
 * Title: BG Math Model (Francois Rivest, 10 Mar 2006)<br>
 * Description: Agent based a Mathematical Model of the Basal Ganglia<br>
 * Copyright: Copyright (c) 2004<br>
 * Company: UdeM<br>
 *
 * Note: Eligibility traces are bounded between -1 and 1. (Actors still untraced)
 *
 * </p>
 *
 *
 * <P> <i>Summary</i>. In this model, the critic uses standard TD formula,
 * while the actor uses a natural gradient that gives biological three-synaptic
 * update rule. Although the critic part is not totally biologically plausible,
 * it is the same as the Suri&Schultz1999Model equations. It would be interesting
 * to find a biologically plausible equivalent formula. </P>
 *
 * <P>
 * <i>Implementation details: </i>
 * <ul>
 * <li>Si(t) is stimuli given by StateRepresentation
 * <li>r(t) is the primary reward
 * <li>Unless state is final, returnReward value is processed in next call to
 * requestAction. If it is final, it is process in episodeTerminated.
 * <li>Critic:
 * <ul>
 *     <li>Wik is weights between stimuli Si(t) and prediction of stimuli Pk(t)
 *     <li>Pk(t) = sum(Wik*Si(t)) is reward prediction k
 *     <li>P(t) = sum(Pk(t))
 *     <li>e(t) = r(t) + gamma*P(t) - P(t-1)
 *     <li>eti(t) = lambda*eti(t-1) + Si(t-1)
 *     <li>gamma = .98 (discounting factor)
 *     <li>learning rule:
 *         Wik(t) = Wik(t-1) + etac*e(t)*eti(,t)
 *     <li>initialisation:
 *         Wik(t=0) > 0
 *  </ul>
 *  <li>Actor:
 *  <ul>
 *     <li>Wij is weights between stimuli Si(t) and action Aj
 *     <li>Aj(t) = sum(Wij*Si(t)) is actor activity k
 *     <li>Aj'(t) = 1 if Aj'(t)>0 and Aj'(t) > Al'(t) for all l not j
 *     <li>learning rule:
 *         Wij(t) = Wij(t-1) + etaa*e(t)*Aj'(t-1)*Si(t-1)
 *     <li>initialisation:
 *         Wij(t=0) > 0
 * </ul>
 * </P>
 *
 * <P>
 * <i>Assumptions: </li>
 * <ul>
 * <li>newEpisode & first requestAction have the same state
 * <li>requestAction & next returnReward have the same state
 * <li>last returnReward and endEpisode have the same state
 * </lu>
 *
 * @author François Rivest
 * @version 1.1
 */

public class Rivest06 extends AbstractObservableAgent {

    /*********************************************************************/
    //Serial Version UID

    /** Serial version UID. */
    static final long serialVersionUID = 3615829507271034044L;

    /**********************************************************************/
    //Private fields

    protected Random rnd = new Random();

    /** Indicates whether or not the model agent should be in evaluation mode
     * only (no learning). */
    //protected boolean m_EvalMode;

    /** StateRepresentation converting state into real-valued vector. */
    protected StateRepresentation m_StateRep;

    /** Number of actor neurons. */
    protected int m_ActorCount;

    /** Number of critic neurons. */
    protected int m_CriticCount;

    /** Stimuli to actor weights. */
    protected double[][] m_Wa;

    /** Stimuli to critic weights. */
    protected double[][] m_Wc;

    /** Previous stimuli activty. */
    protected transient double[] m_PrevStimuli;

    /** Previous critics activity (useless). */
    protected transient double[] m_PrevCritics;

    /** Previous prediction activity. */
    protected transient double m_PrevPrediction;

    /** Previous actor activity. */
    protected transient double[] m_PrevAction;

    /** Previous eligibility trace (for critic only). */
    protected transient double[] m_PrevETraces;

    /** Reward. */
    protected transient double m_Reward;

    /** Indicate no first state yet. */
    protected transient boolean m_Reset;

    /** Discounting factor. */
    protected double m_Gamma = 0.98;

    /** Eligibility trace discount factor. */
    protected double m_Lambda = .9;

    /** Actor learning rate. */
    protected double m_Etaa = .01;

    /** Critic learning rate. */
    protected double m_Etac = .01;

    /** Initialization weight factor. */
    protected double m_InitWeightFactor = .1;

    /** Description DataSet for toDataSet (updated at the end of processContext). */
    protected transient DataSet m_Description;

    /**********************************************************************/
    //Constructors

    /** Construct an agent based on Francois Rivest May 17 BG Math Model.
     * @param    newActionCount            Number of action neurons
     * @param    newCriticCount            Number of critic neurons.
     * @param    newStateRepresentation    State representation or stimuli.
     * @param    newLearningRate           Actor & Critic learning rates.
     * @param    newInitWeightFactor       Initialization weight factor.
     */
    public Rivest06(int newActorCount, int newCriticCount,
                    StateRepresentation newStateRep,
                    double newLearningRate,
                    double newInitWeightFactor)
    {
        m_IsEvaluable = true;

        m_ActorCount = newActorCount;
        m_CriticCount = newCriticCount;
        m_StateRep = newStateRep;

        m_Etaa = newLearningRate;
        m_Etac = newLearningRate;
        m_InitWeightFactor = newInitWeightFactor;

        m_Wa = new double[m_ActorCount][m_StateRep.getOutputCount()];
        m_Wc = new double[m_CriticCount][m_StateRep.getOutputCount()];
        initWeights(m_Wa);
        initWeights(m_Wc);
    }

    /**********************************************************************/
    //Agent interface implementation

    /** Starts by filling previous stimuli, prediction and action. */
    public void newEpisode(State newState)
    {
        //Save previous activity
        m_PrevStimuli = new double[m_StateRep.getOutputCount()];
        m_PrevPrediction = 0.0;
        m_PrevAction = new double[m_ActorCount];
        m_PrevCritics = new double[m_CriticCount];
        m_PrevETraces = new double[m_StateRep.getOutputCount()];
        //Initial reward = 0
        m_Reward = 0;
        m_Reset = true;
        m_StateRep.reset();
    }

    /** Computes actors and critics activities with no-reward given at time t.*/
    public Action requestAction(State currentState) {
        int actionIndex = processContext(currentState, m_Reward);
        return currentState.getActions()[actionIndex];//Assume actions are always the same.
    }

    /** Save reward.  */
    public void returnReward(State resultState, double reward)
    {
        if (resultState.isFinal()) {
        //	int actionIndex = processContext(resultState, reward);
        } else {
            m_Reward = reward;
        }
    }

    /** Complete processContext. */
    public void endEpisode(State finalState) {
        processContext(finalState, m_Reward);
    }

    //public boolean getEvalMode() {return m_EvalMode;}

    //public void setEvalMode(boolean newEvalMode) {
    //	m_EvalMode = newEvalMode;
    //}

    //public boolean isEvaluable() {return false;}

    //public boolean isAdaptive() {return true;}

    /**********************************************************************/
    //Helper function

    /** Initialize a weight matrices with values higher then 0 (1/aORc_count).*/
    protected void initWeights(double[][] w)
    {
        double v = m_InitWeightFactor/(double)(w.length);
        for (int i=0; i<w.length; i++)
        {
            for (int j=0; j<w[0].length; j++)
            {
                w[i][j] = v;
            }
        }

    }

    /** Given a vector of values, return the index of the maximum value. If
     * there are multiple maximum, 1 is selected at random from them.
     * @param    v    Vector of values
     * @return   Index of one of the maximum values at random.
     */
    protected int findRandomMax(double[] v)
    {
        //Find max
        double max = v[0];
        for (int i=1; i<v.length; i++)
        {
            max = Math.max(max, v[i]);
        }

        //Count maxes
        int count = 0;
        for (int i=0; i<v.length; i++)
        {
            if (v[i] == max) {count++;}
        }

        //Gather max indexes
        int[] indexes = new int[count];
        int j = 0;
        for (int i=0; i<v.length; i++)
        {
            if (v[i] == max) {indexes[j] = i; j++;}
        }
        if (j != indexes.length) {
            throw new UnknownError("Maximums count mismatch!");
        }

        //Select one at random
        return indexes[(int)(Math.random()*(double)count)];
    }


    /**********************************************************************/
    //Model simulation

    protected void bound(double[] et)
    {
        for (int i=0; i<et.length; i++)
        {
            et[i] = Math.min(Math.max(et[i],-1),1);
        }
    }

    /** One time-step processing in the basal ganglia.
     * @param    s    Current state to process.
     * @param    r    Reward from previous action.
     * @return   Index of winning action node (-1 no action). */
    protected int processContext(State s, double r)
    {
        //Process stimuli (brain activity)
        double[] stimuli = m_StateRep.getRepresentation(s);

        //Process critic stimuli
        double[] critic = LinearAlgebra.multMatrixVector(m_Wc, stimuli);
        double prediction = 0.0;
        for (int k=0; k<m_CriticCount; k++)
        {
            prediction += critic[k];
        }

        //Process actor stimuli
        double[] actor = LinearAlgebra.multMatrixVector(m_Wa, stimuli);
        int maxIndex = findRandomMax(actor);
//		if (actor[maxIndex] != 0) {
            for (int j=0; j<m_ActorCount; j++)
            {
                if (j != maxIndex) {
                    actor[j] = 0.0;
                } else {
                    actor[j] = 1.0;
                }
            }
//	    } else {
//			maxIndex = -1;
//		}

        //Process effective reinforcement signal (dopamine)
        double e = r + m_Gamma * prediction - m_PrevPrediction;
        if (m_Reset) {
            e = 0; //no update
            m_Reset = false;
        }

        //Process eligibility traces for critic
        double eTraces[] = LinearAlgebra.addVectors(LinearAlgebra.multScalarVector(m_Lambda, m_PrevETraces), m_PrevStimuli);
        bound(eTraces); //*** Rivest06

        //Update critic weights using TD rule
        for (int i=0; i<stimuli.length; i++)
        {
            for (int k=0; k<m_CriticCount; k++)
            {
                m_Wc[k][i] += m_Etac * e * eTraces[i];  //*** Pan05
            }
        }

        //Update actor weights using 3-synaptic hebb rule
        for (int i=0; i<stimuli.length; i++)
        {
            for (int j = 0; j < m_ActorCount; j++) {
                m_Wa[j][i] += m_Etaa * e * m_PrevAction[j] * m_PrevStimuli[i];
            }
        }

        //If it comes from ExtendedStateRepresentation_May11,
        //then update must be called here.
        //if (m_StateRep instanceof ExtendedStateRepresentation) {
        //	((ExtendedStateRepresentation) m_StateRep).train(s, e);
        //}

        //Save previous activity
        m_PrevStimuli = stimuli;
        m_PrevPrediction = prediction;
        m_PrevAction = actor;
        m_PrevCritics = critic;
        m_PrevETraces = eTraces;

        //System.out.println("----------------------------------------------");
        //System.out.println(LinearAlgebra.toString(m_Wa));
        //System.out.println(LinearAlgebra.toString(m_Wc));

        updateDescription(e, maxIndex, new double[0][0], new double[0][0]);

        //return action selected (-1 for no action)
        return maxIndex;
    }

    /*********************************************************************/
    //toDataSet

    public final static String STIMULUS = "Stimulus";
    public final static String REWARD = "Reward";
    public final static String CRITICS = "Critics";
    public final static String PREDICTION = "Prediction";
    public final static String ACTORS = "Actors";
    public final static String ACTION = "Action";
    public final static String DOPAMINE = "Dopamine";
    public final static String CRITICS_WEIGHTS_CHANGE = "CriticsWeightsChange";
    public final static String ACTORS_WEIGHTS_CHANGE = "ActorsWeightsChange";
    public final static String CRITICS_WEIGHTS = "CriticsWeights";
    public final static String ACTORS_WEIGHTS = "ActorsWeights";

    /** Creates an updated description based on final processContext values.
     * Also set the changed bit.
     * @param   e          Dopamine level
     * @param   delta_Wc   m_Wc changes
     * @param   delta_Wa   m_Wa changes
     */
    protected void updateDescription(double e, int action, double[][] delta_Wc, double[][] delta_Wa)
    {
        m_Description = new DataSet(new String[] {STIMULUS,
                                                  REWARD,
                                                  CRITICS,
                                                  PREDICTION,
                                                  ACTORS,
                                                  ACTION,
                                                  DOPAMINE,
                                                  CRITICS_WEIGHTS_CHANGE,
                                                  ACTORS_WEIGHTS_CHANGE,
                                                  CRITICS_WEIGHTS,
                                                  ACTORS_WEIGHTS},
                                    new Object[] {m_PrevStimuli,
                                                  new Double(m_Reward),
                                                  m_PrevCritics,
                                                  new Double(m_PrevPrediction),
                                                  m_PrevAction,
                                                  new Integer(action),
                                                  new Double(e),
                                                  delta_Wc,
                                                  delta_Wa,
                                                  m_Wc,
                                                  m_Wa});
        setChanged();
        notifyObservers();
     }

     public DataSet toDataSet() {return m_Description;}

     /*********************************************************************/
      //toString method

      public String toString()
      {

          //Inherited
          String ret = super.toString() + "\n";
          ret += "Class: Rivest06\n";

          //StateRepresentation
          ret += "\tStateRepresentation = \n";
          ret += Tools.tabText(m_StateRep.toString(),2) + "\n";

          //Structure information
          ret += "\tCriticCount = " + m_CriticCount + "\n";
          ret += "\tActorCount = " + m_ActorCount + "\n";

          //Parameter
          ret += "\tInitWeightFactor = " + m_InitWeightFactor + "\n";
          ret += "\tDiscountFactor = " + m_Gamma + "\n";
          ret += "\tEligibilityTraceDiscountFactor = " + m_Lambda + "\n";
          ret += "\tCriticLearningRate = " + m_Etac + "\n";
          ret += "\tActorLearningRate = " + m_Etaa + "\n";

          //Weights
          ret += "\tCriticWeights = \n";
          ret += Tools.tabText(LinearAlgebra.toString(m_Wc),2) + "\n";
          ret += "\tActorWeights = \n";
          ret += Tools.tabText(LinearAlgebra.toString(m_Wa),2);

          //Skip transients

          //Return
          return ret;
      }

}
