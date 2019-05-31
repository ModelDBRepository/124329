package lnsc;
import java.io.*;
import java.awt.*;
import java.awt.image.*;

/** <P> Set tool functions. </P>
 *
 *  @author Francois Rivest
 *  @version 1.0
 *  @since 1.0
 */
public final class Tools
{

	/** Make class non constructible. */
	private Tools() {}

	/** Generates a set of coordinates that form a grid in the
	 *  <code>[-1,1]x[-1,1]</code> space (including the boundary).
	 *  @param      size                    Number of grid element along both
	 *                                      dimensions.
	 *  @return     A set of <code>size*size</code> bi-dimensional vectors
	 *              equally spaced in the range <code>[-1,1]x[-1,1]</code>.
	 */
	public static final double[][] makeGrid(int size)
	{
		int i, j;
		double[][] ret = new double[(size)*(size)][2];

		for (i=0; i<(size); i++)
		{
			for (j=0; j<(size); j++)
			{
				ret[(size)*i+j][0] = (2.0/(double)(size-1))*(double)i -1.0;
				ret[(size)*i+j][1] = (2.0/(double)(size-1))*(double)j -1.0;
			}
		}

		return ret;
	}

	/** Takes a unit and a count and creates and array of that unit and count
	 * (by copy to the provided units).
	 * @param    newCount    Number of units in the array
	 * @param    newUnit     Units to initialise the array with.
	 * @return    An array of newCount newUnits.
	 */
	public static FunctionalUnit[] createUnitArray(int newCount, FunctionalUnit newUnit)
	{
		FunctionalUnit[] ret = new FunctionalUnit[newCount];
		for (int i=0; i<newCount; i++)
		{
			try {
				ret[i] = (FunctionalUnit) newUnit.clone();
			} catch (CloneNotSupportedException e){
				throw new java.lang.RuntimeException("FunctionalUnit must support Cloneable interface!");
			}
		}
		return ret;
	}

	/** Takes a set of data an generate <i>k</i> train sets and test sets by
	 *  splitting the data into <i>k</i> folds and using each fold as a
	 *  test set for the train set made of the other <i>k-1</i> folds.
	 *  @param      data        A valid {@link DataNames#TRAIN_SET}.
	 *  @param      foldCount   Number of folds into which the data must be split.
	 *  @return     A data set collection containing <i>k</i> pairs of
	 *              {@link DataNames#TRAIN_SET} and {@link DataNames#TEST_SET}.
	 */
	public static final DataSetCollection makeCrossValidationSets(DataSet data,
	                                                              int foldCount)
	{
		int patternCount = ((Integer) data.getData(DataNames.PATTERN_COUNT)).intValue();
		int[] sizes = new int[foldCount];
		int[][] partition = new int[foldCount][];
		int[] indexList = new int[patternCount];
		int i, j, k, val, ind, c=0;
		double[][] in, out, tin, tout;
		double[][] fullIn = (double[][]) data.getData(DataNames.INPUT_PATTERNS);
		double[][] fullOut = (double[][]) data.getData(DataNames.TARGET_PATTERNS);
		DataSet newSet;
		DataSetCollection ret = new DataSetCollection();

		//set up the fold sizes
		for (i=0; i<foldCount; i++)
		{
			if (i < patternCount % foldCount) {
				sizes[i] = (int) Math.ceil((double) patternCount / (double) foldCount);
			} else {
				sizes[i] = (int) Math.floor((double) patternCount / (double) foldCount);
			}
			partition[i] = new int[sizes[i]];
		}

		//generate the partition (i.e. smallSize patterns number of the k sets
		//to do this, generate a list of all numbers
		for (i=0; i<patternCount; i++)
		{
			indexList[i] = i;
		}
		//shuffle them
		for (i=0; i<patternCount-1; i++)
		{
			val = indexList[i];
			ind = (int) (Math.random()*((double)(patternCount-i-1))) + i+1;
			indexList[i] = indexList[ind];
			indexList[ind] = val;
		}
		//take them in order to make the partition
		for (i=0; i<foldCount; i++)
		{
			for (j=0; j<sizes[i]; j++)
			{
				partition[i][j] = indexList[c];
				c++;
			}
		}

		//print out the partition
		for (i=0; i<foldCount; i++)
		{
			for (j=0; j<sizes[i]; j++)
			{
				System.out.print(Integer.toString(partition[i][j]) + ", ");

			}
			System.out.println();
		}


		//make the sets by
		for (i=0; i<foldCount; i++)
		{
			//generate train set (using all subsets but the ith one)
			tin = new double[patternCount][];
			tout = new double[patternCount][];
			c=0;
			for (j=0; j<foldCount; j++)
			{
				if (j != i)
				{
					for (k=0; k<sizes[j]; k++)
					{
						tin[c] = fullIn[partition[j][k]];
						tout[c] = fullOut[partition[j][k]];
						c++;
					}
				}
			}
			//eliminate useless patterns
			in = new double[c][];
			out = new double[c][];
			for (j=0; j<c; j++)
			{
				in[j] = tin[j];
				out[j] = tout[j];
			}
			//save
		    newSet = new DataSet();
			newSet.setData(DataNames.INPUT_PATTERNS, in);
			newSet.setData(DataNames.TARGET_PATTERNS, out);
			newSet.setData(DataNames.PATTERN_COUNT, new Integer(c));
			ret.setData(DataNames.TRAIN_SET, i, newSet);

			//generate test set (using the ith subset)
			in = new double[sizes[i]][];
			out = new double[sizes[i]][];
			for (k=0; k<sizes[i]; k++)
			{
				in[k] = fullIn[partition[i][k]];
				out[k] = fullOut[partition[i][k]];
			}
		    newSet = new DataSet();
			newSet.setData(DataNames.INPUT_PATTERNS, in);
			newSet.setData(DataNames.TARGET_PATTERNS, out);
			newSet.setData(DataNames.PATTERN_COUNT, new Integer(sizes[i]));
			ret.setData(DataNames.TEST_SET, i, newSet);

		}

		//return them
		return ret;
	}

