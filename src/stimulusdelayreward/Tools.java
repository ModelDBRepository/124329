package stimulusdelayreward;

import lnsc.*;
import java.io.*;
import grsnc.binb.*;

/** Set of tools for the environment and simulations data.
 *
 * @author Francois Rivest
 * @version 1.0
 */

public class Tools {

    /** Receives a result file name of the form 'path\\Results(params)ID.dsc76'.
    *   Creates an agent file name of the form 'path\\TrainedAgentID.dsc76'.
    *   Creates a control result filename of the form 'path\\Control(params)ID.dsc76'.
    */
    static String[] explodeNames(String filename) {
        java.util.StringTokenizer tokenizer = new java.util.StringTokenizer(filename, "()", true);

        //extract path part
        String prefix = "";
        for (int i=0; i<tokenizer.countTokens()-4; i++)
        {
            prefix += tokenizer.nextToken();
        }
        prefix = prefix.substring(0, prefix.length()-6);

        //extract param part
        String param = "";
        param += tokenizer.nextToken();
        param += tokenizer.nextToken();
        param += tokenizer.nextToken();

        //extract id.dsc76 part
        String idend = tokenizer.nextToken();

        //Generate file names
        System.out.println(prefix + "\t" + param + "\t" + idend);
        String result = prefix + "Result" + param + idend;
        String agent = prefix + "TrainedAgent" + idend;
        agent = agent.substring(0, agent.length()-3) + "76";
        String control = prefix + "Control" + param + idend;

        return new String[] {result, agent, control};
    }

    static String[] getSuccessFiles(String path) {
        //Open directory and list files
        File f = new File(path);
        if (!f.isDirectory()) {
            System.err.println("Not a directory!");
            return new String[0];
        }
        File[] fs = f.listFiles(new ExtensionFilter(new String[] {"dsc76"}));
        if (fs.length == 0) {
            System.err.println("No '.dsc76' files found!");
            return new String[0];
        }
        //open then and check the success flag
        java.util.Vector v = new java.util.Vector();
        for (int i=0; i<fs.length; i++)
        {
            System.out.print(fs[i].getName());
            try {
                DataSetCollection dsc = Tools.loadDataSet(fs[i].toString());
                System.out.print("\t" + dsc.getDataSetCount());
                String[] strList = dsc.dataNamesList();
                for (int j = 0; j < strList.length; j++) {
                    System.out.print("\t" + strList[j]);
                }
                Boolean suc = (Boolean) dsc.getData("LSTMSuccess",21);
                if (suc.booleanValue()) {
                    v.add(fs[i].toString());
                }
                System.out.print("\t" + suc);
            }
            catch (Exception exc) {
                System.err.println(exc.toString());
            }
            System.out.println();
        }
        return toStrArray(v.toArray());
    }

    static String[] toStrArray(Object[] obj) {
        String[] strArray = new String[obj.length];
        for (int i=0; i<obj.length; i++) {
            strArray[i] = (String) obj[i];
        }
        return strArray;
    }

    /** Loads a data set (saved using {@link #saveDataSet}) from a file.
     *  @param      fileName                The name of the file.
     *  @return     The data set read.
     */
    public static DataSetCollection loadDataSet(String fileName) throws IOException, ClassNotFoundException
    {
        FileInputStream fileIn = new FileInputStream(fileName);
        ObjectInputStream in = new ObjectInputStream(fileIn);
        return (DataSetCollection) in.readObject();
    }

    /** Takes a DataSetCollector and dump V values for trials
     *      1, 11, 21, 31, 41, 51, 101 & 151 & 201
     */
    public static void dumpV(DataSetCollector dsc)
    {
        //Get alignment
        int[] t = {
            -200, -100, 0, 100, 200, 300, 400, 500, 600, 700, 800, 900, 1000,
            1100, 1200, 1300, 1400, 1500, 1600, 1700};

        //Get V
        Object[][] V = new Object[9][];
        V[0] = dsc.extractTrialData(1, false, Rivest06.PREDICTION, -2, 18);
        V[1] = dsc.extractTrialData(11, false, Rivest06.PREDICTION, -2, 18);
        V[2] = dsc.extractTrialData(21, false, Rivest06.PREDICTION, -2, 18);
        V[3] = dsc.extractTrialData(31, false, Rivest06.PREDICTION, -2, 18);
        V[4] = dsc.extractTrialData(41, false, Rivest06.PREDICTION, -2, 18);
        V[5] = dsc.extractTrialData(51, false, Rivest06.PREDICTION, -2, 18);
        V[6] = dsc.extractTrialData(101, false, Rivest06.PREDICTION, -2, 18);
        V[7] = dsc.extractTrialData(151, false, Rivest06.PREDICTION, -2, 18);
        V[8] = dsc.extractTrialData(201, false, Rivest06.PREDICTION, -2, 18);

        //Dump
        System.out.println("=================================================");
        System.out.println("Dumping V ...");
        System.out.println("\t \t'1 \t'11 \t'21 \t'31 \t'41 \t'51 \t'101 \t'151 \t'201");
        for (int i=0; i<t.length; i++)
        {
            System.out.print("\t" + t[i]);
            for (int j=0; j<V.length; j++)
            {
                System.out.print("\t" + ((Double) V[j][i]).toString());
            }
            System.out.println();
        }
        System.out.println("... done!");
        System.out.println("=================================================");

     }

