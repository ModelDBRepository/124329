package lnsc;

/** <P> A sigmoidal logistic unit. Principally used as unit in neural networks.
 *  It has the form:
 *  <code>f(x) = factor*(1/(1+exp(-(mu-x)/beta)))) + offset</code> or:
 *  <code>f(x) = factor*(1/(1+exp(-alpha*(mu-x)))) + offset</code>.
 *  <code>mu</code> is called the half-maximum point of the function and
 *  <code>beta</code> is called the slope. </P>
 *
 *  @author Francois Rivest
 *  @version 1.0
 *  @since 1.0
 */
public final class LogisticUnit extends AbstractSimpleUnit
{
	/*********************************************************************/
    //Serial Version UID

	/** Serial version UID. */
	static final long serialVersionUID = -2690716200172087221L;

	/*********************************************************************/
	//Private fields

	/** Alpha property data. */
	private double m_Alpha = 1.0;

	/** Beta property data. */
	private double m_Beta = 1.0;

	/** Mu property data. */
	private double m_Mu = 0.0;

	/*********************************************************************/
	//Constructors

	/** Creates a simple logistic unit with Factor=1.0 and Offset=0.0,
	 *  Mu=0.0 and Beta=1.0. That is, with output range between 0.0 and 1.0.
	 */
	public LogisticUnit()
	{
		m_IsDifferentiable = true;
        m_IsTwiceDifferentiable = true;
	}

	/** Creates a simple logistic unit with given factor and offset and
	 *  Mu=0.0 and Beta=1.0. That is, with range between Offset and
         *  Factor+Offset.
	 *  @param      newFactor           Function factor.
	 *  @param      newOffset           Function offset.
	 */
	public LogisticUnit(double newFactor, double newOffset)
	{
		super(newFactor, newOffset);
		m_IsDifferentiable = true;
        m_IsTwiceDifferentiable = true;
	}

	/** Creates a simple logistic unit with given factor, offset,
	 *  half-maximum point (Mu) and slope (beta).
	 *  That is, with range between Offset and Factor+Offset.
	 *  @param      newFactor           Function factor.
	 *  @param      newOffset           Function offset.
	 *  @param      newMu               Half-maximum point.
	 *  @param      newBeta             Slope, must be positive.
	 */
	public LogisticUnit(double newFactor, double newOffset,
	                    double newMu, double newBeta)
	{
		this(newFactor, newOffset);
		setMu(newMu);
		setBeta(newBeta);

	}

	/*********************************************************************/
	//Properties

	/** Returns the current value of parameter alpha.
	 *  Alpha = 1/Beta
	 *  @return     Value of alpha.
	 */
	public final double getAlpha() {return m_Alpha;}

	/** Sets the current value of parameter alpha.
	 *  Alpha = 1/Beta
	 *  @param      newAlpha        The new value of alpha, must be positive.
	 */
	public final void setAlpha(double newAlpha)
	{
		if (newAlpha <= 0.0) {
			throw new IllegalArgumentException("Alpha must positive!");
		} else {
			m_Alpha = newAlpha;
			m_Beta = 1.0 / m_Alpha;
		}
	}

	/** Returns the current value of parameter beta.
	 *  Beta is called the slope of the function.
	 *  @return     Value of beta.
	 */
	public final double getBeta() {return m_Beta;}

	/** Sets the current value of parameter beta.
	 *  Beta is called the slope of the function.
	 *  @param      newBeta         The new value of beta, must be positive.
	 */
	public final void setBeta(double newBeta)
	{
		if (newBeta <= 0.0) {
			throw new IllegalArgumentException("Beta must be positive!");
		} else {
			m_Beta = newBeta;
			m_Alpha = 1.0 / m_Beta;
		}
	}

	/** Returns the current value of parameter mu.
	 *  Mu is the half-maximum point of the logistic function.
	 *  @return     Value of mu.
	 */
	public final double getMu() {return m_Mu;}

	/** Sets the current value of parameter mu.
	 *  Mu is the half-maximum point of the logistic function.
	 *  @param      newMu         The new value of mu.
	 */
	public final void setMu(double newMu) {m_Mu = newMu;}

	/*********************************************************************/
	//AbstractSimpleUnit implementation

	protected final double function(double x)
	{
		if (Math.abs(m_Beta) > 1.0) {
			return 1.0 / (1.0 + Math.exp((m_Mu - x) / m_Beta));
		} else {
			return 1.0 / (1.0 + Math.exp((m_Mu - x) / m_Beta));
		}
	}

	protected final double functionDerivative(double x)
	{
		double y;

		if (Math.abs(x) > 25.0) return 0.0;

		y = Math.exp((m_Mu - x) / m_Beta);

		if (Math.abs(m_Beta) > 1.0) {
			return 	y / ((1.0 + y) * (1.0 + y) * m_Beta);
		} else {
			return (y * m_Alpha) / ((1.0 + y) * (1.0 + y));
		}
	}

    protected final double functionSecondDerivative(double x)
    {
        double y = Math.exp(-(-m_Mu + x) / m_Beta);
        return ((y) * (y - 1.0))
			/  ((Math.pow(1.0 + y, 3.0)) * (m_Beta * m_Beta));

    }

	/*********************************************************************/
	//toString method

	public String toString()
	{
		String ret = super.toString() + "\n";
		ret += "Class: LogisticUnit\n";
		ret += "\tAlpha: " + Double.toString(m_Alpha) + "\n";
		ret += "\tBeta: " + Double.toString(m_Beta) + "\n";
		ret += "\tMu: " + Double.toString(m_Mu);
		return ret;
	}


}


//TODO parameter checking??