	/** Imports a data set from a file.
	 *  @param      fileName                The name of the file.
	 *  @param      inputCount              Number of input values per row.
	 *  @param      outputCount             Number of output values per row.
	 *  @param      patternCount            Total number of pattern pairs to expect.
	 *  @return     A valid {@link DataNames#TRAIN_SET}.
	 *
	 * The file must contain 1 pattern per row, the first few elements being the
	 * input values and the last few the output values.
	 */
	public static DataSet importDataSet(String fileName, int inputCount, int outputCount, int patternCount) throws IOException
	{
		//Make data set and space for patterns
		double[][] inputs = new double[patternCount][inputCount];
		double[][] outputs = new double[patternCount][outputCount];
		DataSet ret = new DataSet(new String[] {DataNames.INPUT_PATTERNS, DataNames.TARGET_PATTERNS, DataNames.PATTERN_COUNT},
		                          new Object[] {inputs, outputs, new Integer(patternCount)});

		//File reader and tokenizer
		FileReader inFile = new FileReader(fileName);
		StreamTokenizer in = new StreamTokenizer(inFile);
		in.parseNumbers();
		in.eolIsSignificant(true);
		int count = 0, col = 0;

		//Read data
		while (in.nextToken() != StreamTokenizer.TT_EOF)
		{
			switch (in.ttype)
			{
				case StreamTokenizer.TT_EOL:
					if (col == inputCount + outputCount) {
						col = 0;
						count++;
					} else { //col < inputCount + outputCount
						throw new IOException("Line " + Integer.toString(count) + " is too short!");
					}
					break;
			    case StreamTokenizer.TT_NUMBER:
					if (col < inputCount) {
						inputs[count][col] = in.nval;
					} else if (col < inputCount + outputCount) {
						outputs[count][col-inputCount] = in.nval;
					} else {
						throw new IOException("Line " + Integer.toString(count) + " is too long!");
					}
					col++;
					break;
			    case StreamTokenizer.TT_WORD:
					throw new IOException("Line " + Integer.toString(count) + " column " + Integer.toString(col) + " not a number!");
			}
		}
		if (count != patternCount) {
			throw new IOException("Line: " + Integer.toString(count) + " Invalid pattern count!");
		}

		//Return the data set
		return ret;
	}

	/** Saves a data set to a file.
	 *  @param      fileName                The name of the file.
	 *  @param      dataSet                 The data set to  save.
	 */
	public static void saveDataSet(String fileName, DataSet dataSet) throws IOException
	{
		FileOutputStream fileOut = new FileOutputStream(fileName);
		ObjectOutputStream out = new ObjectOutputStream(fileOut);
		out.writeObject(dataSet);
	}

