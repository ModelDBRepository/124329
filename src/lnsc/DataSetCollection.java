package lnsc;
import java.io.*;
import java.util.*;
import java.util.Vector;


/** <P> Zero-based ordered collection of data sets containing similar data.
 *  Those data can be directly access throught their name and data set index.
 *  </P>
 *
 *  @see DataNames
 *  @see DataSet
 *
 *  @author Francois Rivest
 *  @version 1.0
 *  @since 1.0
 */
public final class DataSetCollection  implements Cloneable, Serializable
{

	/*********************************************************************/
    //Serial Version UID

	/** Serial version UID. */
	static final long serialVersionUID = 208290656206616903L;

	/*********************************************************************/
	//Private fields

	/** Collection initial size */
	private static final int INITIAL_SIZE = 20;

	/** Hash table to store datas. Elements in it are vectors */
	private Hashtable m_DataCollections = new Hashtable(INITIAL_SIZE);

	/** Number of data set in the collection */
	private int m_DataSetCount = 0;

	/*********************************************************************/
	//Constructors

	/** Creates an empy data set collection with initial size INITIAL_SIZE.
	 *
	 */
	public DataSetCollection() {}

	/** Creates an empty data set collection with given initial size.
	 *  @param      initialSize         The initial size.
	 */
	public DataSetCollection(int initialSize)
	{
		m_DataCollections = new Hashtable(initialSize);
	}

	/** Creates a data set collection with initial data set into it.
	 *  @param      newDataSets         Array of corresponding data sets.
	 */
	public DataSetCollection(DataSet[] newDataSets)
	{
		this();
		for (int i=0; i<newDataSets.length; i++)
		{
		    setDataSet(i, newDataSets[i], true);//here true or false
												//has no effect
		}
	}

	/** Creates a data set collection with the same refered data as another one.
	 *  @param      newDataSet          The data set to put into it.
	 */
	/*	public DataSetCollection(DataSet newDataSet)
	{
		this(new DataSet[] {newDataSet});
	}*/

	/*********************************************************************/
	//Helper methods

	private Object[] toArray(Vector dataCollection)
	{
		Object[] ret = new Object[m_DataSetCount];
		if (dataCollection == null) return ret;
		for (int i=0; i<ret.length; i++)
		{
		    ret[i] = dataCollection.get(i);
		}
		return ret;
	}

	/*********************************************************************/
	//Properties/Methods

	/** Returns a list of the names of the data contained in the set.
	 * @return		List of the names of data in the set.
	 */
	public String[] dataNamesList()
	{
		int i, count = m_DataCollections.size();
		String[] ret = new String[count];
		Enumeration enum = m_DataCollections.keys();

		//place all keywords in the array of string
		for (i=0; i<count; i++)
		{
			ret[i] = (String) enum.nextElement();
		}

		//return it
		return ret;
	}

	/** Returns the number of data items in the data set collection.
	 *  @return     Number of data items in the data set collection.
	 */
	public int getDataCount()
	{
		return m_DataCollections.size();
	}

	/** Returns the number of data sets in the collection.
	 *  @return     Number of data sets in the collection.
	 */
	public int getDataSetCount()
	{
		return m_DataSetCount;
	}

	/** Indicates whether the set contains data collection under a given name.
	 * @param		dataName			The name to verify if some data
	 *                                  collection is attached to it.
	 * @return      <code>true</code> if there is some data under the given name.
	 */
	public boolean hasData(String dataName)
	{
		return m_DataCollections.containsKey(dataName);
	}

	/** Returns the data collection stored under a given name.
	 * @param		dataName			Name of the data collection to be retrieved.
	 * @return		The array of datas stored under the given name.
	 */
	public Object[] getDataCollection(String dataName)
	{
		return toArray((Vector) m_DataCollections.get(dataName));
	}

	/** Returns the data set stored under a given index.
	 * @param		index			    Index of the data set to be retrieved.
	 * @return		The set of datas stored under the given index.
	 */
	public DataSet getDataSet(int index)
	{
		if ((index < 0) | (index > m_DataSetCount-1)) {
			throw new java.lang.IllegalArgumentException("Index must be between 0 and DataSetCount!");
		}

		//Create a data set with the content of what is at that index
		DataSet ret = new DataSet();
		//first gather a list of existing data
		String dataList[] = dataNamesList();
		//for all data in the dataset
		for (int i=0; i<dataList.length; i++)
		{
		    Vector temp = (Vector) m_DataCollections.get(dataList[i]);
			if (temp.get(index) != null) {
				ret.setData(dataList[i], temp.get(index));
			}
		}
		//return it
		return ret;
	}

