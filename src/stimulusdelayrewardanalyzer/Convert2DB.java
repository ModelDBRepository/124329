package stimulusdelayrewardanalyzer;

import java.io.*;
import java.sql.*;
import lnsc.*;
import lnsc.lstm.*;

/** Main conversion thread to convert .dsc76 files into ODBC mdb.
 * This class can work on its own thread. It reads a given directory .dsc76
 * files made using StimulusDelayReward_28Jul06_1 based code and extract a
 * specific train or test block into a .txt file.
 *
 * Dump files are renaed to :
 *      m_TargetPath + "\\" + id + "_export_" + m_BlockIndex + xstr + ".txt"
 * Dumping is done by Tools.dumpBlock().
 *
 * @author Francois Rivest
 * @version 1.0
 */


public class Convert2DB implements Runnable {

	public final static int TRAIN = 0;
	public final static int TEST = 1;
	public final static int CONTROL = -1;

	/*********************************************************************/
    //Private fields

	/** Name of the database to fill. */
	protected String m_DBName;

	/** Path where the .dsc files are. */
	protected String m_DscPath;

	/** Date of the training sesion. */
	protected Date m_Date;

	/** Model of network trained. */
	protected int m_Model;

	/** Number of .dsc76 files completely processed. */
	protected int m_NetworkCount;

	/** Number of .dsc76 files in total. */
	protected int m_AllNetworkCount;

	/** Number of blocks within current .dsc file completely processed. */
	protected int m_BlockCount;

	/** Number of all blocks within current .dsc76 files. */
	protected int m_AllBlockCount = 25;

	/** Output text string buffer. */
	protected String m_Buff = "";

	/** Conneciton to databse. */
	protected Connection m_Conn;

	/*********************************************************************/
	//Constructors

	/** Construct a Runnable class that analyse a whole directory of .dsc76
	 * files to convert them into an access database.
	 * @param   newDatabaseName   Name of the access databse to write to.
	 * @param   newDscFilesPath   Name of the directory to read .dsc files from.
	 * @param   newDate           Date of training
	 * @param   newModel          Model of networks trained
	 */
	public Convert2DB(String newDatabaseName, String newDscFilesPath,
					  Date newDate, int newModel) {
		m_DBName = newDatabaseName;
		m_DscPath = newDscFilesPath;
		m_Date = newDate;
		m_Model = newModel;
	}

	/*********************************************************************/
	//Properties

	/** Number of .dsc76 files completely processed. */
	public int getNetworkCount() {
		return m_NetworkCount;
	}

	/** Number of blocks within current .dsc file completely processed. */
	public int getBlockCount() {
		return m_BlockCount;
	}

	/** Number of .dsc76 files in total. */
	public int getAllNetworkCount() {
		return m_AllNetworkCount;
	}

	/** Number of all blocks within current .dsc76 files. */
	public int getAllBlockCount() {
		return m_AllBlockCount;
	}

	/** Returns the current text buffer and empty it. */
	public synchronized String getText() {
		String tmp = m_Buff;
		m_Buff = "";
		return tmp;
	}

	/** Add some string to the text buffer. */
	protected synchronized void dbout(String text) {
		//m_Buff += text;
                System.out.print(text);//16 may 2008
	}

	/*********************************************************************/
	//Helper function

	/** Writes a new network row in the database.
	 * Returns the highest block number if the network exist, 0 if it does not.*/
	protected int createNetwork(int netID, int model) {

		//First check if it exist
		try {
			Statement stmt = m_Conn.createStatement();
			String select = "SELECT nNetworkID FROM _Networks WHERE " +
							"nNetworkID=" + netID;
			//dbout(select + "\n");
			ResultSet res = stmt.executeQuery(select);
			if (res.next()) {
				dbout("Network " + netID + " already exists!\n");
				//Get block number
				stmt = m_Conn.createStatement();
				select = "SELECT MAX(nBlockNumber) AS m FROM _Blocks WHERE " +
							"nNetworkID=" + netID;
				//dbout(select + "\n");
				res = stmt.executeQuery(select);
				if (!res.next()) {
					return 0;
				}
				return res.getInt("m");
			}
		} catch (SQLException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
			dbout(" -Unexpected problem!-");
			return 0;
		}
		//If it does not exist create it
		try {
			Statement stmt = m_Conn.createStatement();
			String insert = "INSERT INTO _Networks " +
				"(nNetworkModelID, nNetworkID) " +
				"VALUES (" + model + ", " + netID  + ")";
			//dbout(insert + "\n");
			stmt.execute(insert); //stmt.RETURN_GENERATED_KEYS does not work in access
								  //stmt.getGeneratedKeys();
		  } catch (SQLException e) {
			  System.err.println(e.getMessage());
			  e.printStackTrace();
			  dbout(" -Unexpected problem!-");
		  }
		  return 0;
	}

