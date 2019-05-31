package lnsc;
import java.util.*;

/** <P> Abstract class containing the basic implementation for the
 *  <code>FunctionalUnit</code> interface. </P>
 *
 *  <P> In order to implement the <code>FunctionalUnit</code> interface,
 *  subclasses need the following 3 things: </P>
 *  <ol>
 *      <li>In the constructor, the fields <code>m_InputCount</code>,
 *          <code>m_OutputCount</code>, <code>m_IsDifferentiable</code>, and
 *          <code>m_IsStateless</code> must be filled appropriately. </li>
 *      <li>Either <code>processDataSet(DataSet, String[])</code> or
 *          <code>processPattern(double[], boolean)</code>
 *          must be implemented and should prefrerably begin by calling
 *          <code>preProcessDataSet(DataSet, String[])</code> or
 *          <code>preProcessPattern(double[], boolean)</code> respectively.
 *          For non stateless function, <code>reset()</code> must be added.</li>
 *      <li>Since <code>FunctionalUnit</code> are <code>Serializable</code> and
 *          <code>Cloneable</code>, any required extra code to make these
 *          interfaces work properly should be added. It is necessary to at
 *          least set the <code>private static serialVersionUID</code> variable
 *          appropriately for the <code>Seriablizable</code> interface. For
 *          complex objects, the <code>Cloneable</code> interface can rely on
 *          <code>Tools.copyObject(Serializable)</code>. </li>
 *  </ol>
 *
 *  @see Tools#copyObject(Serializable)
 *
 *  @author Francois Rivest
 *  @version 1.0
 *  @since 1.0
 */
public abstract class AbstractFunctionalUnit implements FunctionalUnit
{

	/*********************************************************************/
    //Serial Version UID

	/** Serial version UID. */
	static final long serialVersionUID = -6359518335520622462L;

	/*********************************************************************/
	//Private fields

	/** Indicates the number of variables of the function.
	 *  That is, the value returned by getInputCount().
	 *  Derived classes must fill this slot in their constructor.
	 */
	protected int m_InputCount;

	/** Indicates the number of values returned by the function.
	 *  Tha is, the value returned by getOutputCount().
	 *  Derived classes must fill this slot in their constructor.
	 */
	protected int m_OutputCount;

	/** Indicates whether or not the function is differentiable.
	 *  That is, the value returned by isDifferentiable().
	 *  Derived classes must fill this slot in their constructor.
	 */
	protected boolean m_IsDifferentiable;

	/** Indicates whether or not the function is twice differentiable.
	 *  That is, the value returned by isTwiceDifferentiable().
	 *  Derived classes must fill this slot in their constructor.
	 */
	protected boolean m_IsTwiceDifferentiable;

	/** Indicates whether or not the function output depends solely of the
	 *  current input (and not of the previous pattern it has processed).
	 *  That is, the value returned by isStateless().
	 *  Derived classes must fill this slot in their constructor.
	 */
	boolean m_IsStateless;

	/*********************************************************************/
	//FunctionalUnit implementation

	public final int getInputCount() {return m_InputCount;}
	public final int getOutputCount() {return m_OutputCount;}
	public final boolean isDifferentiable() {return m_IsDifferentiable;}
	public final boolean isTwiceDifferentiable() {return m_IsTwiceDifferentiable;}
	public final boolean isStateless() {return m_IsStateless;}

	//Default implementation does nothing
	public void reset() {return;}

	/** This function validates the argument and creates the object to return.
	 *  It should be called at the very beginning of the method
	 *  {@link #processPattern}.
	 */
	protected final FunctionalUnit.ProcessPatternResult preProcessPattern(double[] inputPattern, boolean computeDerivative, boolean computeSecondDerivative)
	{
		if (inputPattern.length != m_InputCount)
			throw new IllegalArgumentException("inputPatten is of the wrong size!");
		else if (computeSecondDerivative && (!m_IsTwiceDifferentiable))
			throw new IllegalArgumentException("computeSecondDerivative requested on a non-twicedifferentiable function!");
		else if (computeDerivative && (!m_IsDifferentiable))
			throw new IllegalArgumentException("computeDerivative requested on a non-differentiable function!");
		else
		{
			if (!computeDerivative && !computeSecondDerivative)
				return new FunctionalUnit.ProcessPatternResult(new double[m_OutputCount]);
			else if (!computeSecondDerivative)
				return new FunctionalUnit.ProcessPatternResult(new double[m_OutputCount],
												new double[m_OutputCount][m_InputCount]);
			else
				return new FunctionalUnit.ProcessPatternResult(new double[m_OutputCount],
												new double[m_OutputCount][m_InputCount],
									new double[m_OutputCount][m_InputCount][m_InputCount]);
		}
	}

	//The default implementation calls processDataSet
	public FunctionalUnit.ProcessPatternResult processPattern(double[] inputPattern, boolean computeDerivative, boolean computeSecondDerivative)
	{
		FunctionalUnit.ProcessPatternResult ret = preProcessPattern(inputPattern, computeDerivative, computeSecondDerivative);
		DataSet dat = new DataSet();
		dat.setData(DataNames.PATTERN_COUNT, new Integer(1));
		dat.setData(DataNames.INPUT_PATTERNS, new double[][] {inputPattern});
		String[] recordList;
		if (computeSecondDerivative) {
			recordList = new String[] {DataNames.SECOND_DERIVATIVES, DataNames.DERIVATIVES};
		} else if (computeDerivative) {
			recordList = new String[] {DataNames.DERIVATIVES};
		} else {
			recordList = new String[0];
		}
		processDataSet(dat, recordList);
		ret.outputPattern = ((double[][]) dat.getData(DataNames.OUTPUT_PATTERNS))[0];
		if (computeDerivative) {
			ret.derivative = ((double[][][]) dat.getData(DataNames.DERIVATIVES))[0];
		}
		if (computeSecondDerivative) {
			ret.secondDerivative = ((double[][][][]) dat.getData(DataNames.SECOND_DERIVATIVES))[0];
		}
		return ret;
	}

