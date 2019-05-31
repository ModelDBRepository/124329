package lnsc.unsup;

/** Defines the interface for online supervised learning algorithm.
 *
 * @author Francois Rivest
 * @version 1.0
 */

public interface OnlineUnsupervisedLearning {

  /** Train whatever associate functions it as for a single patterns. */
  public void train(double[] inputPattern);


}