	/** Writes a new block row in the database and returns its new ID. */
	protected int createBlock(int netID, int trainDelay, Date date,
							  int blockNumber, int blockType) {
		try {
			Statement stmt = m_Conn.createStatement();
			String insert = "INSERT INTO _Blocks " +
				"(nNetworkID, nBlockTrainDelay, nBlockTypeID, " +
				"nBlockNumber, dBlockDate) VALUES (" + netID + ", " +
				trainDelay + ", " + blockType + ", " + blockNumber +
				", '" + date.toString() + "')";
			//dbout(insert + "\n");
			stmt.execute(insert); //stmt.RETURN_GENERATED_KEYS does not work in access
			                      //stmt.getGeneratedKeys();
			//so I must re-extract the key, hoping it is unique
			String select = "SELECT nBlockID FROM _Blocks WHERE " +
							"nNetworkID=" + netID + " AND " +
							"nBlockTrainDelay=" + trainDelay + " AND " +
							"nBlockNumber=" + blockNumber;
			//dbout(select + "\n");
			ResultSet res = stmt.executeQuery(select);
			if (!res.next()) {
				dbout(" -Problem: No key generated for block!- ");
				return 0;
			}
			int blockUID = res.getInt("nBlockID");
			if (res.next()) {
				dbout(" -Problem: Possible block duplicate!- ");
				return 0;
			}
			return blockUID;
		} catch (SQLException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
			dbout(" -Unexpected problem!-");
			return 0;
		}
	}

	/** Writes a new trial row in the database and returns its new ID. */
	protected int createTrial(int blockUID, int trialNumber,
							  int trialDelay, int trialOnset,
							  int trialType) {
		try {
			Statement stmt = m_Conn.createStatement();
			String insert = "INSERT INTO _Trials (" +
				"nBlockID, nTrialNumber, tTrialDelay, tTrialOnset, nTrialTypeID" +
				") VALUES (" +
				blockUID + ", " + trialNumber + ", " + trialDelay + ", " +
				trialOnset + ", " + trialType + ")";
			//dbout(insert + "\n");
			stmt.execute(insert); //stmt.RETURN_GENERATED_KEYS does not work in access
								  //stmt.getGeneratedKeys();
			//so I must re-extract the key, hoping it is unique
			String select = "SELECT nTrialID FROM _Trials WHERE " +
							"nBlockID=" + blockUID + " AND " +
							"nTrialNumber=" + trialNumber;
			//dbout(select + "\n");
			ResultSet res = stmt.executeQuery(select);
			if (!res.next()) {
				dbout(" -Problem: No key generated for trial!- ");
				return 0;
			}
			int trialUID = res.getInt("nTrialID");
			if (res.next()) {
				dbout(" -Problem: Possible trial duplicate!- ");
				return 0;
			}
			return trialUID;
		} catch (SQLException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
			dbout(" -Unexpected problem!-");
			return 0;
		}
	}

