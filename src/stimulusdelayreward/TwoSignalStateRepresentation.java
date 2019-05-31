package stimulusdelayreward;

import lnsc.page.*;


/** This class allows automatic conversion of MonkeyObservableState into two
 *  signals vectors.
 *
 * @author Francois Rivest
 * @version 1.0
 */

public class TwoSignalStateRepresentation extends AbstractStateRepresentation {

    /*********************************************************************/
    //Serial Version UID

    /** Serial version UID. */
    static final long serialVersionUID = -5084567201839836122L;

    /*********************************************************************/
	//Constructors

    public TwoSignalStateRepresentation() {
        m_OutputCount = 2;
    }

    public double[] getRepresentation(State s) {
        MonkeyObservableState state = (MonkeyObservableState) s;
        return new double[] {state.getStimulusSignal(), state.getRewardSignal()};
    }

}