package lnsc.pmvf;
import lnsc.*;
import java.io.*;

/** <P> Interface supported by every objects that can be used like a function.
 *  Every units in a neural network, and neural networks themselves are of this
 *  type. A functional unit has the only constraint of being a multivariate
 *  real-vector-valued function. </P>
 *
 *  <P> The <code>FunctionalUnit2</code> interface has methods that provide
 *  information about the type of function encapsulated by the object such as
 *  number of inputs and outputs and whether it is differentiable or not. It
 *  also provides methods to read the values and derivatives of the function for
 *  a given single input pattern or a whole batch of patterns, in which case
 *  some extra information can also be recorded.  </P>
 *
 *  <P> For a complete list of <code>FunctionalUnit</code> included in this
 *  package see <a href="FunctionalUnitChart.html">FunctionalUnit Chart</a></P>
 *
 *  @author Francois Rivest
 *  @version 1.0
 *  @since 1.0
 */

public interface FunctionalUnit2 extends FunctionalUnit, Cloneable, Serializable
{

	/*********************************************************************/
	//Constants

	/** An empty pattern is a pattern of size 0. It is the only input pattern
	 * pattern allowed for function of 0 inputs such as {@link BiasUnit}.
	 */
	//final double[] EMPTY_PATTERN = new double[0];

	/*********************************************************************/
	//Properties

	/** Indicates the number of parameters of the function.
	 * @return		Number of parameters.
	 */
	int getParameterCount();

	/** Gets a copy of the parameters as a vector.
	 * @return		A copy of the parameter values.
	 */
	double[] getParameters();

	/** Sets the parameters values to those of a given vector.
	 * @param		A copy of the parameter values.
	 */
	void setParameters(double[] parameters);

	/** Indicates whether or not the function is differentiable with respect
	 *  to its parameters.
	 * @return		<code>true</code> if it differentiable, <code>false</code>
	 *               otherwise.
	 */
	boolean isParameterDifferentiable();

	/** Indicates whether or not the function is twice differentiable
	 *  with respect to its parameters.
	 * @return		<code>true</code> if it twice differentiable,
	 *              <code>false</code> otherwise.
	 */
	boolean isParameterTwiceDifferentiable();

	/*********************************************************************/
	//Inner classes

	/** Return type for the method {@link #processPattern}.
	 */
	class ProcessPatternResult2
	{
		/** The output pattern.
		 */
		public double[] outputPattern;
		/** The derivative.
		 */
		public double[][] derivative;
		/** The second derivative.
		 */
		public double[][][] secondDerivative;
		/** The derivative with respect to the parameters.
		 */
		public double[][] parameterDerivative;
		/** The second derivative with respect to the parameters..
		 */
		public double[][][] parameterSecondDerivative;
		/** Creates a result holding nothing.
		 */
		protected ProcessPatternResult2() {};
		/** Creates a result holding the output pattern.
		 *  @param      newOutputPattern        The output pattern to be hold.
		 */
		protected ProcessPatternResult2(double[] newOutputPattern)
		{
			outputPattern = newOutputPattern;
		}
		/** Creates a result holding a zero output pattern.
		 *  @param      newOutputCount            The number of outputs.
		 */
		protected ProcessPatternResult2(int newOutputCount)
		{
			outputPattern = new double[newOutputCount];
		}
		/** Contains extra recorded information. */
		public DataSet extraData;

	}

	/*********************************************************************/
	//Methods

	///** @deprecated */
	//FunctionalUnit.ProcessPatternResult processPattern(double[] inputPattern, boolean computeDerivative, boolean computeSecondDerivative);

	/** Processes an input pattern and returns its output pattern and
	 *  derivatives (if requested).
	 * @param    inputPattern    The input pattern.
	 * @param    computeDerivative    Must be <code>true</code> if the
	 *                                derivative should be computed.
	 * @param    computeSecondDerivative    Must be <code>true</code> if the
	 *                                      second derivative should be computed.
	 * @param    computeParameterDerivative    Must be <code>true</code> if the
	 *                                         derivative with respect to the
	 *                                         parameters should be computed.
	 * @param    computeParameterSeconDerivative    Must be <code>true</code> if
	 *                                              be the derivative with
	 *                                              respect to the parameters
	 *                                              should be computed.
	 * @param    recordList     Extra data to be recorded.
	 * @return    The output pattern and the derivative (if requested).
	 */
	ProcessPatternResult2 processPattern(double[] inputPattern,
										 boolean computeDerivative,
										 boolean computeSecondDerivative,
										 boolean computeParameterDerivative,
										 boolean computeParameterSeconDerivative,
										 String[] recordList);

	/** Processes a set of input patterns and record the requested information.
	 *  To get the derivatives for each pattern, use the keyword
	 *  {@link DataNames#DERIVATIVES}, for the second derivatives use
	 *  {@link DataNames#SECOND_DERIVATIVES}, for derivatives wih respect to the
	 *  parameters use {@link DataNames#PARAMETER_DERIVATIVE},  for second
	 *  derivatives with respect to the parameters
	 *  {@link DataNames#PARAMETER_SECOND_DERIVATIVE}. Derivatives are
	 *  available only when the function is differentiable.
	 * @param    dataSet    The data set holding at least
	 *                      {@link DataNames#INPUT_PATTERNS} and
	 *                      {@link DataNames#PATTERN_COUNT}.
	 * @param    recordList    A list of keywords (usually {@link DataNames}
	 *                         constants) of data to record.
	 * @return    The given data set augmented with the
	 *            {@link DataNames#OUTPUT_PATTERNS} and any other keywords
	 *            supported requested. Each <code>FunctionalUnit</code> may
	 *            provide extra keywords of its own.
	 * @see DataNames
	 * @deprecated
	 */
	//DataSet processDataSet(DataSet dataSet,  String[] recordList);



}

//TODO Add isVertible property and corresponding function
//TODO Add functional structure locking mechanism and exception
//TODO Add functional unit exception