	/** Dumps signal from a block. */
	protected void dumpBlock(int blockUID, DataSetCollection state, DataSetCollection monkey) {

		boolean prevIsInTrial = false;

		for (int i = 0; i < state.getDataSetCount(); i++) {

			//Extract trial/state information
			int time = ( (Integer) state.getData(Tools.STEP, i)).intValue() *
				200; //tTime: block time (ms)
			boolean isInTrial = ( (Boolean) state.getData(Tools.IS_IN_TRIAL, i)).
				booleanValue(); //bIsInTrial: is in trial
			int trialNumber = ( (Integer) state.getData(Tools.CURRENT_TRIAL, i)).
				intValue(); //nTrialNumber: trial in block
			int trialTime = ( (Integer) state.getData(Tools.CURRENT_STEP, i)).
				intValue() * 200; //tTrialTime: time since onset (ms)
			int trialOnset = time - trialTime; //tTrialOnset: time of onset )ms)
			int trialDelay = ( (Integer) state.getData(Tools.CURRENT_DELAY, i)).
				intValue() * 200; //tTrialDelay: trial delay in (ms)
			int trialType = -1; //default is unknown because older simulations did not saved it.
			if (state.hasData(Tools.CURRENT_TRIALTYPE)) {
				trialType = ( (Integer) state.getData(Tools.CURRENT_TRIALTYPE,
					i)).intValue(); //nTrialTypeID: trial type
			}

			//Save trial info on new trial here
			if (isInTrial && !prevIsInTrial) {
				//Check for last test trial switch
				if (i == state.getDataSetCount() - 1) {
					System.out.println("Skipping sample " + time +
									   "ms of trial " + trialNumber +
									   " in blockUID " + blockUID);
					return; //skip last sample
				}
				int trialUID = createTrial(blockUID, trialNumber, trialDelay,
										   trialOnset, trialType);
				if (trialUID == 0) {
					break;
				}
			}
			prevIsInTrial = isInTrial;

			//Extract signals:
			double cs = ( (Double) state.getData(Tools.STIMULUS, i)).
				doubleValue();
			double us = ( (Double) state.getData(Tools.REWARD, i)).doubleValue();
			double[] bg_stim = (double[]) monkey.getData(Tools.STIMULUS, i);
			//The standard is
			//cs, LSTM(P(us)), MB1C1, MB1C2, MB2C1, MB2C2
			//Some very old simulations had US to TD
			//cs, us, LSTM(P(us)), MB1C1, MB1C2, MB2C1, MB2C2
			//Some TDBias simulations (30nov07) have a bias to TD
			//Bias, cs, LSTM(P(us)), MB1C1, MB1C2, MB2C1, MB2C2
			//Some LTSM2Outputs simulations (14Jan08) have 2 LSTM outputsd
			//cs, LSTM(P(cs)), LSTM(P(us)), MB1C1, MB1C2, MB2C1, MB2C2
			//The common point is that LSTM->BG are the last elements!
			double lstm = bg_stim[bg_stim.length - 5];
			double lstmCS = bg_stim[bg_stim.length - 6]; //if LSTM has 2 outputs
			double p = ( (Double) monkey.getData(Tools.PREDICTION, i)).
				doubleValue();
			double da = ( (Double) monkey.getData(Tools.DOPAMINE, i)).
				doubleValue();

			//double[][] w = (double[][]) monkey.getData(Tools.CRITICS_WEIGHTS, i);        //Final value should be suffient, not sampling
			//double[][] wc = (double[][]) monkey.getData(Tools.CRITICS_WEIGHTS_CHANGE, i);//I don't have this data
			//System.out.println(time + "\tw\t" + LinearAlgebra.toString(w) + "\n");

			//Extract lstm signals
			DataSet ds = (DataSet) monkey.getData("LSTM", i);
			double[] inputGates = (double[]) ds.getData(LSTMDataNames.
				LSTM_INPUT_GATES);
			double[] forgetGates = (double[]) ds.getData(LSTMDataNames.
				LSTM_FORGET_GATES);
			double[] outputGates = (double[]) ds.getData(LSTMDataNames.
				LSTM_OUTPUT_GATES);
			double[] states = (double[]) ds.getData(LSTMDataNames.
				LSTM_INTERNAL_STATES);
			double[] acts = (double[]) ds.getData(LSTMDataNames.
												  LSTM_INTERNAL_ACTIVATIONS);
			//Changes on 15Jan08
			boolean has2outputs = ( (double[]) ds.getData(DataNames.
				OUTPUT_PATTERNS)).length == 2 ? true : false;
			double[] output = ( (double[]) ds.getData(DataNames.OUTPUT_PATTERNS));
			double[] target = new double[] {
				0, 0}
				, error = new double[] {
				0, 0};
			if (i != state.getDataSetCount() - 1) {
				ds = (DataSet) monkey.getData("LSTM", i + 1);
				target = ( (double[]) ds.getData(DataNames.TARGET_PATTERNS));
				error = ( (double[]) ds.getData(DataNames.ERROR_PATTERNS));
			}

			//
			//(bg_Stim.length == 7) (bg_Stim.length == 7)
			//	 InputPatterns OutputPatterns
			//   LSTMOutputGates LSTMForgetGates LSTMInputGates
			//   LSTMInternalActivations LSTMInternalStates
			//   SumSquaredError ErrorPatterns  TargetPatterns

			try {
				Statement stmt = m_Conn.createStatement();
				String[] signals = {
						"nBlockID",
						"tTime",
						"bIsInTrial",
						"nTrialNumber",
						"tTrialTime",
						//"tTrialOnset",
						//"tTrialDelay",
						"fCS",
						"fUS",
						"fLSTM",
						"fP",
						"fDA",
						"fLSTMInputGate1",
						"fLSTMInputGate2",
						"fLSTMForgetGate1",
						"fLSTMForgetGate2",
						"fLSTMOutputGate1",
						"fLSTMOutputGate2",
						"fLSTMState11",
						"fLSTMState12",
						"fLSTMState21",
						"fLSTMState22",
						"fLSTMAct11",
						"fLSTMAct12",
						"fLSTMAct21",
						"fLSTMAct22",
						"fLSTMOutput",
						"fLSTMTarget",
						"fLSTMError",
						"fState11",
						"fState12",
						"fState21",
						"fState22"};
				if (has2outputs) {
					signals = concatenateStringArrays(signals, new String[]
						{"fLSTMcs",
						 "fLSTMOutputcs",
						 "fLSTMTargetcs",
						 "fLSTMErrorcs"});
				}
				String[] values = {
						Integer.toString(blockUID),
						Integer.toString(time),
						isInTrial ? "1" : "0",
						Integer.toString(trialNumber),
						Integer.toString(trialTime),
						//Integer.toString(trialOnset),
						//Integer.toString(trialDelay),
						Double.toString(cs),
						Double.toString(us),
						Double.toString(lstm),
						Double.toString(p),
						Double.toString(da),
						Double.toString(inputGates[0]),
						Double.toString(inputGates[1]),
						Double.toString(forgetGates[0]),
						Double.toString(forgetGates[1]),
						Double.toString(outputGates[0]),
						Double.toString(outputGates[1]),
						Double.toString(states[0]),
						Double.toString(states[1]),
						Double.toString(states[2]),
						Double.toString(states[3]),
						Double.toString(acts[0]),
						Double.toString(acts[1]),
						Double.toString(acts[2]),
						Double.toString(acts[3]),
						Double.toString(output[output.length - 1]),
						Double.toString(target[target.length - 1]),
						Double.toString(error[error.length - 1]),
						Double.toString(bg_stim[bg_stim.length - 4]),
						Double.toString(bg_stim[bg_stim.length - 3]),
						Double.toString(bg_stim[bg_stim.length - 2]),
						Double.toString(bg_stim[bg_stim.length - 1])};
					if (has2outputs) {
						values = concatenateStringArrays(values, new String[]
							{Double.toString(lstmCS),
							 Double.toString(output[0]),
							 Double.toString(target[0]),
							 Double.toString(error[0])});
					}
					//Run query
					String insert = SQLTools.buildSQLInsert("AllSignals",
						signals, values);
					//dbout(insert + "\n");
					stmt.execute(insert);
				}
				catch (SQLException e) {
					System.err.println(e.getMessage());
					e.printStackTrace();
					dbout(" -Unexpected problem!-");
					return;
				}

			}
			//DataSet ds = (DataSet) monkey.getData("LSTM", 3);
			//String[] lst = ds.dataNamesList();
			//for (int j=0; j<lst.length; j++)
			//{
			//	dbout(" " + lst[j]);
			//}
			//dbout("\n");

		}

