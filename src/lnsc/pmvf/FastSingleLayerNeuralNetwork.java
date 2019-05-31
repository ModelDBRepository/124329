package lnsc.pmvf;
import lnsc.*;

/** <P> Implements a single layer feed forward network. It computes a weighted
 * sum of each inputs for each output units. Output units are simple units (i.e.
 * units with a single input and output) and markovienne (i.e. their output does
 * not depend on anything else then their input). A bias can be added internally
 * to the weighted sum if needed. Internal bias is added by expanding the input
 * pattern by the left with the value 1 (i.e. inputPattern = [1 | inputPattern]).
 * </P>
 *
 * <P> This class is not very flexible, it is limited to simple output units and
 * does not support embedded pre and post processing nor extra
 * {@link DataNames} keywords except {@link DataNames#NET_INPUT}.
 * {@link processPattern} is slightly faster than {@link processDataSet} and it
 * is slightly faster when units implement {@link processPattern} directly such
 * as classes derivated from {@link AbstractSimpleUnit}. </P>
 *
 * <P> This class is planned to support left and right weight matrix
 * multiplications or row-based or column-based concatenated weight vector for
 * parametric methods. By default the weighted sum is given by y = Wx (left
 * multiplication) and the parameter vector p = [w1 | ... | wk] where wi are the
 * rows of W (rows concatenation). </P>
 *
 *  @author Francois Rivest
 *  @version 1.0
 *  @since 1.0
 */
public final class FastSingleLayerNeuralNetwork extends AbstractFunctionalUnit2 {

	/*********************************************************************/
	//Serial Version UID

	/** Serial version UID. */
	static final long serialVersionUID = -5261880720093004682L;

	/*********************************************************************/
	//Private fields

	/** Indicates whether there must be an internal bias. */
	protected boolean m_HasBias;

	/** Weights property data. */
	protected double[][] m_Weights;

	/** Output units. */
	protected FunctionalUnit[] m_OutputUnits;

	/** Indicates whether it used weights matrix as left multiplication (y = Wx)
	 *  of the input pattern or as right multiplication (y = xW) of the inputs.
	 */
	protected boolean m_UseLeftMultiplication = true;

	/** Indicates whether the matrix is tranformed by concatenation of its
	 * rows (p = [w1 | ... | wk] where wi are the rows) or its column (p =
	 * [w1T | ... | wkT] where wj are the columns) to generate the vector
	 * parameter representation.
	 */
	protected boolean m_UseRowsConcatenation = true;

	/*********************************************************************/
	//Constructors

	/** Creates a single layer network for a given number of inputs, and
	 *  with a given number of output units of a given type. A bias may be
	 *  provided internally. IMPORTANT: If the output units are not stateless,
	 *  they must all be DISTINCT (false under == ).
	 *  have a bias unit on demand.
	 *  @param      newInputCount             Number of inputs.
	 *  @param      newHasBias                Indicates whether a bias input
	 *                                        should be added internally.
	 *  @param      newOutputUnits            Array of simple output units.
	 *  @param      newUseLeftMultiplication  <code>true</code> for left
	 *                                        multiplication (y = Wx) and <code>
	 *                                        false</code> for right
	 *                                        multiplication (y = xW).
	 *  @param      newUseRowsConcatenation   <code>true</code> for rows
	 *                                        concatenation (p = [w1 | ... | wk]
	 *                                        where wi are rows of W) and <code>
	 *                                        false</code> for columns
	 *                                        concatenation (p = [w1T | ... | wkT]
	 *                                        where wj are columns of W).
	 */
	public FastSingleLayerNeuralNetwork(int newInputCount,
										 boolean newHasBias,
										 FunctionalUnit[] newOutputUnits,
										 boolean newUseLeftMultiplication,
										 boolean newUseRowsConcatenation)
	{
		//Setup
		m_UseLeftMultiplication = newUseLeftMultiplication;
		m_UseRowsConcatenation = newUseRowsConcatenation;

		//Parameters check
		if (newInputCount < 0 ) {
			throw new IllegalArgumentException("newInputCount must be non-negative!");
		}
		for (int i=0; i<newOutputUnits.length; i++)
		{
			if ((newOutputUnits[i].getInputCount() != 1) |
				(newOutputUnits[i].getOutputCount() != 1)) {
				throw new IllegalArgumentException("newOutputUnits must be simple units!");
			}
		}

		//Construct input layer
		m_InputCount = newInputCount;
		m_HasBias = newHasBias;

		//Construct output layer
		m_OutputCount = newOutputUnits.length;
		m_OutputUnits = newOutputUnits;

		//Differentiability check
		m_IsDifferentiable = true;
		m_IsTwiceDifferentiable = true;
		for (int i=0; i<m_OutputCount; i++)
		{
			m_IsDifferentiable &= m_OutputUnits[i].isDifferentiable();
			m_IsTwiceDifferentiable &= m_OutputUnits[i].isTwiceDifferentiable();
		}

		//Construct parameters
		if (m_UseLeftMultiplication) {
			m_Weights = new double[m_OutputCount][m_InputCount+(m_HasBias ? 1 : 0)];
		} else {
			m_Weights = new double[m_InputCount+(m_HasBias ? 1 : 0)][m_OutputCount];
		}
		m_ParameterCount = m_OutputCount*(m_InputCount+(m_HasBias ? 1:0));

		//Parameters differentiability check
		m_IsParameterDifferentiable = m_IsDifferentiable;
		m_IsParameterTwiceDifferentiable = m_IsTwiceDifferentiable;

		//Stateless check
		m_IsStateless = true;
		for (int i=0; i<m_OutputCount; i++)
		{
			m_IsStateless &= m_OutputUnits[i].isStateless();
		}
	}

