package lnsc;
import java.io.*;
import java.util.*;


/** <P> Container for input patterns set and all relevant information such as
 *  output patterns set, target patterns set, error patterns set, internal
 *  values and others. Objects are stored and retrieved in it using names
 *  assigned to them. Basically any kind of data can be placed in it, but a
 *  certain number of names are reserved for some specific data. </P>
 *
 *  @see DataNames
 *  @see DataSetCollection
 *
 *  @author Francois Rivest
 *  @version 1.0
 *  @since 1.0
 */
public class DataSet implements Cloneable, Serializable//, UserFriendly
{

	/*********************************************************************/
    //Serial Version UID

	/** Serial version UID. */
	static final long serialVersionUID = 5574419997196829459L;

	/*********************************************************************/
	//Private fields

	/** Hash table initial size */
	private static final int INITIAL_SIZE = 20;

	/** Hash table to store datas */
	private Hashtable m_Datas = new Hashtable(INITIAL_SIZE);


	/*********************************************************************/
	//Constructors

	/** Creates an empy data set with initial size INITIAL_SIZE.
	 *
	 */
	public DataSet() {}

	/** Creates an empty data set with given initial size.
	 *  @param      initialSize         The initial size.
	 */
	public DataSet(int initialSize)
	{
		m_Datas = new Hashtable(initialSize);
	}

	/** Creates a data set with initial data into it.
	 *  @param      newDataNames        Array of data names.
	 *  @param      newDatas            Array of corresponding datas.
	 */
	public DataSet(String[] newDataNames, Object[] newDatas)
	{
		if (newDataNames.length != newDatas.length) {
			throw new IllegalArgumentException("The number of datas and data names must match!");
		}

		for (int i=0; i<newDataNames.length; i++)
		{
			setData(newDataNames[i], newDatas[i]);
		}
	}

	/** Creates a data set with the same refered data as another one.
	 *  @param      newDataSet          The data set from which to take references.
	 */
	public DataSet(DataSet newDataSet)
	{
		addDataSet(newDataSet, true);
	}

	/*********************************************************************/
	//Properties/Methods

	/** Returns a list of the names of the data contained in the set.
	 * @return		List of the names of data in the set.
	 */
	public String[] dataNamesList()
	{
		int i, count = m_Datas.size();
		String[] ret = new String[count];
		Enumeration enum = m_Datas.keys();

		//place all keywords in the array of string
		for (i=0; i<count; i++)
		{
			ret[i] = (String) enum.nextElement();
		}

		//return it
		return ret;
	}

	/** Returns the number of items in the data set.
	 *  @return     Number of items in the data set.
	 */
	public int getDataCount()
	{
		return m_Datas.size();
	}

	/** Returns the data stored under a given name.
	 * @param		dataName			Name of the data to be retrieved.
	 * @return		The data stored under the given name.
	 */
	public Object getData(String dataName)
	{
		return m_Datas.get(dataName);
	}

	/** Stores some data under a given name.
	 * @param		dataName			Name under which the data will be stored.
	 * @param		data				Data to be stored.
	 */
	public void setData(String dataName, Object data)
	{
		m_Datas.put(dataName, data);
		//updateWindow();
	}

	/** Removes the data stored under a given name and returns it.
	 * @param		dataName			Name of the data to remove.
	 * @return		The removed data.
	 */
	public Object removeData(String dataName)
	{
		Object temp = m_Datas.remove(dataName);
		//updateWindow();
		return temp;
	}

	/** Removes all the data in the set except the desired ones.
	 * @param		keptList			List of data names to kept.
	 */
	public void removeAllBut(String[] keepList)
	{
		int i,j;
		boolean keep;
		//first gather a list of existing data
		String dataList[] = dataNamesList();
		//for all data in the dataset
		for (i=0; i<dataList.length; i++)
		{
			//check whether is must be kept
			keep = false;
			for (j=0; j<keepList.length; j++)
			{
				if (dataList[i].compareTo(keepList[j]) == 0) {
					keep = true;
					break;
				}
			}
			//if not, remove it
			if (!keep) {
				m_Datas.remove(dataList[i]);
			}
		}
		//update GUI
		//updateWindow();
	}

