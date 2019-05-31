package lnsc.lstm;

import lnsc.DataNames;

/**
 * <p>Title: LSTM</p>
 * <p>Description: Long Short-Term Memory </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: UdeM</p>
 * @author Francois Rivest
 * @version 1.0
 */

public class LSTMDataNames extends DataNames {

    /** Make class non constructible. */
    protected LSTMDataNames() {}

    /** A <code>double[c]</code> array in FastLSTMMemoryBlock representing the
     * activity in the <i>c</i> memory cells. A <code>double[c*m]</code> array
     * in FastLSTMNetwork representing the concatenation of the internal
     * activity of the memory cells of the <i>b</i> memory blocks in order.
     */
    public static final String LSTM_INTERNAL_STATES = "LSTMInternalStates";

    /** A <code>double[b*c]</code> array in FastLSTMNetwork representing the
     * concatenation of the <i>b</i> memory block <i>c</i> cell's output
     * activity in order.
     */
    public static final String LSTM_INTERNAL_ACTIVATIONS = "LSTMInternalActivations";

    /** A <code>double[b]</code> array in FastLSTMNetwork representing the
     * activity of the input gates of the <i>b</i> memory blocks in order.
     */
    public static final String LSTM_INPUT_GATES = "LSTMInputGates";

    /** A <code>double[b]</code> array in FastLSTMNetwork representing the
     * activity of the forget gates of the <i>b</i> memory blocks in order.
     */
    public static final String LSTM_FORGET_GATES = "LSTMForgetGates";

    /** A <code>double[b]</code> array in FastLSTMNetwork representing the
     * activity of the output gate of the <i>b</i> memory blocks in order.
     */
    public static final String LSTM_OUTPUT_GATES = "LSTMOutputGates";

}