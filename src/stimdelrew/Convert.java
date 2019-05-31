package stimdelrew;

import stimulusdelayrewardanalyzer.*;
import java.sql.*;
import java.io.*;

/** Main routine to convert .dsc76 and .ds76 simulation result files into
 *  MS Access StimDelRew.mdb database.
 *
 * @author Francois Rivest
 * @version 1.0
 */

public class Convert {

  /** There must be two arguments. The first one if the path of the .dsc76 and
   * .ds76 files, and the second if the ODBC database name.
   */
  public static void main(String[] args) {

    //Params check
    if (args.length < 2) {
        System.out.println("\nThere must be 2 parameters in order: " +
                           "\n\tpath     The path where the .dsc76 and .ds76 files are located. eg: E:\\MyDataDir\\" +
                           "\n\tdbname   The ODBC database name. eg: MyDBName");
        return;
    }

  //Path check
    File[] dsclist = Tools.listDataSetCollections(args[0]);
    if (dsclist != null) {
        System.out.println(dsclist.length + " files found!");
    } else {
        System.out.println("No file to be converted!");
        return;
    }

    //DB check
    String result = Tools.testConnection(args[1]);
    if (result != null) {
        System.out.println("Cannot connect to DB, make sure it is a valid ODBC DB!");
        return;
    }

    //Convert
    Convert2DB m_C2DB = new Convert2DB(args[1], args[0], new Date(System.currentTimeMillis()), 1);
    Thread m_thConvert = new Thread(m_C2DB, "Convert2DB");
    m_thConvert.setPriority(Thread.MIN_PRIORITY);
    m_thConvert.start();


  }

}