package stimulusdelayreward;

import lnsc.page.*;
import lnsc.*;
import java.util.Observer;
import java.util.Observable;

/** Default collector for an experiment. Will store data from state and agents.
 *
 * @author Francois Rivest
 * @version 1.0
 */

public class DataSetCollector implements Observer {

    public DataSetCollection StateHistory;
    public DataSetCollection MonkeyHistory;

    protected int m_Counter;

    protected int m_CorrectCounter;
    public int m_FirstCorrectStep = -1;
    public boolean m_CorrectFound;

    /** Constructor. */
    public DataSetCollector() {
        StateHistory = new DataSetCollection(10000);
        MonkeyHistory = new DataSetCollection(10000);
    }

    /** Assumes state called before monkey request action and
     * monkey called only once in request action. */
    public void update(Observable o, Object arg) {
        if (((Object) o) instanceof Environment) {
            StateHistory.setDataSet(m_Counter, (DataSet) arg, true);
        } else {
            MonkeyHistory.setDataSet(m_Counter, (DataSet) arg, true);
            DataSet lstm = (DataSet) ((DataSet) arg).getData("LSTM");
            //Assumes the reward to predict properly is always the last signal.
            double[] fullerrpat = ((double[]) lstm.getData(DataNames.ERROR_PATTERNS));/***20080208*/
            double err = Math.abs(fullerrpat[fullerrpat.length-1]);/***20080208*/
            if (!m_CorrectFound) {
                if (err <= .5) { //.5
                    m_CorrectCounter++;
                    if (m_FirstCorrectStep == -1) {
                        m_FirstCorrectStep = m_Counter;
                    }
                    if (m_CorrectCounter >= 10*30) {
                        m_CorrectFound = true;
                    }
                } else {
                    m_CorrectCounter = 0;
                    m_FirstCorrectStep = -1;
                }
            }

            m_Counter++;   //State is always dispatched first
        }
    }

    /** Return the first index in DataSetCollection for "CurrentTrial" = x */
    public int getTrialIndex(int trial) {
        for (int i=0; i<m_Counter; i++)
        {
             if (((Integer) StateHistory.getData(ExperimentState.CURRENT_TRIAL, i)).intValue() == trial) {
                 return i;
             }

        }
        return -1;//not found
    }

    /** Extract a column of data for a given trial.
     * @param   trial      Trial number
     * @param   state      True if it is in the state history, false for agent history
     * @param   dataName   Keyword to extract
     * @param   start      Start offset (eg, -1, will began at the pattern previous the first trial pattern)
     * @param   stop       Stop offset (eg, +10, will stop at 10 after the first (included) trial pattern)
     */
    protected Object[] extractTrialData(int trial, boolean state, String dataName, int start, int stop)
    {
       Object[] ret = new Object[stop-start];
       int index = getTrialIndex(trial);
       if (index == -1) {return ret;}

       Object[] temp;
       if (state) {
           temp = StateHistory.getDataCollection(dataName);
       } else {
           temp = MonkeyHistory.getDataCollection(dataName);
       }

       for (int i=0; i<ret.length; i++)
       {
           ret[i] = temp[index+start+i];
       }

       return ret;

    }
}