	/** Returns the data stored under a given name and index.
	 * @param		dataName			Name of the data to be retrieved.
	 * @param       dataindex           Index of the data to be retrieved.
	 * @return		The data stored under the given name and index.
	 */
	public Object getData(String dataName, int dataIndex)
	{
		if ((dataIndex < 0) | (dataIndex > m_DataSetCount-1)) {
			throw new java.lang.IllegalArgumentException("dataIndex must be between 0 and DataSetCount!");
		}
		return ((Vector) m_DataCollections.get(dataName)).get(dataIndex);
	}

	/** Increase the data set count and memory space for it if necessary.
	 *  @param      newDataSetCount     The new size in number of data set.
	 */
	private void resize(int newDataSetCount)
	{
		if (newDataSetCount < 0) {
			throw new java.lang.IllegalArgumentException("dataIndex must be non-negative!");
		}
		if (newDataSetCount <= m_DataSetCount) {return;}

		//if necessary extends all arrays
		if (newDataSetCount >= m_DataSetCount) {
			m_DataSetCount = newDataSetCount;
			String[] dataList = dataNamesList();
			for (int i=0; i<dataList.length; i++)
			{
			    ((Vector) m_DataCollections.get(dataList[i])).setSize(m_DataSetCount);
			}
		}
	}

	/** Stores some data under a given name and index.
	 * @param		dataName			Name under which the data will be stored.
	 * @param		dataIndex			Index under which the data will be stored.
	 * @param		data				Data to be stored.
	 */
	public void setData(String dataName, int dataIndex, Object data)
	{
		if (dataIndex < 0) {
			throw new java.lang.IllegalArgumentException("dataIndex must be non-negative!");
		}

		//if necessary extends all arrays
		if (dataIndex > m_DataSetCount-1) {resize(dataIndex+1);}

		Vector temp;
		//if data already exist, use that array
		if (hasData(dataName)) {
			temp = (Vector) m_DataCollections.get(dataName);
		//else create an array of the right size in put in in the collection
		} else {
			temp = new Vector();
			temp.setSize(m_DataSetCount);
			m_DataCollections.put(dataName, temp);
		}
		//set data
		temp.set(dataIndex, data);
	}

	/** Stores a data set under a given index.
	 * @param		dataIndex			Index under which the data will be stored.
	 * @param		dataSet				Data set to be stored.
	 * @param       overwrite           When <code>true</code>, data already
	 *                                  existing is overwritten, otherwise, it
	 *                                  the data is not added.
	 */
	public void setDataSet(int dataIndex, DataSet dataSet, boolean overwrite)
	{
		if (dataIndex < 0) {
			throw new java.lang.IllegalArgumentException("dataIndex must be non-negative!");
		}

		//if necessary extends all arrays
		if (dataIndex > m_DataSetCount-1) {resize(dataIndex+1);}

		//extract data names
		String[] dataList = dataSet.dataNamesList();

		for (int i=0; i<dataList.length; i++)
		{
			if (overwrite) {
				if (hasData(dataList[i]) && (getData(dataList[i], dataIndex) != null)) {
					System.err.println("Overwriting " + dataList[i]);
				}
				setData(dataList[i], dataIndex, dataSet.getData(dataList[i]));
			} else {
				if (!hasData(dataList[i])) {
					setData(dataList[i], dataIndex, dataSet.getData(dataList[i]));
				} else {
					System.err.println(dataList[i] + " skipped");
				}
			}
		}
	}

	/** Removes the data collection stored under a given name and returns it.
	 * @param		dataName			Name of the data collection to remove.
	 * @return		The removed data collection.
	 */
	public Object[] removeData(String dataName)
	{
		Object[] temp = toArray((Vector) m_DataCollections.remove(dataName));
		return temp;
	}

	/** Removes all the data collection in the set except the desired ones.
	 * @param		keptList			List of datan ames to kept.
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
				m_DataCollections.remove(dataList[i]);
			}
		}
	}

	/** Changes the name under which some data collection is stored.
	 * @param		oldDataName			Name associate to the data collection.
	 * @param		newDataName			New name to associate to the data collection.
	 * @return      The array of data associated to the name change.
	 */
	public Object[] renameData(String oldDataName, String newDataName)
	{
		Vector temp;
		//first check if oldDataName if valid
		if (!m_DataCollections.containsKey(oldDataName))
		{
			return toArray(null);
		}
		//if so rename it
		else
		{
			temp = (Vector) m_DataCollections.put(newDataName, m_DataCollections.remove(oldDataName));
			return toArray(temp);
		}
	}

	/*********************************************************************/
	//toString method

	public String toString()
	{
		String ret = "";
		ret += "\tClass: DataSetCollection\n";
		//Datas
/*
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
*/
		//return
		return ret;
	}


}