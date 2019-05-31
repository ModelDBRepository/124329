package lnsc.page;
import lnsc.DataSet;
import java.util.Observable;

/** Optional basis for class implementing {@link Agent} interface. Derived
 *  classes constructor must fill the protected variables. Add necessary Agent
 *  information and properties. In general setEvalMode should be overrided.
 *  Same as {@link AbstractAgent}, but derived from java.util.Observer.
 *
 *  <P> Agent could themselves notify their oberver by the end ot the {@link
 *  #requestAction} process, or should at least call {@link #setChanged()}.
 *  {@link notifyObservers() send a {@link #toDataSet} description by default.
 *  </P>
 *
 *  <p>Agents are assumed serializable! Episode state information should be
 *  transient! The observers list is not!</p>
 *
 * @author Francois Rivest
 * @version 1.3
 */


public abstract class AbstractObservableAgent extends Observable implements Agent {


	/*********************************************************************/
	//Serial Version UID

	/** Serial version UID. */
	static final long serialVersionUID = 8791326209624732464L;

	/*********************************************************************/
	//Private fields

	/** Value returned by {@link Agent#getEvalMode}. Default = false.*/
	protected boolean m_EvalMode = false;

	/** Value returned by {@link Agent#isEvaluable}. Default = true.*/
	protected boolean m_IsEvaluable = true;

	/** Value returned by {@link Agent#isAdaptive}. Default = false.*/
	protected boolean m_IsAdaptive = false;

	/*********************************************************************/
	//Interface implementation

	public void setEvalMode(boolean newEvalMode) {
		if (newEvalMode && !m_IsEvaluable) {
			throw new UnsupportedOperationException("isEvaluable = false");
		}
		m_EvalMode = newEvalMode;
	}

	public boolean getEvalMode() {return m_EvalMode;}

	public boolean isEvaluable() {return m_IsEvaluable;}

	public boolean isAdaptive() {return m_IsAdaptive;}

	/*********************************************************************/
	//toDataSet interface

	public DataSet toDataSet() {return null;}

	/*********************************************************************/
	//Observable Interface implementation

	public void notifyObservers() {notifyObservers(toDataSet());}

}