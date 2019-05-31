package lnsc.page;
import lnsc.DataSet;

/** Optional basis for class implementing {@link Agent} interface. Derived
 *  classes constructor must fill the protected variables. Add necessary Agent
 *  information and properties. In general setEvalMode should be overrided.
 *
 *  <p>Agents are assumed serializable! Episode state information should be
 *  transient!</p>
 *
 * @author Francois Rivest
 * @version 1.3
 */


public abstract class AbstractAgent implements Agent {


	/*********************************************************************/
	//Serial Version UID

	/** Serial version UID. */
	static final long serialVersionUID = 4877910757366812500L;

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

}