	/** Creates a single layer network for a given number of inputs, and
	 *  with a given number of output units of a given type. A bias may be
	 *  provided internally.
	 *  have a bias unit on demand.
	 *  @param      newInputCount             Number of inputs.
	 *  @param      newHasBias                Indicates whether a bias input
	 *                                        should be added internally.
	 *  @param      newOutputCount            Number of outputs.
	 *  @param      newOutputUnit             Sample of an output unit.
	 *  @param      newUseLeftMultiplication  <code>true</code> for left
	 *                                        multiplication (y = Wx) and <code>
	 *                                        false</code> for right
	 *                                        multiplication (y = xW).
	 *  @param      newUseRowsConcatenation   <code>true</code> for rows
	 *                                        concatenation (p = [w1 | ... | wk]
	 *                                        where wi are rows of W) and <code>
	 *                                        false</code> for columns
	 *                                        concatenation (p = [w1T | ... | wkT]
	 *                                        where wj are columns of W).
	 */
	public FastSingleLayerNeuralNetwork(int newInputCount,
										 boolean newHasBias,
										 int newOutputCount,
										 FunctionalUnit newOutputUnit,
										 boolean newUseLeftMultiplication,
										 boolean newUseRowsConcatenation)
	{
		this(newInputCount,
			 newHasBias,
			 Tools.createUnitArray(newOutputCount, newOutputUnit),
			 newUseLeftMultiplication,
			 newUseRowsConcatenation);
	}

	/** Creates a single layer network for a given number of inputs, and
	 *  with a given number of output units of a given type. A bias may be
	 *  provided internally.
	 *  have a bias unit on demand.
	 *  @param      newInputCount             Number of inputs.
	 *  @param      newHasBias                Indicates whether a bias input
	 *                                        should be added internally.
	 *  @param      newOutputUnits            Array of simple output units.
	 */
	public FastSingleLayerNeuralNetwork(int newInputCount,
										 boolean newHasBias,
										 FunctionalUnit[] newOutputUnits)
	{
		this(newInputCount,
			 newHasBias,
			 newOutputUnits,
			 true,
			 true);
	}

	/** Creates a single layer network for a given number of inputs, and
	 *  with a given number of output units of a given type. A bias may be
	 *  provided internally.
	 *  have a bias unit on demand.
	 *  @param      newInputCount             Number of inputs.
	 *  @param      newHasBias                Indicates whether a bias input
	 *                                        should be added internally.
	 *  @param      newOutputCount            Number of outputs.
	 *  @param      newOutputUnit             Sample of an output unit.
	 */
	public FastSingleLayerNeuralNetwork(int newInputCount,
										 boolean newHasBias,
										 int newOutputCount,
										 FunctionalUnit newOutputUnit)
	{
		this(newInputCount,
			 newHasBias,
			 Tools.createUnitArray(newOutputCount, newOutputUnit),
			 true,
			 true);
	}

	/*********************************************************************/
	//Properties

