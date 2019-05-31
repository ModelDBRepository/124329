package lnsc.page;

/** Optional basis for class implementing {@link StateRepresentation} interface.
 *  Derived classes constructor must fill the protected variables and implement
 *  {@link #getRepresentation}.
 *
 * <p>State representations are assumed serializable! Episode state information
 * should be transient (only information about how to generate the
 * representation)!</p>
 *
 * @author Francois Rivest
 * @version 1.3
 */


public abstract class AbstractStateRepresentation implements StateRepresentation {

	/*********************************************************************/
	//Serial Version UID

	/** Serial version UID. */
	static final long serialVersionUID = -8269095195759502297L;

	/*********************************************************************/
	//Private fields

	/** Value returned by {@link StateRepresentation#getOutputCount}.
	 * No default (must be specified).*/
	protected int m_OutputCount;

	/** Value returned by {@link StateRepresentation#isStateless}.
	 * Default is true, otherwise reset() must be override.*/
	protected boolean m_IsStateless = true;

	/*********************************************************************/
	//Interface implementation

	public int getOutputCount() {return m_OutputCount;}

	public boolean isStateless() {return m_IsStateless;}

	public void reset() {
		if (!m_IsStateless) {
			throw new java.lang.UnsupportedOperationException("reset() not implemented for !isStateless StateRepresentation!");
		}
	}

}