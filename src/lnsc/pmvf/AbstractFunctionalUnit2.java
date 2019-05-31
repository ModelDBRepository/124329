package lnsc.pmvf;
import lnsc.*;

/** <P> Abstract class containing the basic implementation for the
 *  <code>FunctionalUnit2</code> interface. </P>
 *
 *  <P> In order to implement the <code>FunctionalUnit2</code> interface,
 *  subclasses need the following 4 things: </P>
 *  <ol>
 *      <li>In the constructor, the fields <code>m_InputCount</code>,
 *          <code>m_OutputCount</code>, <code>m_IsDifferentiable</code>,
 *          <code>m_IsTwiceDifferentiable</code>, <code>m_ParametersCount,
 *          <code>m_IsParametersDifferentiable</code>, and
 *          <code>m_IsParametersTwiceDifferentiable</code> must be filled
 *          appropriately. </li>
 *      <li><code>processPattern(double[], boolean, boolean, boolean, boolean)</code>
 *          must be implemented and should prefrerably begin by calling
 *          <code>preProcessPattern(double[], boolean, boolean, boolean, boolean)</code>.
 *          For non stateless function, <code>reset()</code> must be added.</li>
 *      <li>If the function is parametric (i.e. has more than one parameters),
 *          functions <code>getParameters()</code> and <code>setParameters(double[])</code>
 *          must be written accordingly. Note that get and set parameters must
 *          work by copying values, not referencing to whole arrays.</li>
 *      <li>Since <code>FunctionalUnit2</code> are <code>Serializable</code> and
 *          <code>Cloneable</code>, any required extra code to make these
 *          interfaces work properly should be added. It is necessary to at
 *          least set the <code>private static serialVersionUID</code> variable
 *          appropriately for the <code>Seriablizable</code> interface. For
 *          complex objects, the <code>Cloneable</code> interface can rely on
 *          <code>Tools.copyObject(Serializable)</code> for deep cloning. </li>
 *  </ol>
 *
 *  @author Francois Rivest
 *  @version 1.0
 *  @since 1.0
 */