    /** Takes a DataSetCollector and dump V values for trials
     *      1, 11, 21, 31, 41, 51, 101 & 151 & 201
     */
    public static void dumpE(DataSetCollector dsc)
    {
        //Get alignment
        int[] t = {
            -200, -100, 0, 100, 200, 300, 400, 500, 600, 700, 800, 900, 1000,
            1100, 1200, 1300, 1400, 1500, 1600, 1700};

        //Get V
        Object[][] e = new Object[9][];
        e[0] = dsc.extractTrialData(1, false, Rivest06.DOPAMINE, -2, 18);
        e[1] = dsc.extractTrialData(11, false, Rivest06.DOPAMINE, -2, 18);
        e[2] = dsc.extractTrialData(21, false, Rivest06.DOPAMINE, -2, 18);
        e[3] = dsc.extractTrialData(31, false, Rivest06.DOPAMINE, -2, 18);
        e[4] = dsc.extractTrialData(41, false, Rivest06.DOPAMINE, -2, 18);
        e[5] = dsc.extractTrialData(51, false, Rivest06.DOPAMINE, -2, 18);
        e[6] = dsc.extractTrialData(101, false, Rivest06.DOPAMINE, -2, 18);
        e[7] = dsc.extractTrialData(151, false, Rivest06.DOPAMINE, -2, 18);
        e[8] = dsc.extractTrialData(201, false, Rivest06.DOPAMINE, -2, 18);

        //Dump
        System.out.println("=================================================");
        System.out.println("Dumping e ...");
        System.out.println("\t \t'1 \t'11 \t'21 \t'31 \t'41 \t'51 \t'101 \t'151 \t'201");
        for (int i=0; i<t.length; i++)
        {
            System.out.print("\t" + t[i]);
            for (int j=0; j<e.length; j++)
            {
                System.out.print("\t" + ((Double) e[j][i]).toString());
            }
            System.out.println();
        }
        System.out.println("... done!");
        System.out.println("=================================================");
    }

    /** Takes a DataSetCollector and dump R values for trials
     *      1, 11, 21, 31, 41, 51, 101 & 151 & 201
     */
    public static void dumpR(DataSetCollector dsc)
    {
        //Get alignment
        int[] t = {
            -200, -100, 0, 100, 200, 300, 400, 500, 600, 700, 800, 900, 1000,
            1100, 1200, 1300, 1400, 1500, 1600, 1700};

        //Get r
        Object[][] r = new Object[9][];
        r[0] = dsc.extractTrialData(1, false, Rivest06.REWARD, -2, 18);
        r[1] = dsc.extractTrialData(11, false, Rivest06.REWARD, -2, 18);
        r[2] = dsc.extractTrialData(21, false, Rivest06.REWARD, -2, 18);
        r[3] = dsc.extractTrialData(31, false, Rivest06.REWARD, -2, 18);
        r[4] = dsc.extractTrialData(41, false, Rivest06.REWARD, -2, 18);
        r[5] = dsc.extractTrialData(51, false, Rivest06.REWARD, -2, 18);
        r[6] = dsc.extractTrialData(101, false, Rivest06.REWARD, -2, 18);
        r[7] = dsc.extractTrialData(151, false, Rivest06.REWARD, -2, 18);
        r[8] = dsc.extractTrialData(201, false, Rivest06.REWARD, -2, 18);

        //Dump
        System.out.println("=================================================");
        System.out.println("Dumping r ...");
        System.out.println("\t \t'1 \t'11 \t'21 \t'31 \t'41 \t'51 \t'101 \t'151 \t'201");
        for (int i=0; i<t.length; i++)
        {
            System.out.print("\t" + t[i]);
            for (int j=0; j<r.length; j++)
            {
                System.out.print("\t" + ((Double) r[j][i]).toString());
            }
            System.out.println();
        }
        System.out.println("... done!");
        System.out.println("=================================================");

     }

     public static void saveDataSetCollection(String fileName, DataSetCollection dataSet) throws IOException
     {
         FileOutputStream fileOut = new FileOutputStream(fileName);
         ObjectOutputStream out = new ObjectOutputStream(fileOut);
         out.writeObject(dataSet);
     }


}