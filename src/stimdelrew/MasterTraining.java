package stimdelrew;

import stimulusdelayreward.*;
import lnsc.page.*;
import lnsc.DataSetCollection;
import lnsc.DataSet;
import java.util.Random;
import java.io.*;

/** Main routine training networks until there are 5 successful networks.
 *  It saves successfull networks and final training data in .ds76 and .dsc76
 *  files respectively.
 *
 * @author Francois Rivest
 * @version 1.0
 */


public class MasterTraining {

    static final int ERROR = -2;
    static final int CRASH = -1;
    static final int FAIL = 0;
    static final int LEARN = 1;
    static final int KEPT = 3;

    /** No arguments required. */
    public static void main(String[] args) {

        //Generate 30 fully successful network
        int total = 0;
        int error = 0;
        int crash = 0;
        int fail = 0;
        int learn = 0;
        int kept = 0;
        while (kept <5) {
            System.out.println("Trial " + (total+1));
            try {
                switch (run(.5, 2, true, true)) {
                    case FAIL:
                        fail++;
                        break;
                    case KEPT:
                        kept++;
                    case LEARN:
                        learn++;
                        break;
                    case ERROR:
                    default:
                        error++;
                }
            } catch(Exception e) {
                crash++;
                System.out.println("(true,true) crashes");
            }
            total++;
            System.out.println("So far " + (kept));
        }

        try {
            PrintStream out = new PrintStream(new FileOutputStream("Result.log", true));
            out.println("Total: " + total);
            out.println("Error: " + error);
            out.println("Crash: " + crash);
            out.println("Fail: " + fail);
            out.println("Learn: " + learn);
            out.println("Kept: " + kept);
            out.close();
        } catch (Exception e) {
            System.err.println("Can't write log!");
        }

        System.out.println("Total: " + total);
        System.out.println("Error: " + error);
        System.out.println("Crash: " + crash);
        System.out.println("Fail: " + fail);
        System.out.println("Learn: " + learn);
        System.out.println("Kept: " + kept);



    }

    public static int run(double lr, int uc, boolean inSquash, boolean outSquash)
    {

        double learningRate = lr;
        int unitCount = uc;

        boolean gate2gate = false;
        boolean in2out = false;

        boolean success = false;
        boolean lastSuccess = false;
        int firstSuccess = -1;
        boolean prevLastSuccess = false; //On 18Sep06 to use previous to last train block

        //Create the agent
        AbstractObservableAgent a =
            new ActorCritic_PDAETLSTM_Monkey2(
                              unitCount,unitCount,
                              inSquash, outSquash,
                              gate2gate, in2out,
                              learningRate, .1,
                              //TD: Rivest06, {no bias, cue only}->AC,
                              4, new FlexibleSignalStateRepresentation(false, true, false),
                              .8, true);//LSTM: e-trace, reset

        //Create space for all data
        DataSetCollection dataCol = new DataSetCollection(22);

        int stop = 100*10;
        int lowbound = stop-21;

        //Run multiple 4 minutes block (about 40 trials at 6s per trial)
        for (int i=0; i<=stop; i++)
        {

            //INSERT TEST BLOCK HERE
            if (i % 10 == 0) {
                //Environement
                SingleAgentEnvironment env = new SingleAgentEnvironment(0,0,10*60*1);
                //Data collector
                DataSetCollector dc = new DataSetCollector();
                env.addObserver(dc);
                a.addObserver(dc);
                //Run
                env.go(a, new ExperimentTestState(1000));//test
                //Collect data
                if (i> lowbound) {
                    dataCol.setData("TestState", i-lowbound, dc.StateHistory);
                    dataCol.setData("TestMonkey", i-lowbound, dc.MonkeyHistory);
                }
                //Clean collector
                a.deleteObserver(dc);
            }
            //TEST BLOCK END'S HERE

            //Environement
            SingleAgentEnvironment env = new SingleAgentEnvironment(0,0,10*60*2);
            //Data collector
            prevLastSuccess = lastSuccess; ////On 18Sep06: save previous train block success
            lastSuccess = false;
            DataSetCollector dc = new DataSetCollector();
            env.addObserver(dc);
            a.addObserver(dc);
            //Run
            env.go(a, new ExperimentState(1000));
            //Test for success
            if (dc.m_CorrectFound) {
                System.out.println("Learning succeed at block " + i + " step " + dc.m_FirstCorrectStep
                                   + " with alpha = " + learningRate + "and " + unitCount + "units!");
                success = true;
                lastSuccess = true;
                if (firstSuccess == -1) {
                    firstSuccess = i;
                }
            }
            //Collect data
             if (i> lowbound) {
                 dataCol.setData("State", i-lowbound, dc.StateHistory);
                 dataCol.setData("Monkey", i-lowbound, dc.MonkeyHistory);
                 dataCol.setData("LSTMSuccess", i-lowbound, new Boolean(dc.m_CorrectFound));
             }
            //Clean collector
            a.deleteObserver(dc);
            if (i%100 ==0) {System.out.println();}
        }
        //Tools.dumpV(dc);
        //Tools.dumpE(dc);
        //Tools.dumpR(dc);

        if (!success) {
            System.out.println("Learning failed(" + learningRate + "_" + unitCount
                               + "_" + inSquash + "_" + outSquash + ")!");
            return FAIL;
        } else if (prevLastSuccess) { //Save only if it remained successfull
            Random rnd = new Random(java.lang.System.currentTimeMillis());
            long id =  rnd.nextInt();
            String name = "Result" + id + ".dsc76";
            try {
                System.out.print("Saving history in " + name + " ...");
                Tools.saveDataSetCollection(name, dataCol);
                DataSet dat = new DataSet();
                dat.setData("Agent", a);
                dat.setData("Learn", new Boolean(success));
                dat.setData("Kept", new Boolean(prevLastSuccess));//On 18Sep06
                dat.setData("First", new Integer(firstSuccess));
                dat.setData("VeryLast", new Boolean(lastSuccess));//On 18Sep06
                lnsc.Tools.saveDataSet("TrainedAgent" + id + ".ds76", dat);
                System.out.println(" done!");
                //System.out.println("Not saved!");
            } catch (Exception e) {
                System.err.println(e.toString());
                return ERROR;
            }
        }
        return (prevLastSuccess ? KEPT : LEARN);//On 18Sep06
    }
}
