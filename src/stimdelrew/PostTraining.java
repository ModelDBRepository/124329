package stimdelrew;

import stimulusdelayreward.*;
import java.io.*;
import lnsc.page.*;
import lnsc.*;

/** Main routine to test trained networks on a control block (CS only or US only).
 *  It saves successfull networks and data in .ds76 and .dsc76 files respectively
 *  (beginning with "StopTest_").
 *
 * @author Francois Rivest
 * @version 1.0
 */


public class PostTraining {

    /** The first argument must be the directory where to find .ds76 files of
     * trained networks to be tested. */
    public static void main(String[] args) {

      //First args must be directory where to find .ds76 files of trained nets

            //List files
            File[] files = getNetworksList(
                args[0]);

            if (files == null) {
                return;
            }

            for (int z = 0; z < files.length; z++) {

                System.out.print(files[z].getName() + "\t");

                    //Load agent
                    String a_name = files[z].getName();
                    System.out.println("Loading " + a_name);
                    AbstractObservableAgent a = loadAgent(files[z]);
                    //Train agent
                    if (a != null) {
                        testAgent(a, a_name, 1);//single block
                        System.out.println("\n\n");
                    }

                System.out.println();
                //System.out.println(LinearAlgebra.toString(blocks));
            }


    }



    static void testAgent(AbstractObservableAgent a, String a_name, int stop) {

        System.out.println("Testing " + a_name);

        //Create space for all data
        DataSetCollection dataCol = new DataSetCollection(5);//22


        //Run multiple 4 minutes block (about 40 trials at 6s per trial)
        for (int i = 0; i < stop; i++) {

            //Environement
            SingleAgentEnvironment env = new SingleAgentEnvironment(0,0,10*60*2);
            //Data collector
            DataSetCollector dc = new DataSetCollector();
            env.addObserver(dc);
            a.addObserver(dc);
            //Run
            env.go(a, new ExperimentControlState(1000));

            //Collect data
            //if (i > lowbound) {
                dataCol.setData("ControlState", 25+i, dc.StateHistory);
                dataCol.setData("ControlMonkey", 25+i, dc.MonkeyHistory);
            //}
            //Clean collector
            a.deleteObserver(dc);
            if (i % 100 == 0) {
                System.out.println();
            }
        }
        //Tools.dumpV(dc);
        //Tools.dumpE(dc);
        //Tools.dumpR(dc);

            String name = "StopTest_" + a_name;
            try {
                System.out.print("Saving history in " + name + ".dsc76 ...");
                stimulusdelayreward.Tools.saveDataSetCollection(name.replaceAll(".ds76",".dsc76"),
                    dataCol);
                DataSet dat = new DataSet();
                dat.setData("Agent", a);
                dat.setData("BlockCount", new Integer(stop));
                dat.setData("BlockNumber", new Integer(25));
                lnsc.Tools.saveDataSet(name, dat);
                System.out.println(" done!");
                //System.out.println("Not saved!");
            }
            catch (Exception e) {
                System.err.println(e.toString());
                System.err.println("Can't save agent " + name);
            }
    }



    static AbstractObservableAgent loadAgent(File filename) {
        try {
            DataSet dat = lnsc.Tools.loadDataSet(filename.getAbsolutePath());
            AbstractObservableAgent a = (AbstractObservableAgent) dat.getData("Agent");
            return a;
        } catch (Exception e) {
            System.err.println(e.toString());
            System.err.println("Can't load agent " + filename.getName());
        }
        return null;
    }

    static File[] getNetworksList(String path) {

        //Open directory and list files
        File f = new File(path);
        if (!f.isDirectory()) {
            System.err.println("Not a directory!");
            return null;
        }
        //List files
        File[] fs = f.listFiles(new ExtensionFilter(new String[] {"ds76"}));
        if (fs.length == 0) {
            System.err.println("No '.ds76' files found!");
            return null;
        }
        //Return
        //open then and check the success flag
        return fs;
    }
}
