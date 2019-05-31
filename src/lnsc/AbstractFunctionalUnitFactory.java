package lnsc;

/** Basic internal functionalities for FunctionalUnit factories based on
 *  units prototypes.
 *
 * @author Francois Rivest
 * @version 1.0
 */


public abstract class AbstractFunctionalUnitFactory implements FunctionalUnitFactory {

	/*********************************************************************/
    //Serial Version UID

	/** Serial version UID. */
	//static final long serialVersionUID = ;

	/*********************************************************************/
	//Private fields

	/** Prototype of the unit to create. */
	protected FunctionalUnit m_PrototypeUnit;

}