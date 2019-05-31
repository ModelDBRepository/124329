package lnsc.page;
import lnsc.DataSet;
import java.util.Enumeration;

/** Optional basis for class implementing {@link State} interface. Derived
 *  classes constructor must fill the protected variables. Add necessary state
 *  information and properties. By default do, undo and clone are not supported,
 *  but can be added in derived classes. When setting IsCloneable to true,
 *  basic cloning is automatically inherited from Object. By default states are
 *  observable, and hence getObservableStates return itself. Default
 *  implementation for {@link #getActionsEnumerator} uses {@link #getActions}.
 *
 * @author Francois Rivest
 * @version 1.2
 */

public abstract class AbstractState implements State {

	/*********************************************************************/
	//Private fields

	/** Value returned by {@link State#supportsDo}. Default = false.*/
	protected boolean m_SupportsDo = false;

	/** Value returned by {@link State#supportsUndo}. Default = false.*/
	protected boolean m_SupportsUndo = false;

	/** Value returned by {@link State#isCloneable}. Default = false.*/
	protected boolean m_IsCloneable = false;

	/** Value returned by {@link State#geValue}. Default = 0 (non-final).*/
	protected double m_Value = 0;

	/** Value returned by {@link State#isObservable}. Default = true (observable).*/
	protected boolean m_IsObservable = true;

	/** Value returned by {@link State#isValid}. Default = false.*/
	protected boolean m_IsFinal = false;

	/** Value returned by {@link State#isValid}. Default = true.*/
	protected boolean m_IsValid = true;

	/** Value returned by {@link State#geDefault}. Default = 1 (deterministic).*/
	protected double m_Probability = 1;

	/** Value returned by {@link State#isDeterministic}. Default = true.*/
	protected boolean m_IsDeterministic = true;

	/** Value returned by {@link Action#areFixed}. Default = false (count = -1).*/
	protected boolean m_AreActionsFixed = false;

	/** Value returned by {@link ActionGenerator#getActionCount}. Default = -1 (not fixed).*/
	protected int m_ActionCount = -1;


	/*********************************************************************/
	//Interface implementation

	public boolean supportsDo() {return m_SupportsDo;}

	public boolean supportsUndo() {return m_SupportsUndo;}

	public boolean isCloneable() {return m_IsCloneable;}

    public double getValue() {return m_Value;}

	public boolean isObservable() {return m_IsObservable;}

    public boolean isFinal() {return m_IsFinal;}

    public boolean isValid() {return m_IsValid;}

    public double getProbability() {return m_Probability;}

	public boolean isDeterministic() {return m_IsDeterministic;}

	public boolean areActionsFixed() {return m_AreActionsFixed;}

	public int getActionCount() {return m_ActionCount;}

	public Enumeration getActionsEnumerator() {
		return new DefaultActionsEnumerator(getActions());
	}

	public State[] getObservableStates() {return new State[] {this};}

	public void doAction(Action a) {
		throw new UnsupportedOperationException("supportsDo = false");
	}

	public void undoAction() {
		throw new UnsupportedOperationException("supportsUndo = false");
	}

	/*********************************************************************/
	//toDataSet interface

	public DataSet toDataSet() {return null;}

	/*********************************************************************/
	//Cloeanble interface

	public Object clone() {
		if (!m_IsCloneable) {
			throw new UnsupportedOperationException("isCloneable = false");
		} else {
			try {
				return super.clone();
			} catch (CloneNotSupportedException e) {
				throw new java.lang.Error();
			}
		}
	}

	/*********************************************************************/
	//toString method

	public String toString() {
		String ret = "";
		ret += "Class: AbstractState\n";
		ret += "\tSupportsDo: " + m_SupportsDo + "\n";
		ret += "\tSupportsUndo: " + m_SupportsUndo + "\n";
		ret += "\tIsCloneable: " + m_IsCloneable + "\n";
		ret += "\tIsObservable: " + m_IsObservable + "\n";
		ret += "\tIsDeterministic: " + m_IsDeterministic + "\n";
		ret += "\tProbability: " + m_Probability + "\n";
		ret += "\tAreActionsFixed: " + m_AreActionsFixed + "\n";
		ret += "\tActionCount: " + m_ActionCount + "\n";
		ret += "\tIsValid: " + m_IsValid + "\n";
		ret += "\tIsFinal: " + m_IsFinal + "\n";
		ret += "\tValue: " + m_Value;
		return ret;
	}

}