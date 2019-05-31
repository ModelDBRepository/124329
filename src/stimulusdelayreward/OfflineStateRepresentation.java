package stimulusdelayreward;

import lnsc.page.*;


/**
 * State representation that is processed elsewhere before being called by ...
 *
 * This class is not good practice.
 *
 * @author Francois Rivest
 * @version 1.0
 */

public class OfflineStateRepresentation extends AbstractStateRepresentation {

    /*********************************************************************/
    //Serial Version UID

    /** Serial version UID. */
    static final long serialVersionUID = -156664253264716318L;

    /*********************************************************************/
    //Private fields (current state)

    /** Off-line representation. */
    protected transient double[] m_Rep;

    /*********************************************************************/
    //Constructors

    /** Creates an offlined process representation.
     * @param    count    Representation length.
     */
    public OfflineStateRepresentation(int count) {
        m_OutputCount = count;
        m_Rep = new double[count];
    }

    /*********************************************************************/
    //Interface implementation

    public double[] getRepresentation(State s) {
        return m_Rep;
    }

    /*********************************************************************/
    //Methods

    public void setRep(double[] rep)
    {
        if (rep.length != m_OutputCount) {
            throw new IllegalArgumentException("Invalid representation length!");
        }
        m_Rep = rep;
    }

}