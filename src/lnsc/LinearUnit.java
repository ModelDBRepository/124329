package lnsc;


/** <P> A univariate single-valued linear function. Principally used as input
 *  in neural networks. It has the form: <code>f(x) = factor*x + offset</code>.
 *  </P>
 *
 *  @author Francois Rivest
 *  @version 1.0
 *  @since 1.0
 */
public final class LinearUnit extends AbstractSimpleUnit
{
	/*********************************************************************/
    //Serial Version UID

	/** Serial version UID. */
	static final long serialVersionUID = -3902126060060618664L;

	/*********************************************************************/
	//Constructors

	/** Creates a simple linear unit with Factor=1.0 and Offset=0.0. */
	public LinearUnit()
	{
		m_IsDifferentiable = true;
		m_IsTwiceDifferentiable = true;
	}

	/** Creates a simple linear unit with given factor and offset.
	 *  @param      newFactor           Function factor.
	 *  @param      newOffset           Function offset.
	*/
	public LinearUnit(double newFactor, double newOffset)
	{
		super(newFactor, newOffset);
		m_IsDifferentiable = true;
		m_IsTwiceDifferentiable = true;
	}

	/*********************************************************************/
	//AbstractSimpleUnit implementation

	protected final double function(double x) {return x;}

	protected final double functionDerivative(double x) {return 1.0;}

	protected final double functionSecondDerivative(double x) {return 0.0;}

	/*********************************************************************/
	//toString method

	public String toString()
	{
		String ret = super.toString() + "\n";
		ret += "Class: LinearUnit";
		return ret;
	}

}
