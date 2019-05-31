package lnsc;
import java.util.*;
import java.io.*;

/** <P> Interface supported by every objects that can be used like a function.
 *  Every units in a neural network, and neural networks themselves are of this
 *  type. A functional unit has the only constraint of being a multivariate
 *  real-vector-valued function. </P>
 *
 *  <P> The <code>FunctionalUnit</code> interface has methods that provide
 *  information about the type of function encapsulated by the object such as
 *  number of inputs and outputs and whether it is differentiable or not. It
 *  also provides methods to read the values and derivatives of the function for
 *  a given single input pattern or a whole batch of patterns, in which case
 *  some extra information can also be recorded. </P>
 *
 *  <P> For a complete list of <code>FunctionalUnit</code> included in this
 *  package see <a href="FunctionalUnitChart.html">FunctionalUnit Chart</a></P>
 *
 *  @author Francois Rivest
 *  @version 1.0
 *  @since 1.0
 */
public interface FunctionalUnit extends Cloneable, Serializable
{

	/*********************************************************************/
    //Serial Version UID

	/** Serial version UID. */

	/*********************************************************************/
    //Constants

	/** An empty pattern is a pattern of size 0. It is the only input pattern
	 * pattern allowed for function of 0 inputs such as {@link BiasUnit}.
	 */
	final double[] EMPTY_PATTERN = new double[0];

	/*********************************************************************/
    //Properties

	/** Indicates the number of variables of the function.
	 * @return		Number of inputs.
	 */
	int getInputCount();

	/** Indicates the number of values returned by the function.
	 * @return		Number of outputs.
	 */
	int getOutputCount();

	/** Indicates whether or not the function is differentiable.
	 * @return		<code>true</code> if it differentiable, <code>false</code>
	 *               otherwise.
	 */
	boolean isDifferentiable();

	/** Indicates whether or not the function is twice differentiable.
	 * @return		<code>true</code> if it twice differentiable,
	 *              <code>false</code> otherwise.
	 */
	boolean isTwiceDifferentiable();

	/** Indicates whether or not the function output depends solely of the
	 * current input (and not of the previous pattern it has processed).
	 * (Any such internal state use for next computation should be in transient
	 * variables and not being serialized. They should be reseted on reset.)
	 * @return    <code>true</code> if output depends solely on current input
	 *            <code>false</code> otherwise.
	 */
	boolean isStateless();

	/*********************************************************************/
    //Inner classes

	/** Return type for the method {@link #processPattern}.
	 */
	class ProcessPatternResult
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
		/** Creates a result holding nothing.
		 */
		public ProcessPatternResult() {};
		/** Creates a result holding the output pattern.
		 *  @param      newOutputPattern        The output pattern to be hold.
		 */
		public ProcessPatternResult(double[] newOutputPattern)
		{
			outputPattern = newOutputPattern;
		}
		/** Creates a result holding the output pattern and the derivative.
		 *  @param      newOutputPattern        The output pattern to be hold.
		 *  @param      newDerivative           The derivatve to be hold.
		 */
		public ProcessPatternResult(double[] newOutputPattern,
									double[][] newDerivative)
		{
			outputPattern = newOutputPattern;
			derivative = newDerivative;
		}

		/** Creates a result holding the output pattern and the derivative.
		 *  @param      newOutputPattern        The output pattern to be hold.
		 *  @param      newDerivative           The derivatve to be hold.
		 *  @param      newSecondDerivative     The second derivatve to be hold.
		 */
		public ProcessPatternResult(double[] newOutputPattern,
									double[][] newDerivative,
									double[][][] newSecondDerivative)
		{
			outputPattern = newOutputPattern;
			derivative = newDerivative;
			secondDerivative = newSecondDerivative;
		}
	}

	/*********************************************************************/
    //Methods

	/** Reset internal transient state for non stateless functions. */
	public void reset();

	/** Processes an input pattern and returns its output pattern and
	 *  derivative (if requested).
	 * @param		InputPattern			The input pattern.
	 * @param		ComputeDerivative		Must be <code>true</code> if the
	 *                                      derivative should be computed.
	 * @param		ComputeSecondDerivative	Must be <code>true</code> if the
	 *                                      second derivative should be computed.
	 * @return		The output pattern and the derivative (if requested).
	 */
	ProcessPatternResult processPattern(double[] inputPattern,
	                                    boolean computeDerivative,
										boolean computeSecondDerivative);

	/** Processes a set of input patterns and record the requested information.
	 *  To get the derivatives for each pattern, use the keyword
	 *  {@link DataNames#DERIVATIVES}, for the second derivatives use
	 *  {@link DataNames#SECOND_DERIVATIVES} and for the error patterns, use
	 *  {@link DataNames#ERROR_PATTERNS}. Derivatives are available only when
	 *  the function is differentiable. Error patterns required
	 *  {@link DataNames#TARGET_PATTERNS} to be in the provided data set.
	 * @param		DataSet					The data set holding at least
	 *                                      {@link DataNames#INPUT_PATTERNS} and
	 *                                      {@link DataNames#PATTERN_COUNT}.
	 * @param		RecordList				A list of keywords (usually
	 *                                      {@link DataNames} constants) of
	 *                                      things to record.
	 * @return		The given data set augmented with the
	 *              {@link DataNames#OUTPUT_PATTERNS} and any other keywords
	 *              supported requested. Each <code>FunctionalUnit</code>
	 *              may provide extra keywords of its own.
	 * @see DataNames
	 */
	DataSet processDataSet(DataSet dataSet,  String[] recordList);

	/*********************************************************************/
    //Cloneable interface

    Object clone() throws CloneNotSupportedException;

}

//TODO Add isVertible property and corresponding function
//TODO Add functional structure locking mechanism and exception
//TODO Add functional unit exception