package lnsc.page;
import java.util.Enumeration;

/** Optional basis for class implementing {@link ActionGenerator} interface.
 *  Derived classes constructor must fill the protected variables and implement
 *  {@link #getActions} and {@link #getActionsEnumerator}. Default
 *  implementation for {@link #getActionsEnumerator} uses {@link #getActions}.
 *
 * @author Francois Rivest
 * @version 1.1
 */

public abstract class AbstractActionGenerator implements ActionGenerator {

	/*********************************************************************/
	//Private fields

	/** Value returned by {@link ActionGenerator#areFixed}. Default = false (count = -1).*/
	protected boolean m_AreActionsFixed = false;

	/** Value returned by {@link ActionGenerator#getActionCount}. Default = -1 (not fixed).*/
	protected int m_ActionCount = -1;

	/*********************************************************************/
    //Interface implementation

	public Enumeration getActionsEnumerator(State s) {
		return new DefaultActionsEnumerator(getActions(s));
	}

    public boolean areActionsFixed() {return m_AreActionsFixed;}

    public int getActionCount() {return m_ActionCount;}

}