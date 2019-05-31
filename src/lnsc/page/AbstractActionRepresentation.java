package lnsc.page;

/** Optional basis for class implementing {@link ActionRepresentation} interface.
 *  Derived classes constructor must fill the protected variables and implement
 *  {@link #getRepresentation}.
 *
 * <p>Action representations are assumed serializable (only information about
 * how to generate the representation)!</p>
 *
 * @author Francois Rivest
 * @version 1.3
 */


public abstract class AbstractActionRepresentation implements ActionRepresentation {

	/*********************************************************************/
	//Serial Version UID

	/** Serial version UID. */
	static final long serialVersionUID =7985566509368319122L;

	/*********************************************************************/
	//Private fields

	/** Value returned by {@link ActionRepresentation#getOutputCount}.
	 * No default (must be specified).*/
	protected int m_OutputCount;

	/*********************************************************************/
	//Interface implementation

    public int getOutputCount() {return m_OutputCount;}

}