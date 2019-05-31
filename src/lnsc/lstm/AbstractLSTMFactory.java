package lnsc.lstm;

import lnsc.*;

/** <P> Basis to write a Long Short-Term Memory (LSMT) network factory. </P>
 *
 * <P> Basis for Gers, Schraudolph & Schmiduber 2002 (from Journal of Machine
 * Learning Research 3:115-143) LSTM network. See also Gers, Schmidhuber, &
 * Cummins 2000 (from Neural Computation, 12(10) 2451-2471) and Hochreiter &
 * Schmidhuber 1997 (from Neural Computation 9(8):1735-1780). </P>
 *
 * <P> This basic factory comes with some default value for every properties,
 * connectivity, or activation function, that can be customized in the class
 * FastLSTMNetwork and the FastLSTMMemoryblocks it contains. These properties
 * are read from protected fields when it time to create a FastLSTMNetwork
 * and than calls the abstract initializeWeights() method before returning the
 * newly created network. </P>
 *
 * <P> Derived factories simply have to provide their own constructors from
 * which they saved their curtomized properties into the protected field of
 * AbstractLSTMFactory, the initializeWeights() method, and their javadoc. </P>
 *
 *  @see FastLSTMNetwork
 *  @see LSTMFactory
 *
 *  @author Francois Rivest
 *  @version 1.0
 *  @since 1.0
 */


public abstract class AbstractLSTMFactory implements FunctionalUnitFactory {

    /*********************************************************************/
    //Serial Version UID

    /** Serial version UID. */
    //static final long serialVersionUID = ;

    /*********************************************************************/
    //Private fields (memory block)

    /** Number of memory cells per memory block. */
    protected int m_CellperBlock;

    /** Memory block input gate processing function (in). */
   protected FunctionalUnit m_InputGate = new LogisticUnit(1, 0);

    /** Memory block forget gate processing function (fgt). */
    protected FunctionalUnit m_ForgetGate = new LogisticUnit(1, 0);

    /** Memory block output gate processing function (out). */
    protected FunctionalUnit m_OutputGate = new LogisticUnit(1, 0);

    /** Memory cell first processing function (g). */
    protected FunctionalUnit m_g = new LogisticUnit(2,-1);

    /** Memory cell second processing function (h). */
    protected FunctionalUnit m_h = new LogisticUnit(2,-1);

    /*********************************************************************/
    //Private fields (network)

    /** Number of input to the network. */
    protected int m_InputCount;

    /** Number of memory block. */
    protected int m_BlockCount;

   /** Number of output of the network. */
    protected int m_OutputCount;

    /** Sample of an output function. */
    protected FunctionalUnit m_SampleOutput;

    /** Connects block gates to block. */
    protected boolean m_GateToGate;

    /** Connects bias to output layer. */
    protected boolean m_BiasToOutput;

    /** Connects input to output layer. */
    protected boolean m_InputToOutput;

    /** Connects block gates to output layer. */
    protected boolean m_GateToOutput;

    /** Output layer local gradient factor. */
    protected double m_OutputWeightsLocalGradientFactor = 1.0;

    /*********************************************************************/
    //Constructors

    //Force packages
    protected AbstractLSTMFactory() {}

    /*********************************************************************/
    //Helpers

    /** Function used to initialize a new network weights.
     * @param   newNet   The new network to initialize teh weights.
     */

    protected abstract void initializeWeights(FastLSTMNetwork newNet);

    /*********************************************************************/
    //FunctionalUnitFactory interface implementation

    public FunctionalUnit createUnit() {
        FastLSTMNetwork newNet = new FastLSTMNetwork(
            m_InputCount,
            m_BlockCount,
            m_CellperBlock,
            m_g,
            m_h,
            m_InputGate,
            m_ForgetGate,
            m_OutputGate,
            m_OutputCount,
            m_SampleOutput,
            m_GateToGate,
            m_BiasToOutput,
            m_InputToOutput,
            m_GateToOutput);
         newNet.setOutputWeightsLocalGradientFactor(m_OutputWeightsLocalGradientFactor);
        initializeWeights(newNet);
        return newNet;
    }

}