		/*********************************************************************/
		//Runnable interface

		/** Function called by THREAD.start(). */
		public void run() {

			//Default for the moment
			int trainDelay = 1000; //in ms
			dbout("Model " + m_Model + ", Date " + m_Date.toString() +
				  ", Delay " + trainDelay + "ms\n");

			//Open connection
			m_Conn = Tools.openConnection(m_DBName);
			if (m_Conn == null) {
				dbout("Opening " + m_DBName + " failed!\n");
				return;
			}
			else {
				dbout("Opening " + m_DBName + " succeed!\n");
			}

			//Gather files
			File[] dscList = Tools.listDataSetCollections(m_DscPath);
			if (dscList == null) {
				dbout("Opening " + m_DscPath + " failed!\n");
				return;
			}
			else {
				dbout("Searching " + m_DscPath + ": " + dscList.length +
					  " files found!\n");
			}
			m_AllNetworkCount = dscList.length;

			//For all files
			for (int i = 0; i < m_AllNetworkCount; i++) {
				//Now check if this one is kept
				int netID = Integer.parseInt(Tools.getID(dscList[i].getName()));
                                System.out.println(netID);
				/*if (!isKept(dscList[i])) {//16 may 2008
                                  	dbout(netID + " not kept!\n");
					continue;
				}*/
				//Load the file
				dbout("Loading " + netID + " ... ");
				DataSetCollection dsc;
				try {
					dsc = Tools.loadDataSetColl(dscList[i]);
					dbout("done!\n");
				}
				catch (Exception e) {
					dbout(e.toString() + " Abort!");
					break;
				}
			        m_BlockCount++;

				//I should first check if the network as already been saved,
				//but here I assume it is the first time
				int blockCount = createNetwork(netID, m_Model);

				//for every block (train and test)
				for (int j = 0; j < dsc.getDataSetCount(); j++) {

					//Check for initial test block
					if (dsc.hasData("ControlState") &&
						(dsc.getData("ControlState", j) != null)) {
						blockCount++;
						dbout("Converting control block " + j +
							  " as block " + blockCount);
						int blockUID = createBlock(netID, trainDelay, m_Date,
							blockCount, CONTROL);
						dbout(" UID:" + blockUID + "\n");
						if (blockUID != 0) {
							dumpBlock(blockUID,
									  (DataSetCollection) dsc.getData(
								"ControlState", j),
									  (DataSetCollection) dsc.getData(
								"ControlMonkey", j));
						}
						m_BlockCount++;
					}

					//Check for test block
					if (dsc.hasData("TestState") && (dsc.getData("TestState", j) != null)) {
						blockCount++;
						dbout("Converting test block " + j + " as block " +
							  blockCount);
						int blockUID = createBlock(netID, trainDelay, m_Date,
							blockCount, TEST);
						dbout(" UID:" + blockUID + "\n");
						if (blockUID != 0) {
							dumpBlock(blockUID,
									  (DataSetCollection) dsc.getData(
								"TestState", j),
									  (DataSetCollection) dsc.getData(
								"TestMonkey", j));
						}
						m_BlockCount++;
					}

					//check for train block
					if (dsc.hasData("State") && (dsc.getData("State", j) != null)) {
						blockCount++;
						dbout("Converting train block " + j + " as block " +
							  blockCount);
						int blockUID = createBlock(netID, trainDelay, m_Date,
							blockCount, TRAIN);
						dbout(" UID:" + blockUID + "\n");
						if (blockUID != 0) {
							dumpBlock(blockUID,
									  (DataSetCollection) dsc.getData("State",
								j),
									  (DataSetCollection) dsc.getData("Monkey",
								j));
						}
						m_BlockCount++;
					}
				}

				//Track progress;
				synchronized (this) {
					m_BlockCount = 0;
					m_NetworkCount++;
				}
				;

			}

			//Close connection
			try {
				m_Conn.close();
			}
			catch (SQLException e) {
				dbout("Unexpected exception when closing connection!\n");
				e.printStackTrace();
			}
		}

	/** Given a .dsc file, check in the corresponding .ds file for the
	 * kept variable.
	 */
	boolean isKept(File f)
	{
		//Get .ds file name
		String temp = f.getAbsolutePath();
		//System.out.println(temp);
		File fs = new File(temp.replaceFirst("Result", "TrainedAgent").replaceFirst("dsc", "ds"));
		//System.out.println(fs.getName());
		//Check if file exist
		if (!fs.exists()) {
			System.err.println(fs.getName() + " not found!");
			return false;
		}
		//Read kept flag and return it
		//System.out.print(fs.getName());
		try {
			DataSet dat = lnsc.Tools.loadDataSet(fs.toString());
			boolean thissuc = ( (Boolean) dat.getData("Kept")).booleanValue();
			return thissuc;
		}
		catch (Exception exc) {
			System.err.println(exc.toString());
			return false;
		}
	}

	//HELPER function

	String[] concatenateStringArrays(String[] s1, String[] s2)
	{
		String[] ret = new String[s1.length+s2.length];
		for (int i=0; i<s1.length; i++)
		{
			ret[i] = s1[i];
		}
		for (int i=0; i<s2.length; i++)
		{
			ret[s1.length+i] = s2[i];
		}
		return ret;
	}
}