public abstract class AbstractFunctionalUnit2 implements FunctionalUnit2
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
	 *  That is, the value returned by getOutputCount().
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

	/** Indicates the number of parameters for this function.
	 *  That is, the value returned by getParameterCount().
	 *  Derived classes must fill this slot in their constructor.
	 */
	protected int m_ParameterCount;

	/** Indicates whether or not the function is differentiable with respect
	 *  to its parameters.
	 *  That is, the value returned by isParameterDifferentiable().
	 *  Derived classes must fill this slot in their constructor.
	 */
	protected boolean m_IsParameterDifferentiable;

	/** Indicates whether or not the function is twice differentiable with
	 *  respect to its parameters.
	 *  That is, the value returned by isParameterTwiceDifferentiable().
	 *  Derived classes must fill this slot in their constructor.
	 */
	protected boolean m_IsParameterTwiceDifferentiable;

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
	public final int getParameterCount() {return m_ParameterCount;}
	public final boolean isParameterDifferentiable() {return m_IsParameterDifferentiable;}
	public final boolean isParameterTwiceDifferentiable() {return m_IsParameterTwiceDifferentiable;}
	public final boolean isStateless() {return m_IsStateless;}

	//Default implementation does nothing
	public void reset() {return;}

	//For functionalUnit only
	public FunctionalUnit.ProcessPatternResult processPattern(
		   double[] inputPattern,
		   boolean computeDerivative,
		   boolean computeSecondDerivative)
	{
		FunctionalUnit2.ProcessPatternResult2 ret =
			processPattern(inputPattern,
						   computeDerivative,
						   computeSecondDerivative,
						   false,
						   false,
						   new String[0]);
		return new FunctionalUnit.ProcessPatternResult(ret.outputPattern,
			                                           ret.derivative,
			                                           ret.secondDerivative);
	}

	/** This function validates the arguments and creates the object to
	 *  return. It should be called at the very beginning of the method
	 *  {@link #processPattern}.
	 */
	protected final FunctionalUnit2.ProcessPatternResult2 preProcessPattern(
		   double[] inputPattern,
		   boolean computeDerivative,
		   boolean computeSecondDerivative,
		   boolean computeParameterDerivative,
		   boolean computeParameterSecondDerivative,
		   String[] recordList)
	{
		if (inputPattern.length != m_InputCount)
			throw new IllegalArgumentException("inputPatten is of the wrong size!");
		else if (computeDerivative && (!m_IsDifferentiable))
			throw new IllegalArgumentException("computeDerivative requested on a non-differentiable function!");
		else if (computeSecondDerivative && (!m_IsTwiceDifferentiable))
			throw new IllegalArgumentException("computeSecondDerivative requested on a non-twicedifferentiable function!");
		else if (computeParameterDerivative && (!m_IsParameterDifferentiable))
			throw new IllegalArgumentException("computeParameterDerivative requested on a non-differentiable function!");
		else if (computeParameterSecondDerivative && (!m_IsParameterTwiceDifferentiable))
			throw new IllegalArgumentException("computeParameterSecondDerivative requested on a non-twicedifferentiable function!");
		else
		{
			FunctionalUnit2.ProcessPatternResult2 ret = new FunctionalUnit2.ProcessPatternResult2(m_OutputCount);
			if (computeDerivative) {ret.derivative = new double[m_OutputCount][m_InputCount];}
			if (computeSecondDerivative) {ret.secondDerivative = new double[m_OutputCount][m_InputCount][m_InputCount];}
			if (computeParameterDerivative) {ret.parameterDerivative = new double[m_OutputCount][m_ParameterCount];}
			if (computeParameterSecondDerivative) {ret.parameterSecondDerivative = new double[m_OutputCount][m_ParameterCount][m_ParameterCount];}
			return ret;
		}
	}

	//The default implementation calls processDataSet
	public FunctionalUnit2.ProcessPatternResult2 processPattern(
		   double[] inputPattern,
		   boolean computeDerivative,
		   boolean computeSecondDerivative,
		   boolean computeParameterDerivative,
		   boolean computeParameterSecondDerivative,
		   String[] recordList)
	{
        //Preprocessing
		FunctionalUnit2.ProcessPatternResult2 ret =
			preProcessPattern(inputPattern,
							  computeDerivative,
							  computeSecondDerivative,
							  computeParameterDerivative,
							  computeParameterSecondDerivative,
							  recordList);

		//Construct dataSet
		DataSet dat = new DataSet();
		dat.setData(DataNames.PATTERN_COUNT, new Integer(1));
		dat.setData(DataNames.INPUT_PATTERNS, new double[][] {inputPattern});

		//Construct recordList
		if (computeDerivative) {
			recordList = DataNames.concat(recordList, new String[]{DataNames.DERIVATIVES});
		}
		if (computeSecondDerivative) {
			recordList = DataNames.concat(recordList, new String[]{DataNames.SECOND_DERIVATIVES});
		}
		if (computeParameterDerivative) {
			recordList = DataNames.concat(recordList, new String[]{DataNames.PARAMETER_DERIVATIVES});
		}
		if (computeParameterSecondDerivative) {
			recordList = DataNames.concat(recordList, new String[]{DataNames.PARAMETER_SECOND_DERIVATIVES});
		}

		//Process
		processDataSet(dat, recordList);

		//Extract results
		ret.outputPattern = ((double[][]) dat.getData(DataNames.OUTPUT_PATTERNS))[0];
		if (computeDerivative) {
			ret.derivative = ((double[][][]) dat.getData(DataNames.DERIVATIVES))[0];
		}
		if (computeSecondDerivative) {
			ret.secondDerivative = ((double[][][][]) dat.getData(DataNames.SECOND_DERIVATIVES))[0];
		}
		if (computeParameterDerivative) {
			ret.parameterDerivative = ((double[][][]) dat.getData(DataNames.PARAMETER_DERIVATIVES))[0];
		}
		if (computeParameterSecondDerivative) {
			ret.parameterSecondDerivative = ((double[][][][]) dat.getData(DataNames.PARAMETER_SECOND_DERIVATIVES))[0];
		}
		dat.removeAllBut(recordList);
		ret.extraData = dat;

		//Return
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
		double[][][] derivatives = null;
		double[][][][] secondDerivatives = null;
		double[][][] parameterDerivatives = null;
		double[][][][] parameterSecondDerivatives = null;

		//Preprocessing
		preProcessDataSet(dataSet, recordList);

		//Extract dataSet
		int patternCount = ((Integer) dataSet.getData(DataNames.PATTERN_COUNT)).intValue();
		double[][] inputs = (double[][]) dataSet.getData(DataNames.INPUT_PATTERNS);

		//Extract recordList
		boolean computeDerivative = DataNames.isMember(DataNames.DERIVATIVES, recordList);
		boolean computeSecondDerivative = DataNames.isMember(DataNames.SECOND_DERIVATIVES, recordList);
		boolean computeParameterDerivative = DataNames.isMember(DataNames.PARAMETER_DERIVATIVES, recordList);
		boolean computeParameterSecondDerivative = DataNames.isMember(DataNames.PARAMETER_SECOND_DERIVATIVES, recordList);

		//Create result spaces
		double[][] outputs = new double[patternCount][];
		if (computeDerivative) {
			derivatives = new double[patternCount][][];
		}
		if (computeSecondDerivative) {
			secondDerivatives = new double[patternCount][][][];
		}
		if (computeParameterDerivative) {
			parameterDerivatives = new double[patternCount][][];
		}
		if (computeParameterSecondDerivative) {
			parameterSecondDerivatives = new double[patternCount][][][];
		}

		//Process patterns
		DataSetCollection extraData = new DataSetCollection(patternCount);
		for (int i=0; i<patternCount; i++)
		{
			FunctionalUnit2.ProcessPatternResult2 ret =
				processPattern(inputs[i],
							   computeDerivative,
							   computeSecondDerivative,
							   computeParameterDerivative,
							   computeParameterSecondDerivative,
							   recordList);
			outputs[i] = ret.outputPattern;
			if (computeDerivative) {derivatives[i] = ret.derivative;}
			if (computeSecondDerivative) {secondDerivatives[i] = ret.secondDerivative;}
			if (computeParameterDerivative) {derivatives[i] = ret.parameterDerivative;}
			if (computeParameterSecondDerivative) {secondDerivatives[i] = ret.parameterSecondDerivative;}
			extraData.setDataSet(i, ret.extraData, false);
		}

		//Save results
		dataSet.setData(DataNames.OUTPUT_PATTERNS, outputs);
		if (computeDerivative) {dataSet.setData(DataNames.DERIVATIVES, derivatives);}
		if (computeSecondDerivative) {dataSet.setData(DataNames.SECOND_DERIVATIVES, secondDerivatives);}
		if (computeParameterDerivative) {dataSet.setData(DataNames.PARAMETER_DERIVATIVES, parameterDerivatives);}
		if (computeParameterSecondDerivative) {dataSet.setData(DataNames.PARAMETER_SECOND_DERIVATIVES, parameterSecondDerivatives);}
		dataSet.setData(DataNames.EXTRA_DATA, extraData);

		//Compute ERROR_PATTERNS on requested
		if (DataNames.isMember(DataNames.ERROR_PATTERNS, recordList)) {
			computeErrorPatterns(dataSet);
		}

		//return
		return dataSet;
	}


	/** Computes the error patterns given a data set that contains both the
	 *  real outputs {@link DataNames#OUTPUT_PATTERNS} and the target
	 *  outputs {@link DataNames#TARGET_PATTERNS}. Results are stored in the
	 *  data set under {@link DataNames#ERROR_PATTERNS}.
	 *  @param          dataSet         A data set containing two required
	 *                                  datas.
	 *  @return         The original <code>dataSet</code> augmented with
	 *                  the added data.
	 *  @todo error checking
	 *  @deprecated
	 */
	protected DataSet computeErrorPatterns(DataSet dataSet)
	{
		//TODO: error checking
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
		ret += "\tParameterCount: " + Integer.toString(m_ParameterCount) + "\n";
		ret += "\tIsParameterDifferentiable: " + new Boolean(m_IsParameterDifferentiable).toString() + "\n";
		ret += "\tIsParameterTwiceDifferentiable: " + new Boolean(m_IsParameterTwiceDifferentiable).toString() + "\n";
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