	/** Gets a reference to the array of output units. Modifying the units
	 *  inside this array could lead to unexpected error.
	 *  @return      Reference to the array of output units.
	 */
	public final FunctionalUnit[] getOutputUnits()
	{
		return m_OutputUnits;
	}

	/** Indicates whether there is an internal bias.
	 * @return    <code>true</code> id there is an internal bias.
	 */
	public boolean hasBias() {return m_HasBias;}

	/** Indicates whether it used weights matrix as left multiplication (y = Wx)
	 *  of the input pattern or as right multiplication (y = xW) of the inputs.
	 * @return    <code>true</code> if it uses left multiplication (default).
	 */
	public boolean useLeftMultiplication() {return m_UseLeftMultiplication;}

	/** Indicates whether the matrix is tranformed by concatenation of its
	 * rows (p = [w1 | ... | wk] where wi are the rows) or its column (p =
	 * [w1T | ... | wkT] where wj are the columns) to generate the vector
	 * parameter representation.
	 * @return    <code>true</code> if it uses rows concatenation (default).
	 */
	public boolean useRowsConcatenation() {return m_UseRowsConcatenation;}

	/** Gets a reference to the weight matrix. Matrix sizes depends on the
	 * {link #useLeftMultiplication()} value.
	 *  @return      References to the weights matrix.
	 */
	public double[][] getWeights()
	{
		return m_Weights;
	}

	/** Assigns a new weight matrix by reference. Matrix sizes depends on the
	 * {link #useLeftMultiplication()} value.
	 * @param        A new weights matrix reference.
	 */
	public void setWeights(double[][] newWeights)
	{
		//Parameters check
		if (m_UseLeftMultiplication) {
			if (!LinearAlgebra.isMatrix(newWeights, m_OutputCount, m_InputCount + (m_HasBias ? 1 : 0))) {
				throw new IllegalArgumentException("newWeights is of the wrong size!");
			}
		} else {
			if (!LinearAlgebra.isMatrix(newWeights, m_InputCount + (m_HasBias ? 1 : 0), m_OutputCount)) {
				throw new IllegalArgumentException("newWeights is of the wrong size!");
			}
		}
		//Assign new matrix
		m_Weights = newWeights;
	}

	/*********************************************************************/
	//FunctionalUnit interface implementation

	public void reset()
	{
		for (int i=0; i<m_OutputCount; i++)
		{
			m_OutputUnits[i].reset();
		}
	}

