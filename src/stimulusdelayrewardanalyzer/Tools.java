package stimulusdelayrewardanalyzer;

import lnsc.*;
import java.io.*;
import lnsc.lstm.*;
import java.sql.*;

/** Set of tools for the environment and simulations data.
 *
 * @author Francois Rivest
 * @version 1.0
 */

public class Tools {


	/** Try to establshed a connection and close it.
	 * @param dbName  ODBC connection name
	 * @return Error message string or null on success.
	 */
	public static String testConnection(String dbName) {

		//Load driver
		try {
			Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
		} catch (ClassNotFoundException e) {
			return e.toString();
		}

		//Open connection
		Connection conn;
		try {
			conn = DriverManager.getConnection("jdbc:odbc:" + dbName, "Admin", "");
		} catch (SQLException e) {
			return e.toString();
		}

		//Close connection
		try {
			conn.close();
		} catch (SQLException e) {
			return e.toString();
		}

		//Success
		return null;
	}

	/** Establsihed a connection and close it.
	 * @param dbName  ODBC connection name
	 * @return Connection or null if it does not work.
	 */
	public static Connection openConnection(String dbName) {

		//Load driver
		try {
			Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
		} catch (ClassNotFoundException e) {
			System.err.println(e.toString());
			return null;
		}

		//Open connection
		Connection conn;
		try {
			conn = DriverManager.getConnection("jdbc:odbc:" + dbName, "Admin", "");
		} catch (SQLException e) {
			System.err.println(e.toString());
			return null;
		}

		//Success
		return conn;
	}

	/** Lists all the data set collection (builx x76, .dsc76 files) in a directory.
	 * @param  path   Path of the directory to open
	 * @return An array of .dsc76 Files.
	 */
	public static File[] listDataSetCollections(String path)
	{
		//Open directory and list files
		File f = new File(path);
		if (!f.isDirectory()) {
			System.err.println("Not a directory!");
			return null;
		}
		File[] fs = f.listFiles(new ExtensionFilter(new String[] {"dsc76"}));
		if (fs.length == 0) {
			System.err.println("No '.dsc76' files found!");
			return new File[0];
		}
		return fs;
	}

	/** Extract the network ID from a "Resultid.dsc76" filename string or
	 * from a "xxxTrainedAgentid.dsc76" filename string.
	 * @param fileName A .dsc76 file name of the form ...tid.dsc76
	 * @retun The network id
	 */
	public static String getID(String fileName) {
		fileName = fileName.substring(0,fileName.length()-6);
		fileName = fileName.substring(fileName.lastIndexOf('t')+1);
		return fileName;
	}

	/** Extract the network retrained delay  from a "delay_TrainedAgentid.dsc76"
	 * filename string.
	 * @param fileName A .dsc76 file name of the form delay_TrainedAgentid.dsc76
	 * @retun The delay
	 */
	public static String getDelay(String fileName) {
		fileName = fileName.substring(fileName.lastIndexOf('\\')+1,
									  fileName.lastIndexOf('_'));
		return fileName;
	}

	/** Loads a data set (saved using {@link #saveDataSet}) from a file.
   *  @param      fileName                The name of the file.
   *  @return     The data set read.
   */
  public static DataSetCollection loadDataSetColl(String fileName) throws IOException, ClassNotFoundException
  {
	  FileInputStream fileIn = new FileInputStream(fileName);
	  ObjectInputStream in = new ObjectInputStream(fileIn);
	  DataSetCollection dsc = (DataSetCollection) in.readObject();
	  in.close();
	  fileIn.close();
	  return dsc;
  }

  /** Loads a data set (saved using {@link #saveDataSet}) from a file.
   *  @param      fileName                The name of the file.
   *  @return     The data set read.
   */
  public static DataSetCollection loadDataSetColl(File fileName) throws IOException, ClassNotFoundException
  {
	  FileInputStream fileIn = new FileInputStream(fileName);
	  ObjectInputStream in = new ObjectInputStream(fileIn);
	  DataSetCollection dsc = (DataSetCollection) in.readObject();
	  in.close();
	  fileIn.close();
	  return dsc;
  }

    public static void dumpBlock(DataSetCollection state, DataSetCollection monkey, PrintStream out)
    {
		out.println("Stimulus\tReward\tCritic\tDA\tLSTM\tPredErr" +
					"\tMB1Cell1\tMB1Cell2\tMB1In\tMB1fgt\tMB1out\tMB1Act1\tMB1Act2" +
					"\tMB2Cell1\tMB2Cell2\tMB2In\tMB2fgt\tMB2out\tMB2Act1\tMB2Act2");

	    for (int i=0; i<state.getDataSetCount(); i++)
	    {
			//out.print(state.getData(CURRENT_STEP,i));
			out.print(state.getData(STIMULUS,i));
			out.print("\t" + state.getData(REWARD,i));
			out.print("\t" + ((double[])monkey.getData(CRITICS,i))[0]);
			out.print("\t" + monkey.getData(DOPAMINE,i));
			DataSet lstm = (DataSet) monkey.getData("LSTM", i);
			out.print("\t" + ((double[])lstm.getData(DataNames.OUTPUT_PATTERNS))[0]);
			out.print("\t" + ((Double)lstm.getData(DataNames.SUM_SQUARED_ERROR)).doubleValue());
			for (int k=0; k<2; k++)
			{
				for (int l=0; l<2; l++)
				{
					out.print("\t" + ((double[])lstm.getData(LSTMDataNames.LSTM_INTERNAL_STATES))[k*2+l]);
				}
				out.print("\t" + ((double[])lstm.getData(LSTMDataNames.LSTM_INPUT_GATES))[k]);
				out.print("\t" + ((double[])lstm.getData(LSTMDataNames.LSTM_FORGET_GATES))[k]);
				out.print("\t" + ((double[])lstm.getData(LSTMDataNames.LSTM_OUTPUT_GATES))[k]);
				for (int l=0; l<2; l++)
				{
					out.print("\t" + ((double[])lstm.getData(LSTMDataNames.LSTM_INTERNAL_ACTIVATIONS))[k*2+l]);
				}

			}


			//Boolean b  = (Boolean) state.getData(IS_IN_TRIAL,i);
			//if (b.booleanValue()) {
			//	Boolean type = (Boolean) state.getData(TRIAL_TYPE,i);
			//	System.out.print((type.booleanValue() ? "\tLong" : "\tShort"));
			//} else {
			//	System.out.print("\tInter");
			//}

			out.println();
	    }


    }

	//From ExperimentState
	public final static String STEP = "Step";
	public final static String STIMULUS = "Stimulus";
	public final static String REWARD = "Reward";
	public final static String CURRENT_STEP = "CurrentStep";
	public final static String CURRENT_DELAY = "CurrentDelay";
	public final static String IS_IN_TRIAL = "IsInTrial";
	public final static String CURRENT_TRIAL = "CurrentTrial";
	public final static String CURRENT_TRIALTYPE = "CurrentTrialType";

	//From Schultz97
	//public final static String STIMULUS = "Stimulus";
	//public final static String REWARD = "Reward";
	public final static String CRITICS = "Critics";
	public final static String PREDICTION = "Prediction";
	public final static String ACTORS = "Actors";
	public final static String DOPAMINE = "Dopamine";
	public final static String CRITICS_WEIGHTS_CHANGE = "CriticsWeightsChange";
	public final static String ACTORS_WEIGHTS_CHANGE = "ActorsWeightsChange";
	public final static String CRITICS_WEIGHTS = "CriticsWeights";
	public final static String ACTORS_WEIGHTS = "ActorsWeights";

}