	/** Saves a data set to a file.
	 *  @param      file                    The file descriptor.
	 *  @param      dataSet                 The data set to  save.
	 */
	public static void saveDataSet(File file, DataSet dataSet) throws IOException
	{
		FileOutputStream fileOut = new FileOutputStream(file);
		ObjectOutputStream out = new ObjectOutputStream(fileOut);
		out.writeObject(dataSet);
	}

	/** Loads a data set (saved using {@link #saveDataSet}) from a file.
	 *  @param      fileName                The name of the file.
	 *  @return     The data set read.
	 */
	public static DataSet loadDataSet(String fileName) throws IOException, ClassNotFoundException
	{
		FileInputStream fileIn = new FileInputStream(fileName);
		ObjectInputStream in = new ObjectInputStream(fileIn);
		return (DataSet) in.readObject();
	}

	/** Loads a data set (saved using {@link #saveDataSet}) from a file.
	 *  @param      file                  The file descriptor.
	 *  @return     The data set read.
	 */
	public static DataSet loadDataSet(File file) throws IOException, ClassNotFoundException
	{
		FileInputStream fileIn = new FileInputStream(file);
		ObjectInputStream in = new ObjectInputStream(fileIn);
		return (DataSet) in.readObject();
	}

	/** Copies an object by writing it to a temp file and reading it afterwards.
	 *  This function requires serializability of the object.
	 *  @param      obj                     A serializable object.
	 *  @return     A deep copy of the object.
	 */
	public static Serializable copyObject(Serializable obj)
	{
		Serializable ret;
		try {
			//take temp file name
			String fileName = Double.toString(Math.random());
			//write
			FileOutputStream fileOut = new FileOutputStream(fileName);
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
	    	out.writeObject(obj);
		    out.close();
			fileOut.close();
			//read
			FileInputStream fileIn = new FileInputStream(fileName);
			ObjectInputStream in = new ObjectInputStream(fileIn);
			ret = (Serializable) in.readObject();
			in.close();
		    //clean up temp file
			(new File(fileName)).delete();
		} catch (Exception e) {
			throw new RuntimeException(e.toString());
		}
		return ret;
	}

	/** Tabulates text. Given a string with new line character, a tab character
	 *  is inserted at the beginning of every line.
	 *  @param      text                    Text to be tabulated.
	 *  @param      tabCount                Number of tab to add.
	 *  @return     The tabulated text.
	 */
	public static String tabText(String text, int tabCount)
	{
		int start = 0, stop;
		String newString = new String();
		String tabs = new String();
		for (int i=0; i<tabCount; i++) {tabs += "\t";}
		while ((stop = text.indexOf('\n',start)) != -1)
		{
			//copy the line with the newline char with tabs before
			newString += tabs + text.substring(start,stop+1);
			start = stop+1;
		    if (start >= text.length()) {break;}
		}
		//add tabbed last line if any
		if (!(start >= text.length())) {
			newString += tabs + text.substring(start, text.length());
		}
		return newString;
	}

	/** Takes an images and returns and r,g,b arrays of values between 0 and 256.
	 *  It supports only 256 color Gif.
	 *  @param      image                   The image to transform.
	 *  @return     double[3][width][height]    An array where the first index
	 *                                          indicates red (0), green (1), or
	 *                                          blue (2) component, and the two
	 *                                          others the x,y positon of the
	 *                                          pixel. Values range [0,256).
	 */
	public int[][][] GIFEncoder(Image image) throws AWTException, InterruptedException
	{
		int width = image.getWidth(null);
		int height = image.getHeight(null);

		int values[] = new int[width*height];
		PixelGrabber grabber = new PixelGrabber(image, 0, 0, width, height, values, 0, width);

		if(grabber.grabPixels() != true) {
			throw new AWTException("Grabber returned false: " + grabber.status());
		}

		int r[][] = new int[width][height];
		int g[][] = new int[width][height];
		int b[][] = new int[width][height];

		int index = 0;
		for (int y = 0; y < height; ++y)
		{
			for (int x = 0; x < width; ++x)
			{
				r[x][y] = (int) ((values[index] >> 16) & 0xFF);
				g[x][y] = (int) ((values[index] >> 8) & 0xFF);
				b[x][y] = (int) ((values[index]) & 0xFF);
				index++;
			}
		}

		return new int[][][] {r,g,b};
    }
}