	public FunctionalUnit2.ProcessPatternResult2 processPattern(
		   double[] inputPattern,
		   boolean computeDerivative,
		   boolean computeSecondDerivative,
		   boolean computeParameterDerivative,
		   boolean computeParameterSecondDerivative,
		   String[] recordList)
	{
		//Param check
		FunctionalUnit2.ProcessPatternResult2 ret = preProcessPattern(
				  inputPattern,
				  computeDerivative,
				  computeSecondDerivative,
				  computeParameterDerivative,
				  computeParameterSecondDerivative,
				  recordList);

		//Process input layer
		double[] inputs =
			m_HasBias ?
			LinearAlgebra.concatenateVectors(new double[] {1.0}, inputPattern) :
			inputPattern;

		//Process weights
		double[] weightedSums =
			m_UseLeftMultiplication ?
			LinearAlgebra.multMatrixVector(m_Weights, inputs) :
			LinearAlgebra.multVectorMatrix(inputs, m_Weights);

		//Collect weighted sum if requested
		if (DataNames.isMember(DataNames.NET_INPUT, recordList)) {
			ret.extraData = new DataSet();
			ret.extraData.setData(DataNames.NET_INPUT, weightedSums);
		}

		//Process output
		FunctionalUnit.ProcessPatternResult[] outputs =
			new FunctionalUnit.ProcessPatternResult[m_OutputCount];
		for (int i=0; i<m_OutputCount; i++)
		{
			outputs[i] = m_OutputUnits[i].processPattern(
						 new double[] {weightedSums[i]},
						 computeDerivative & computeParameterDerivative,
						 computeSecondDerivative & computeParameterSecondDerivative);
		}

		//Results
		for (int o=0; o<m_OutputCount; o++)
		{
			ret.outputPattern[o] = outputs[o].outputPattern[0];
		}

		//Derivatives
		if (computeDerivative) {
			for (int o = 0; o < m_OutputCount; o++) {
				for (int i = 0; i < m_InputCount; i++) {
					if (m_UseLeftMultiplication) {
						ret.derivative[o][i] =
							outputs[o].derivative[0][0] *
							m_Weights[o][i+(m_HasBias ? 1 : 0)];
					} else {
						ret.derivative[o][i] =
							outputs[o].derivative[0][0] *
							m_Weights[i+(m_HasBias ? 1 : 0)][o];
					}
				}
			}
		}
		if (computeSecondDerivative) {
			for (int o = 0; o < m_OutputCount; o++) {
				for (int i = 0; i < m_InputCount; i++) {
					if (m_UseLeftMultiplication) {
						ret.secondDerivative[o][i][i] =
							outputs[o].secondDerivative[0][0][0] *
							m_Weights[o][i+(m_HasBias ? 1 : 0)] *
							m_Weights[o][i+(m_HasBias ? 1 : 0)];
					} else {
						ret.secondDerivative[o][i][i] =
							outputs[o].secondDerivative[0][0][0] *
							m_Weights[i+(m_HasBias ? 1 : 0)][o] *
							m_Weights[i+(m_HasBias ? 1 : 0)][o];
					}
				}
			}
		}

		//Parametric Derivatives
		if (computeParameterDerivative) {
			for (int o = 0; o < m_OutputCount; o++) {
				for (int i = 0; i < inputs.length; i++) {
					int weightIndex;
					if (m_UseLeftMultiplication == m_UseRowsConcatenation) {
							weightIndex = o*inputs.length + i;
					} else {
							weightIndex = o + i*m_OutputCount;
					}
					ret.parameterDerivative[o][weightIndex] =
						outputs[o].derivative[0][0] *
						inputs[i];
				}
			}
		}
		if (computeParameterSecondDerivative) {
			for (int o = 0; o < m_OutputCount; o++) {
				for (int i = 0; i < inputs.length; i++) {
					int weightIndex;
					if (m_UseLeftMultiplication == m_UseRowsConcatenation) {
							weightIndex = o*inputs.length + i;
					} else {
							weightIndex = o + i*m_OutputCount;
					}
					ret.parameterSecondDerivative[o][weightIndex][weightIndex] =
						outputs[o].secondDerivative[0][0][0] *
						inputs[i] * inputs[i];
				}
			}
		}


		//Return
		return ret;
	}

	public double[] getParameters()
	{
		//Return a vectorized copy of the parameters (the weights)
		return
					m_UseRowsConcatenation ?
					LinearAlgebra.concatenateRows(m_Weights) :
					LinearAlgebra.concatenateCols(m_Weights);
	}

	public void setParameters(double[] parameters)
	{
		//Parameters check
		if (!LinearAlgebra.isVector(parameters, m_ParameterCount)) {
			throw new IllegalArgumentException("parameters is of the wrong size!");
		}

		//Convert to a matrix
		int rows =
			m_UseLeftMultiplication ?
			m_OutputCount:
			(m_InputCount + (m_HasBias ? 1:0));
		int cols =
			m_UseLeftMultiplication ?
			(m_InputCount + (m_HasBias ? 1:0)):
			m_OutputCount;
		double[][] newWeights =
			m_UseRowsConcatenation ?
			LinearAlgebra.cutInRows(parameters, cols) :
			LinearAlgebra.cutInCols(parameters, rows);

		//Overwrite m_Weights
		LinearAlgebra.eMatrix(m_Weights, newWeights);
	}

	/*********************************************************************/
	//toString method

	public String toString()
	{
		String ret = super.toString() + "\n";
		ret += "Class: FastSingleLayerNetwork\n";
		//Properties
		ret += "\tLeftMultiplication:" + m_UseLeftMultiplication + "\n";
		ret += "\tRowsConcatenation:" + m_UseRowsConcatenation + "\n";
		//Inputs
		ret += "\tHasBias:" + m_HasBias + "\n";
		//Outputs
		for (int i=0; i<m_OutputCount; i++)
		{
			ret += "\tOutputUnits:[" + i + "]\n";
			ret += Tools.tabText(m_OutputUnits[i].toString(), 2) + "\n";
		}
		//Weights
		ret += "\tWeights = \n";
		ret += Tools.tabText(LinearAlgebra.toString(m_Weights),2);
		//return
		return ret;
	}

	/*********************************************************************/
	//Cloneable interface implementation

	public Object clone()
	{
		return Tools.copyObject(this);
	}

}

//TODO: Check for distinct output units in the [] constructor under not stateless