package stimulusdelayreward;

import lnsc.page.*;
import lnsc.lstm.*;
import lnsc.*;
import grsnc.binb.*;

/**
 * This is the basic monkey using the full model.
 *
 * It uses and ActorCritic model of the basal ganglia and it uses a
 * eligibility traces driven version of LSTM (ETLSTM1) as frontal cortex.
 * Both system runs in parallel and basal ganglia receives input from LSTM
 * output at previous time step. LSTM are updates on their next input. LSTM
 * are trained to predict their next inputs.
 *
 * DA signal from BG is used to modulate the LSTM learning rate.
 *
 *
 * @author Francois Rivest
 * @version 1.0
 */


public class ActorCritic_PDAETLSTM_Monkey2 extends AbstractObservableAgent {

    /*********************************************************************/
    //Serial Version UID

    /** Serial version UID. */
    static final long serialVersionUID = 8854214947734172413L;

    /*********************************************************************/
    //Private fields (current state)

    /** AC model. */
    protected Agent m_ACMModel;

    /** LSTM network model. */
    protected ETLSTMNetwork1 m_LSTMNet;

    /** LSTM trainer. */
    protected OnlineSPMSELearning m_Trainer;

    /** LSTM state representation. */
    protected StateRepresentation m_LSTMStateRep;

    /** AC state representation. */
    protected StateRepresentation m_ACMStateRep;

    /** AC state representation with previous LSTM. */
    protected StateRepresentation m_ACMExtendedStateRep;

    /** LSTM total outputs count. */
    protected int m_LSTMCount;

    /** Base LSTM learning rate. */  /***DA2***/
    protected double m_LSTMlr;/***DA2***/

    /** Last LSTM output, to use as input ot BG. */
    protected transient double[] m_PrevLSTM;

    /** Latest monkeys state. */
    protected transient DataSet m_LatestState = null;

    static private String[] m_RecordList = new String[] {
        LSTMDataNames.INPUT_PATTERNS,
        LSTMDataNames.OUTPUT_PATTERNS,
        LSTMDataNames.TARGET_PATTERNS,
        LSTMDataNames.ERROR_PATTERNS,
        LSTMDataNames.SUM_SQUARED_ERROR,
        LSTMDataNames.LSTM_INTERNAL_STATES,
        LSTMDataNames.LSTM_INTERNAL_ACTIVATIONS,
        LSTMDataNames.LSTM_INPUT_GATES,
        LSTMDataNames.LSTM_FORGET_GATES,
        LSTMDataNames.LSTM_OUTPUT_GATES
    };

    /*********************************************************************/
    //Constructors
    public ActorCritic_PDAETLSTM_Monkey2(int blockCount, int cellPerBlock,
                                         boolean inSquash, boolean outSquash,
                                         boolean gate2gate, boolean in2out,
                                         double LSTMlr, double ACMlr,
                                         int ACmodel,  StateRepresentation ACStateRep,
                                         double lambda, boolean oppSignResetTraces) {

        ETLSTMFactory1 fact = new ETLSTMFactory1(
            2, blockCount, cellPerBlock, inSquash, outSquash, 1, new LogisticUnit(),
            gate2gate, true, in2out, false, 1, //gate2gate, bias2output, input2output, gate2output, outputfactor
            lambda, oppSignResetTraces); //lambda, oppsignresettraces
        m_LSTMNet = (ETLSTMNetwork1) fact.createUnit();
        m_Trainer = new OnlineSPMSELearning(m_LSTMNet, LSTMlr, 1);
        //m_LSTMCount = blockCount*(3+2*cellPerBlock) + m_LSTMNet.getOutputCount(); /***10Mar06***/
        m_LSTMCount = blockCount*cellPerBlock + m_LSTMNet.getOutputCount(); /***10Mar06***/
        m_LSTMlr = LSTMlr; /***DA2***/

        m_LSTMStateRep = new TwoSignalStateRepresentation();
        m_ACMStateRep  = ACStateRep;//20080208
        m_ACMExtendedStateRep = new OfflineStateRepresentation(m_ACMStateRep.getOutputCount()+m_LSTMCount); /***10Mar06***/

        /*if (ACmodel == 1) {
            m_ACMModel = new Rivest05(1, 1, m_ACMExtendedStateRep, ACMlr, .1);
        } else if (ACmodel == 2) {
            m_ACMModel = new Schultz97(1, 1, m_ACMExtendedStateRep, ACMlr, .1);
        } else if (ACmodel == 3) {
           m_ACMModel = new Pan05(1, 1, m_ACMExtendedStateRep, ACMlr, .1);
       } else*/ if (ACmodel == 4) {
           m_ACMModel = new Rivest06(1, 1, m_ACMExtendedStateRep, ACMlr, .1);
       } else {
           throw new RuntimeException("Unknown model!");
       }

    }

    /*********************************************************************/
    //Interface implementation

    public void newEpisode(State newState) {
        m_Trainer.reset();
        m_ACMModel.newEpisode(newState);
        m_PrevLSTM = new double[m_LSTMCount];/***10Mar06***/
        //This assumes stateless representations
    }

    public void returnReward(State resultState, double reward) {
        //useless in this framework
    }