	/** Changes the name under which some data is stored.
	 * @param		oldDataName			Name associate to the data.
	 * @param		newDataName			New name to associate to the data.
	 * @return      The data associated to the name change.
	 */
	public Object renameData(String oldDataName, String newDataName)
	{
		Object temp;
		//first check if oldDataName if valid
		if (!m_Datas.containsKey(oldDataName))
		{
			return null;
		}
		//if so rename it
		else
		{
			temp = m_Datas.put(newDataName, m_Datas.remove(oldDataName));
			//updateWindow();
			return temp;
		}
	}

	/** Indicates whether the set contains data under a given name.
	 * @param		dataName			The name to verify if some data is
	 *                                  attached to it.
	 * @return      <code>true</code> if there is some data under the given name.
	 */
	public boolean hasData(String dataName)
	{
		return m_Datas.containsKey(dataName);
	}

	/** Adds the content of a second data set to the data set.
	 *  @param      dataSet             The data set to add.
	 *  @param      overwrite           When <code>true</code>, data already
	 *                                  existing is overwritten, otherwise, it
	 *                                  the data is not added.
	 */
	public void addDataSet(DataSet dataSet, boolean overwrite)
	{
		String[] dataList = dataSet.dataNamesList();

		for (int i=0; i<dataList.length; i++)
		{
			if (overwrite) {
				if (hasData(dataList[i])) {
					System.err.println("Overwriting " + dataList[i]);
				}
				setData(dataList[i], dataSet.getData(dataList[i]));
			} else {
				if (!hasData(dataList[i])) {
					setData(dataList[i], dataSet.getData(dataList[i]));
				} else {
					System.err.println(dataList[i] + " skipped");
				}
			}
		}

	}

	/** Adds the content of a second data set to the data set. No warning is given.
	 *  @param      dataSet             The data set to add.
	 *  @param      overwrite           When <code>true</code>, data already
	 *                                  existing is overwritten, otherwise, it
	 *                                  the data is not added.
	 */
	public void addDataSetww(DataSet dataSet, boolean overwrite)
	{
		String[] dataList = dataSet.dataNamesList();

		for (int i=0; i<dataList.length; i++)
		{
			if (overwrite) {
				setData(dataList[i], dataSet.getData(dataList[i]));
			} else {
				if (!hasData(dataList[i])) {
					setData(dataList[i], dataSet.getData(dataList[i]));
				}
			}
		}

	}
	/*********************************************************************/
	//toString method

	public String toString()
	{
		String ret = "";
		ret += "\tClass: DataSet\n";
		//Datas
		String[] names = dataNamesList();
		for (int i=0; i<names.length; i++)
		{
			ret += '\t' + names[i];
		    if (m_Datas.get(names[i]) instanceof double[][]) {
				ret += '\n' + Tools.tabText(LinearAlgebra.toString((double[][]) m_Datas.get(names[i])),2);
			} else if (m_Datas.get(names[i]) instanceof double[]) {
			    ret += '\n' + Tools.tabText(LinearAlgebra.toString((double[]) m_Datas.get(names[i])),2);
			} else {
				ret += '\n' + Tools.tabText(m_Datas.get(names[i]).toString(),2);
			}
			if (i < names.length-1) {
				ret += '\n';
			}
		}
		//return
		return ret;
	}

	/*********************************************************************/
	//UserFriendly interface implementation
	/*
	private transient DataSetWindow m_Window;

	public void openWindow()
	{
		if (m_Window == null) {
			m_Window = new DataSetWindow(this);
		}
		m_Window.setSize(300,300);
		m_Window.setTitle("DataSet " + Integer.toString(this.hashCode()));
		m_Window.show();
	}

	public void updateWindow()
	{
		if (m_Window != null) {
			m_Window.refreshData();
		}
	}

	public void closeWindow()
	{
		if (m_Window != null) {
			m_Window.dispose();
		}
	}
	*/

}


//TODO Rewrite it with more modern structure
//TODO move 'isMember'