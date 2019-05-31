package lnsc.page;

/** Optional basis for class implementing {@link StateGenerator} interface.
 *  Derived classes constructor must fill the protected variables and implement
 *  {@link #getStates}.
 *
 * @author Francois Rivest
 * @version 1.0
 */

public abstract class AbstractStateGenerator implements StateGenerator {

	/*********************************************************************/
    //Private fields

	/** Value returned by {@link StateGenerator#isDeterministic}. Default = true.*/
	protected boolean m_IsDeterministic = true;

	/*********************************************************************/
	//Interface implementation

    public boolean isDeterministic() {return m_IsDeterministic;}

}