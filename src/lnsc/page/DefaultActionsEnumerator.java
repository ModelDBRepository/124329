package lnsc.page;
import java.util.*;

/** Creates an actions enumator based on an array of actions.
 *
 * @author Francois Rivest
 * @version 1.1
 */


public final class DefaultActionsEnumerator implements Enumeration {

	/*********************************************************************/
    //Private fields

	/** Current action index. */
	private int m_Index;

	/** Array of actions. */
	private Action[] m_Actions;

	/*********************************************************************/
	//Constructors

	/** Creates an action enumerator based on an array of actions.
	 * @param  newActions    Array of actions.
	 */
	public DefaultActionsEnumerator(Action[] newActions)
	{
		m_Actions = newActions;
    }

	/*********************************************************************/
	//Interface implementation

    public boolean hasMoreElements()
	{
		if (m_Index == m_Actions.length) {
			return false;
		} else {
			return true;
		}
    }

    public Object nextElement()
	{
		if (!hasMoreElements()) {
			throw new NoSuchElementException();
		}
		Action a = m_Actions[m_Index];
		m_Index++;
		return a;
    }

}