    public Action requestAction(State currentState) {

        //------------------------------------
        //The first step is to process the ACM

        //--Create ACM representation
        double[] acm_input = LinearAlgebra.concatenateVectors(m_ACMStateRep.getRepresentation(currentState), m_PrevLSTM);
        ((OfflineStateRepresentation) m_ACMExtendedStateRep).setRep(acm_input);

        //--Process ACM
        m_ACMModel.returnReward(currentState, ((MonkeyObservableState)currentState).getRewardSignal());
        Action a = m_ACMModel.requestAction(currentState);

        //--Collect data
        m_LatestState = m_ACMModel.toDataSet();
        double da = ((Double) m_LatestState.getData(Rivest06.DOPAMINE)).doubleValue();

        //--------------------------------------
        //The second step is to process the LSTM

        //--Create ACM representation
        double[] lstm_input = m_LSTMStateRep.getRepresentation(currentState);
        m_Trainer.setLearningRate(m_LSTMlr*(1+Math.abs(da)));        /***DA2***/

        //--Process LSTM model
        DataSet lstm_data = m_Trainer.train(lstm_input, m_RecordList);

        //--Collect data
        m_LatestState.setData("LSTM", lstm_data);
        m_PrevLSTM = (double[]) lstm_data.getData(DataNames.OUTPUT_PATTERNS);
        /***10Mar06***/
        //m_PrevLSTM = LinearAlgebra.concatenateVectors(m_PrevLSTM, (double[]) lstm_data.getData(LSTMDataNames.LSTM_INTERNAL_ACTIVATIONS));
        m_PrevLSTM = LinearAlgebra.concatenateVectors(m_PrevLSTM, (double[]) lstm_data.getData(LSTMDataNames.LSTM_INTERNAL_STATES));
        //m_PrevLSTM = LinearAlgebra.concatenateVectors(m_PrevLSTM, (double[]) lstm_data.getData(LSTMDataNames.LSTM_INPUT_GATES));
        //m_PrevLSTM = LinearAlgebra.concatenateVectors(m_PrevLSTM, (double[]) lstm_data.getData(LSTMDataNames.LSTM_FORGET_GATES));
        //m_PrevLSTM = LinearAlgebra.concatenateVectors(m_PrevLSTM, (double[]) lstm_data.getData(LSTMDataNames.LSTM_OUTPUT_GATES));
        m_PrevLSTM = bound(m_PrevLSTM);
        /***10Mar06***/

        //---------------------------
        //The third step notification

        //--Notify obervers
        setChanged();
        notifyObservers();

        //--Return null action
        return a;
   }

    public void endEpisode(State finalState) {

        //------------------------------------
        //The first step is to process the ACM

        //--Create ACM representation
        double[] acm_input = LinearAlgebra.concatenateVectors(m_ACMStateRep.getRepresentation(finalState), m_PrevLSTM);
        ((OfflineStateRepresentation) m_ACMExtendedStateRep).setRep(acm_input);

        //--Process ACM
        m_ACMModel.returnReward(finalState, ((MonkeyObservableState)finalState).getRewardSignal());
        Action a = m_ACMModel.requestAction(finalState);

        //--Collect data
        m_LatestState = m_ACMModel.toDataSet();
        double da = ((Double) m_LatestState.getData(Rivest06.DOPAMINE)).doubleValue();

        //--------------------------------------
        //The second step is to process the LSTM

        //--Create ACM representation
        double[] lstm_input = m_LSTMStateRep.getRepresentation(finalState);
        m_Trainer.setLearningRate(m_LSTMlr*(1+Math.abs(da)));        /***DA2***/

        //--Process LSTM model
        DataSet lstm_data = m_Trainer.train(lstm_input, m_RecordList);

        //--Collect data
        m_LatestState.setData("LSTM", lstm_data);
        m_PrevLSTM = (double[]) lstm_data.getData(DataNames.OUTPUT_PATTERNS);
        /***10Mar06***/
        //m_PrevLSTM = LinearAlgebra.concatenateVectors(m_PrevLSTM, (double[]) lstm_data.getData(LSTMDataNames.LSTM_INTERNAL_ACTIVATIONS));
        m_PrevLSTM = LinearAlgebra.concatenateVectors(m_PrevLSTM, (double[]) lstm_data.getData(LSTMDataNames.LSTM_INTERNAL_STATES));
        //m_PrevLSTM = LinearAlgebra.concatenateVectors(m_PrevLSTM, (double[]) lstm_data.getData(LSTMDataNames.LSTM_INPUT_GATES));
        //m_PrevLSTM = LinearAlgebra.concatenateVectors(m_PrevLSTM, (double[]) lstm_data.getData(LSTMDataNames.LSTM_FORGET_GATES));
        //m_PrevLSTM = LinearAlgebra.concatenateVectors(m_PrevLSTM, (double[]) lstm_data.getData(LSTMDataNames.LSTM_OUTPUT_GATES));
        m_PrevLSTM = bound(m_PrevLSTM);
        /***10Mar06***/

        //---------------------------
        //The third step notification

        //--Notify obervers
        setChanged();
        notifyObservers();

    }

    /*********************************************************************/
    //toDataSet

     public DataSet toDataSet() {
         return m_LatestState;
     }

     /*********************************************************************/
     //toString

      public String toString()
      {
          return m_ACMModel.toString() + "\n" + m_LSTMNet.toString();
      }

      /*********************************************************************/
      //Helper

      protected double[] bound(double[] p)
      {
          for (int i=0; i<p.length; i++)
          {
              p[i] = bound(p[i]);
          }
          return p;
      }

      protected double bound(double p)
      {
          double ubound = 1;
          double lbound = 0;
          return Math.max(Math.min(p,ubound),lbound);
      }

}
