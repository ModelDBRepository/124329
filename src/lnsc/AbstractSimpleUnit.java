package lnsc;
import java.util.*;

/** <P> Abstract class containing the basic implementation for simple
 *  univariate single-real-valued <code>FunctionalUnit</code>. These functions
 *  will have the form <code>f'(x) = factor*f(x) + offset</code> where
 *  <code>f(x)</code> needs to be implemented. </P>
 *
 * <P> This class encapsulates all the methods and slots required to create a
 * functional unit of 1 input and 1 output from the class
 * <code>AbstractFunctionUnit</code>.  It is the fastest way to create such
 * units. Only 3 simple things are needed by subclasses: </P>
 *  <ol>
 *      <li>In the constructor, the field <code>m_IsDifferentiable</code> must
 *          be filled appropriately. (<code>m_IsStateless</code> is assumed
 *          <code>true</code>)</li>
 *      <li>Only the simple methods <code>function(double)</code>, and
 *          <code>functionDerivative(double)</code> if the function is
 *          differentiable, need to be implemented.
 *      <li>Since <code>FunctionalUnit</code> are <code>Serializable</code> and
 *          <code>Cloneable</code>, any required extra code to make these
 *          interfaces work properly should be added. It is necessary to at
 *          least set the <code>private static serialVersionUID</code> variable
 *          appropriately for the <code>Seriablizable</code> interface. For
 *          complex object, the <code>Cloneable</code> interface can rely on
 *          <code>Tools.copyObject(Serializable)</code>. </li>
 *  </ol>
 *
 * <P>Moreover, every function derived from <code>AbstractSimpleUnit</code> can
 * also be re-scaled and offset to match any specific output range. The output
 * computed by the original <code>function(double)</code> can be multiplied by
 * <code>factor</code> and then translated by adding <code>offset</code>. The
 * <code>functionDerivative(double)</code> is adjusted accordingly. By default
 * <code>factor = 1.0</code> and <code>offset = 0.0</code>. </P>
 *
 *  @author Francois Rivest
 *  @version 1.0
 *  @since 1.0
 */
public abstract class AbstractSimpleUnit extends AbstractFunctionalUnit
{
	/*********************************************************************/
    //Serial Version UID

	/** Serial version UID. */
	static final long serialVersionUID = 5215067520421445273L;

	/*********************************************************************/
	//Private fields

	/** Factor property data. */
	private double m_Factor = 1.0;

	/** Offset property data. */
	private double m_Offset = 0.0;

	/*********************************************************************/
	//Constructors

	/** Creates a simple unit with Factor=1.0 and Offset=0.0. */
	public AbstractSimpleUnit()
	{
		m_InputCount = 1;
		m_OutputCount = 1;
		m_IsStateless = true;
	}

	/** Creates a simple unit with given factor and offset.
	 *  @param      newFactor           Function factor.
	 *  @param      newOffset           Function offset.
	*/
	public AbstractSimpleUnit(double newFactor, double newOffset)
	{
		this();
		setFactor(newFactor);
		setOffset(newOffset);
	}

	/*********************************************************************/
	//

	/** Univariate real-valued function to implement.
	 *  @param      x                   The input.
	 *  @return     The output (that will be scaled and offset).
	 */
	protected abstract double function(double x);

	/** Derivative of the univariate real-valued function implemented.
	 *  It has to be implemented only if the function is differentiable,
	 *  otherwise an empty method returning 0.0 should be provided.
	 *  @param      x                   The input.
	 *  @return     The derivative (that will be scaled).
	 */
	protected abstract double functionDerivative(double x);

	/** Second derivative of the univariate real-valued function implemented.
	 *  It has to be implemented only if the function is twice differentiable,
	 *  otherwise an empty method returning 0.0 should be provided.
	 *  @param      x                   The input.
	 *  @return     The second derivative (that will be scaled).
	 */
	protected abstract double functionSecondDerivative(double x);

	/*********************************************************************/
	//Properties

	/** Returns the value added to the scaled output.
	 *  @return     Value added to the scaled output.
	 */
	public final double getOffset() {return m_Offset;}

	/** Sets the value added to the scaled output.
	 *  @param      newOffset               Value added to the scaled output.
	 */
	public final void setOffset(double newOffset) {m_Offset = newOffset;}

	/** Returns the scaling factor applied to the internal output.
	 *  @return     Factor applied to the internal output.
	 */
	public final double getFactor() {return m_Factor;}

	/** Sets the factor applied to the internal output.
	 *  @param      newFactor               Factor applied to the internal
	 *                                      output.
	 */
	public final void setFactor(double newFactor) {m_Factor = newFactor;}

	/*********************************************************************/
	//FunctionalUnit implementation

	public final FunctionalUnit.ProcessPatternResult processPattern(double[] inputPattern, boolean computeDerivative, boolean computeSecondDerivative)
	{
		//param checking
		FunctionalUnit.ProcessPatternResult ret = preProcessPattern(inputPattern, computeDerivative, computeSecondDerivative);
		//compute output pattern
		ret.outputPattern[0] = m_Factor * function(inputPattern[0]) + m_Offset;
		//compute derivative
		if (computeDerivative)
		{
			ret.derivative[0][0] = m_Factor * functionDerivative(inputPattern[0]);
		}
		//compute derivative
		if (computeSecondDerivative)
		{
			ret.secondDerivative[0][0][0] = m_Factor * functionSecondDerivative(inputPattern[0]);
		}
		//return
		return ret;
	}

	/*********************************************************************/
	//toString method

	public String toString()
	 {
		String ret = super.toString() + "\n";
		ret += "Abstract Class: AbstractSimpleUnit\n";
		ret += "\tOffset: " + Double.toString(m_Offset) + "\n";
		ret += "\tFactor: " + Double.toString(m_Factor);
		return ret;
	 }

}