	/** This function validates the <code>dataSet</code>. It should be called at
	 *  the very beginning of the method {@link #processDataSet}.
	 */
	protected final DataSet preProcessDataSet(DataSet dataSet, String[] recordList)
	{
		//check requirement
		if (!dataSet.hasData(DataNames.PATTERN_COUNT)) {
			throw new MissingDataException("Missing DataNames.PATTERN_COUNT in data set!", DataNames.PATTERN_COUNT);
		}
		if (!dataSet.hasData(DataNames.INPUT_PATTERNS)) {
			throw new MissingDataException("Missing DataNames.INPUT_PATTERNS in data set!", DataNames.INPUT_PATTERNS);
		}
		//check types & sizes
		if (!(dataSet.getData(DataNames.PATTERN_COUNT) instanceof Integer)) {
			throw new InvalidDataException("DataNames.PATTERN_COUNT must be an Integer!", DataNames.PATTERN_COUNT);
		}
		int pCount = ((Integer) dataSet.getData(DataNames.PATTERN_COUNT)).intValue();
		if (pCount < 0) {
			throw new InvalidDataException("DataNames.PATTERN_COUNT must be non-negative!", DataNames.PATTERN_COUNT);
		}
		if (!LinearAlgebra.isMatrix(dataSet.getData(DataNames.INPUT_PATTERNS), pCount, m_InputCount)) {
			throw new InvalidDataException("DataNames.INPUT_PATTERNS does match DataNames.PATTERN_COUNT and FunctionalUnit.getInputCount() in data set!", DataNames.INPUT_PATTERNS);
		}
		//return
		return dataSet;
	}

	//The default implementation calls processPattern
	public DataSet processDataSet(DataSet dataSet, String[] recordList)
	{
		int i;
		FunctionalUnit.ProcessPatternResult ret;
		double[][][] derivatives = null;
		double[][][][] secondDerivatives = null;

		//parameter check
		preProcessDataSet(dataSet, recordList);

		//gather input patterns
		double[][] inputs = (double[][]) dataSet.getData(DataNames.INPUT_PATTERNS);
		int count = inputs.length;

		//create output patterns space
		double[][] outputs = new double[count][];

		//create derivative space if necessary
		boolean computeDerivative = DataNames.isMember(DataNames.DERIVATIVES, recordList);
		if (computeDerivative) {derivatives = new double[count][][];}

		//create derivative space if necessary
		boolean computeSecondDerivative = DataNames.isMember(DataNames.SECOND_DERIVATIVES, recordList);
		if (computeSecondDerivative) {secondDerivatives = new double[count][][][];}

		//for each input pattern
		for (i=0; i<count; i++)
		{
			//compute output pattern (and derivative)
			ret = processPattern(inputs[i], computeDerivative, computeSecondDerivative);
			outputs[i] = ret.outputPattern;
			if (computeDerivative) {derivatives[i] = ret.derivative;}
			if (computeSecondDerivative) {secondDerivatives[i] = ret.secondDerivative;}
		}

		//put result back in data set
		dataSet.setData(DataNames.OUTPUT_PATTERNS, outputs);
		if (computeDerivative) {dataSet.setData(DataNames.DERIVATIVES, derivatives);}
		if (computeSecondDerivative) {dataSet.setData(DataNames.SECOND_DERIVATIVES, secondDerivatives);}
		//check for error computation
		if (DataNames.isMember(DataNames.ERROR_PATTERNS, recordList)) {
			computeErrorPatterns(dataSet);
		}
		//return
		return dataSet;
	}

	/** Computes the error patterns given a data set that contains both the
	 *  real outputs 'OutputPatterns' and the target outputs 'TargetPatterns'.
	 *  Results are stored in the data set under 'ErrorPatterns'.
	 *  @param          dataSet         A data set containing three required
	 *                                  datas.
	 *  @return         The original <code>dataSet</code> augmented with
	 *                  'ErrorPatterns'.
	 */
	protected DataSet computeErrorPatterns(DataSet dataSet)
	{
		//error checking
		double[][] outputs = (double[][]) dataSet.getData(DataNames.OUTPUT_PATTERNS);
		double[][] targets = (double[][]) dataSet.getData(DataNames.TARGET_PATTERNS);
		double[][] errors = LinearAlgebra.batchSubVectors(outputs, targets);
		dataSet.setData(DataNames.ERROR_PATTERNS, errors);
		return dataSet;
	}

	/*********************************************************************/
	//toString method

	 public String toString()
	 {
		String ret = new String();
		ret += "Interface: FunctionalUnit\n";
		ret += "\tInputCount: " + Integer.toString(m_InputCount) + "\n";
		ret += "\tOutputCount: " + Integer.toString(m_OutputCount) + "\n";
		ret += "\tIsDifferentiable: " + new Boolean(m_IsDifferentiable).toString() + "\n";
		ret += "\tIsTwiceDifferentiable: " + new Boolean(m_IsTwiceDifferentiable).toString() + "\n";
		ret += "\tIsStateless: " + new Boolean(m_IsStateless).toString();
		return ret;
	 }


    /*********************************************************************/
	//Cloneable interface implementation

    public Object clone() throws CloneNotSupportedException
	{
		return super.clone();
	}

}
