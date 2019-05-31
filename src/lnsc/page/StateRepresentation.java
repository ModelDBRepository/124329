package lnsc.page;
import java.io.Serializable;

/** State representation converter, use to convert a state into a real-vector.
 *
 * <p>State representations are assumed serializable! Episode state information
 * should be transient (only information about how to generate the
 * representation)!</p>
 *
 * @author Francois Rivest
 * @version 1.3
 */

public interface StateRepresentation extends Serializable {

    /** Converts the state into a vector of real based on specific representation.
	 *  @param      s     State to be converted.
	 *  @return     Real vector representation of the state.
	 */
	public double[] getRepresentation(State s);

	/** Indicates the number of values in the state representation.
	 *  @return    Number of outputs.
	 */
	public int getOutputCount();

	/** Indicates whether or not the function output depends solely of the
	 * current state (and not of the previous state is has processed).
	 * (Any such internal state use for next computation should be in transient
	 * variables and not being serialized. They should be reseted on reset.)
	 * @return    <code>true</code> if output depends solely on current input
	 *            <code>false</code> otherwise.
	 */
	boolean isStateless();

	/** Reset internal transient state for non stateless functions. */
	public void reset();

}
