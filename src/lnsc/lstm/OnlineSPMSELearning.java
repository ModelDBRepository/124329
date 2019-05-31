package lnsc.lstm;

import java.io.Serializable;
import lnsc.pmvf.*;
import lnsc.unsup.*;
import lnsc.*;


/** Generic Predictive Minimized Sum-squared-error online learning procedure.
 *  It should be upgraded to use a generic Optimizer and to be exported into
 *  some other package. At the limit, it could be generalized by having a
 *  FunctionalUnit to optimize.  (It minimizes half-MSE.) This one can predict
 *  a single of its input.
 *
 *  Note that in the data set, input/output and network data are current pattern
 *  data. Target, error, gradients, param, and everything related to learning
 *  are previous pattern data. Since the goal of the net is to predict the next
 *  input, the correction can only be done on the next call to train, prior to
 *  procesing the new input pattern.
 *
 *  Note, if the function is not stateless, it should not process data between
 *  train calls, unless a reset is called.
 *
 *  The network must have a single output.
 *
 * @author Francois Rivest
 * @version 1.0
 */


public class OnlineSPMSELearning implements OnlineUnsupervisedLearning, Serializable {

    /*********************************************************************/
    //Serial Version UID

    /** Serial version UID. */
    static final long serialVersionUID = -8476691339663537022L;

    /*********************************************************************/
    //Private fields

    /** Network to be trained. */
    protected FunctionalUnit2 m_Func;

    /** Learning rate. */
    protected double m_Alpha;

    /** Input index. */
    protected int m_Index;

    /*********************************************************************/
    //Private fields (state)

    /** Gradient at t-1. */
    protected transient double[][] m_PreviousGradient;

    /** Output at t-1. */
    protected transient double[] m_PreviousOutput;

    /*********************************************************************/
    //Constructors

    /** Constructs a learner for an LSTM network.
     * @param   newNet   LSTMNetwork to train
     * @param   newAlpha Learning rat
     * @param   newIndex
     */
    public OnlineSPMSELearning(FunctionalUnit2 newFunc, double newAlpha, int newIndex) {
        m_Func = newFunc;
        m_Alpha = newAlpha;
        m_Index = newIndex;
        m_PreviousOutput = new double[1];
        m_PreviousGradient = new double[1][m_Func.getParameterCount()];
    }

    /*********************************************************************/
    //Properties

    /** Allow the learning rate to be changed.
     * @param   newLearningRate   New learning rate value.
     */
    public void setLearningRate(double newLearningRate) {
        m_Alpha = newLearningRate;
    }

    /*********************************************************************/
    //Special

    /** Indicates the end of a sequence, restart function internal state. */
    public void reset() {
        m_Func.reset();
        m_PreviousOutput = new double[1];
        m_PreviousGradient = new double[1][m_Func.getParameterCount()];
    }

    /*********************************************************************/
    //Methods


    /*********************************************************************/
    //OnlineSupervisedLearning interface implementation

    //The current input is used in conjunction with the previous pattern
    //information (output & gradient) to make the previous step updates.
    //Then the current pattern is process and results are saved for processing
    //at the next pattern presentation.

    public DataSet train(double[] inputPattern, String[] recordList)
    {

        //Compute error vector
        double[] errorPattern = LinearAlgebra.subVectors(m_PreviousOutput, new double[] {inputPattern[m_Index]});

        //Compute sse value
        Double sse_val = new Double(LinearAlgebra.sumSquares(errorPattern));

        //Compute squared error gradient to paramater
        double[] gradients = LinearAlgebra.multVectorMatrix(errorPattern, m_PreviousGradient);

        //Compute deltas
        double[] deltas = LinearAlgebra.multScalarVector(-m_Alpha, gradients);

        //Update weights
        double[] params = m_Func.getParameters();
        m_Func.setParameters(LinearAlgebra.addeVectors(params, deltas));

        //Process the pattern through the network and get derivatives to weights
        FunctionalUnit2.ProcessPatternResult2 result = m_Func.processPattern(inputPattern, false, false, true, false, recordList);

        //Backup current data
        m_PreviousOutput = result.outputPattern;
        m_PreviousGradient = result.parameterDerivative;

        //Plots
        //System.out.println(inputPattern[0] + "\t" + inputPattern[1] + "\t" +
        //                   targetPattern[0] + "\t" +  result.outputPattern[0] + "\t" +
        //                   errorPattern[0]);
        //System.out.println("Deltas:\n" + Tools.tabText(LinearAlgebra.toString(deltas),2));


        //Return extra data
        DataSet ret;
        //Adaptive model specific data
        if ((result.extraData == null) && (recordList.length != 0)) {
            ret = new DataSet();
        } else {
            ret = result.extraData;
        }
        if (DataNames.isMember(DataNames.INPUT_PATTERNS, recordList)) {
            ret.setData(DataNames.INPUT_PATTERNS, inputPattern);
        }
        if (DataNames.isMember(DataNames.OUTPUT_PATTERNS, recordList)) {
            ret.setData(DataNames.OUTPUT_PATTERNS, result.outputPattern);
        }
        //Adaptation rule specific data
        if (DataNames.isMember(DataNames.ERROR_PATTERNS, recordList)) {
            ret.setData(DataNames.ERROR_PATTERNS, errorPattern);
        }
        if (DataNames.isMember(DataNames.TARGET_PATTERNS, recordList)) {
            ret.setData(DataNames.TARGET_PATTERNS, inputPattern);
        }
        if (DataNames.isMember(DataNames.SUM_SQUARED_ERROR, recordList)) {
            ret.setData(DataNames.SUM_SQUARED_ERROR, sse_val);
        }
        //Adaptation specific data
        if (DataNames.isMember(DataNames.VALUE, recordList)) {
            ret.setData(DataNames.VALUE, sse_val);
        }
        if (DataNames.isMember(DataNames.GRADIENT, recordList)) {
            ret.setData(DataNames.GRADIENT, gradients);
        }
        if (DataNames.isMember(DataNames.VARIABLES, recordList)) {
            ret.setData(DataNames.VARIABLES, params);
        }
        if (DataNames.isMember(DataNames.VARIABLE_CHANGES, recordList)) {
            ret.setData(DataNames.VARIABLE_CHANGES, deltas);
        }

        //Return
        return ret;

    }

    public void train(double[] inputPattern)
    {
        train(inputPattern, DataNames.EMPTY_RECORDLIST);
    }

    /*********************************************************************/
    //toString method


    public String toString()
    {
        String ret = super.toString() + "\n";
        ret += "Class: OnlineSPMSELearning\n";
        ret += "\tLearningRate: " + m_Alpha;
        ret += "\tIndex: " + m_Index;
        return ret;
    }


    /*********************************************************************/
    //Cloneable/Serializable interface implementation


}
