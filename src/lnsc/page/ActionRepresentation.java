package lnsc.page;
import java.io.Serializable;

/** Action representation converter, use to convert an Action into a real-vector.
 *
 * <p>Action representations are assumed serializable (only information about
 * how to generate the representation)!</p>
 *
 * @author Francois Rivest
 * @version 1.3
 */

public interface ActionRepresentation extends Serializable {

	/** Converts the Action into a vector of real based on specific representation.
	 *  @param     a     Action to be converted.
	 *  @return    Real-vector representation of the Action.
	 */
	public double[] getRepresentation(Action a);

	/** Indicates the number of values in the action representation.
	 *  @return    Number of outputs.
	 */
	public int getOutputCount();

}
