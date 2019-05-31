package lnsc;
import java.lang.reflect.*;

/** <P> Set of linear algebra functions for vectors and matrices. </P>
 *
 *  @author Francois Rivest
 *  @version 1.0
 *  @since 1.0
 */
public final class LinearAlgebra
{

	/** Make class non constructible. */
	private LinearAlgebra() {}

	/** Returns the sign of a number.
	 *  @param      x       The number to return the sign of.
	 *  @return     <code>-1</code> for negative, <code>1</code> for positive,
	 *              <code>0</code> otherwise.
	 */
	public static double sign(double x)
	{
		if (x<0) {
			return -1.0;
		} else if (x>0) {
			return 1.0;
		} else {
			return 0.0;
		}
	}

	/*********************************************************************/
	//Type checking tools


	/** Checks whether a given object is of type <code>double[]</code>.
	 * @param		v		Object to be tested.
	 * @return		<code>true</code> is the object is of type
	 *              <code>double[]</code>.
	 */
	public static final boolean isVector(Object v)
	{
		return (v instanceof double[]);
	}

	/** Checks whether a given object is of type <code>double[size]</code>.
	 * @param		v		Object to be tested.
	 * @param		size	Size to check for.
	 * @return		<code>true</code> is the object is of type
	 *              <code>double[size]</code>.
	 */
	public static final boolean isVector(Object v, int size)
	{
		return ((v instanceof  double[]) &&
			    (Array.getLength(v) == size));
	}

	/** Checks whether a given object is of type <code>double[][]</code> and
	 *  whether sub vectors are of the same length.
	 * @param		m		Object to be tested.
	 * @return		<code>true</code> is the object is of type
	 *              <code>double[row][col]</code>.
	 */
	public static final boolean isMatrix(Object m)
	{
		int i, rows, cols;

		//check whether it is an array of double
		if (!(m instanceof double[][])) return false;

		//check whether it is rectangular
		//i.e. whether each rows has the same size
		rows = Array.getLength(m);
		if (rows != 0)
		{
			cols = Array.getLength(((Object[]) m)[0]);
			for (i=1; i<rows; i++)
			{
				if (cols != Array.getLength(((Object[]) m)[i])) return false;
			}
		}

		return true;
	}

	/** Checks whether a given object is of type <code>double[rows][cols]</code>.
	 * @param		m		Object to be tested.
	 * @param		rows	Number of rows to check for.
	 * @param		cols	Number of columns to check for.
	 * @return		<code>true</code> is the object is of type
	 *              <code>double[rows][cols]</code>.
	 */
	public static final boolean isMatrix(Object m, int rows, int cols)
	{
		int i;

		//check whether it is an array of double
		if (!(m instanceof double[][])) return false;

		//check if rows match
		if (Array.getLength(m) != rows) return false;

		//check whether it is rectangular
		//i.e. whether each rows has the same size
		if (rows != 0)
		{
			for (i=0; i<rows; i++)
			{
				if (cols != Array.getLength(((Object[]) m)[i])) return false;
			}
		}

		return true;
	}

	/** Checks whether a given object is of type <code>double[][][]</code> and
	 *  whether sub matrices are of the same dimensions.
	 * @param		m		Object to be tested.
	 * @return		<code>true</code> is the object is of type
	 *              <code>double[slab][colx][coly]</code>.
	 */
	public static final boolean is3DMatrix(Object m)
	{
		int slabs, colxs = -1, colys = -1;

		//check whether it is an array of double
		if (!(m instanceof double[][][])) {return false;}

		//check whether it is rectangular
		//i.e. whether each layer has the same dimensions
		slabs = Array.getLength(m);
		if (slabs != 0)
		{
			if (colxs == -1) {colxs = Array.getLength(((Object[]) m)[0]);}
		    for (int i=0; i<slabs; i++)
			{
			    if (colxs != Array.getLength(((Object[]) m)[i])) {return false;}
				if (colxs != 0)
				{
					if (colys == -1) {colys = Array.getLength(((Object[][]) m)[0][0]);}
					for (int j=0; j<colxs; j++)
					{
					    if (colys != Array.getLength(((Object[][]) m)[i][j])) {return false;}
					}
				}
			}
		}

		return true;
	}

        /** Check whether the matrix is a 2D squared matrix (row = col).
         * @param    m     The matrix to check
         * @return   <code>true</code> if the number of rows equals the number of columns.
         */
        public static boolean isSquareMatrix(double[][] m)
        {
          boolean isSquare = true;
          for (int i = 0; i<m.length; i++) {
            if (m.length != m[i].length)
              isSquare = false;
          }
          return isSquare;
        }

        /** Check whether the matrix is a 3D squared matrix (row = col = slabs).
         * @param    m     The matrix to check
         * @return   <code>true</code> if the number of rows equals the number
         *           of columns and the number of slabs.
         */
        public static boolean isSquareMatrix(double[][][] m)
        {
          boolean isSquare = true;
          for (int i = 0; i<m.length; i++) {
            for (int j = 0; j<m[i].length; j++) {
            if ((m.length != m[i].length) || (m[i].length != m[i][j].length))
              isSquare = false;
            }
          }
          return isSquare;
        }

        /** Check whether the matrix is a 4D squared matrix (row = col = slabs = hyperslabs).
         * @param    m     The matrix to check
         * @return   <code>true</code> if the number of rows equals the number
         *           of columns, the number of slabs and the number of hyperslabs.
         */
        public static boolean isSquareMatrix(double[][][][] m)
        {
          boolean isSquare = true;
          for (int i = 0; i<m.length; i++) {
            for (int j = 0; j<m[i].length; j++) {
              for (int k = 0; k<m[i][j].length; k++) {
              if ((m.length != m[i].length) || (m[i].length != m[i][j].length) ||
                (m[i][j].length != m[i][j][k].length))
                isSquare = false;
              }
            }
          }
          return isSquare;
        }

	/*********************************************************************/
	//toString method

	//Follows toString definition as in Francois Rivest February 2nd 2001

	/** Generates a {@link java.lang.Object#toString} representation of a
	 *  vector.
	 */
	public static final String toString(double[] v)
	{
		String ret = new String();
		ret += "Vector(" + v.length + "):\n";
		ret += "\t";
		for (int i=0; i<v.length; i++)
		{
			ret += Double.toString(v[i]) + '\t';
		}
		return ret;
	}

	/** Generates a {@link java.lang.Object#toString} representation of a
	 *  vector of integer.
	 */
	public static final String toString(int[] v)
	{
		String ret = new String();
		ret += "Vector(" + v.length + "):\n";
		ret += "\t";
		for (int i=0; i<v.length; i++)
		{
			ret += Integer.toString(v[i]) + '\t';
		}
		return ret;
	}

	/** Generates a {@link java.lang.Object#toString} representation of a
	 *  matrix (as defined by {@link #isMatrix}).
	 */
	public static final String toString(double[][] m)
	{
		String ret = new String("Matrix(" + m.length + "," + m[0].length + "):");
		for (int i=0; i<m.length; i++)
		{
			ret += "\n\t";
			for (int j=0; j<m[i].length; j++)
			{
				ret += Double.toString(m[i][j]) + '\t';
			}
		}
		return ret;
	}

	/** Generates a {@link java.lang.Object#toString} representation of a
	 *  matrix of integer (as defined by {@link #isMatrix}).
	 */
	public static final String toString(int[][] m)
	{
		String ret = new String("Matrix(" + m.length + "," + m[0].length + "):");
		for (int i=0; i<m.length; i++)
		{
			ret += "\n\t";
			for (int j=0; j<m[i].length; j++)
			{
				ret += Integer.toString(m[i][j]) + '\t';
			}
		}
		return ret;
	}

	/** Generates a {@link java.lang.Object#toString} representation of a
	 *  3D matrix of integer (as defined by {@link #is3DMatrix}).
	 */
	public static final String toString(double[][][] m)
	{
		String ret = new String("Array(" + m.length + "):Matrix(" + m[0].length + "," + m[0][0].length + "):");
		for (int k = 0; k<m.length; k++)
		{
			ret += "\n\tSlab(" + k + ")";
			for (int i=0; i<m[k].length; i++)
			{
				ret += "\n\t\t";
				for (int j=0; j<m[k][i].length; j++)
				{
					ret += Double.toString(m[k][i][j]) + '\t';
				}
			}
		}
		return ret;
	}

	/*********************************************************************/
	//Matrix and vector operators

	/** Computes the multiplication of a vector times a scalar.
	 * @param			s		The scalar.
	 * @param			v		The vector.
	 * @return			The resulting vector.
	 */
	public static final double[] multScalarVector(double s, double[] v)
	{
		int i;

		//TODO parameters checking

		//make vector
		double[] ret = new double[v.length];

		//compute multiplication
		for (i=0; i<v.length;i++)
		{
			ret[i] = s * v[i];
		}

		//returns
		return ret;
	}

	/** Computes the multiplication of a matrix times a scalar.
	 * @param			s		The scalar.
	 * @param			m		The matrix.
	 * @return			The resulting matrix.
	 */
	public static final double[][] multScalarMatrix(double s, double[][] m)
	{
		int i,j;

		//TODO parameters checking

		//make vector
		double[][] ret = new double[m.length][m[0].length];

		//compute multiplication
		for (i=0; i<m.length;i++)
		{
			for (j=0; j<m[0].length; j++)
			{
			    ret[i][j] = s * m[i][j];
			}
		}

		//returns
		return ret;
	}

	/** Computes the multiplication of a matrix times a scalar.
	 * @param			s		The scalar.
	 * @param			m		The 3D matrix.
	 * @return			The resulting matrix.
	 */
	public static final double[][][] multScalar3DMatrix(double s, double[][][] m)
	{
		int i,j,k;

		//TODO parameters checking

		//make vector
		double[][][] ret = new double[m.length][m[0].length][m[0][0].length];

		//compute multiplication
		for (i=0; i<m.length;i++)
		{
			for (j=0; j<m[0].length; j++)
			{
				for (k=0; k<m[0][0].length; k++)
				{
			        ret[i][j][k] = s * m[i][j][k];
				}
			}
		}

		//returns
		return ret;
	}

	/** Computes the multiplication of a matrix times a column vector.
	 * @param			m		The matrix.
	 * @param			v		The vector.
	 * @return			The resulting vector.
	 */
	public static final double[] multMatrixVector(double[][] m, double[] v)
	{
		int i,j;
		double sum;

		//TODO parameters checking

		//make array
		double[] ret = new double[m.length];

		//compute multiplication
		for (i=0; i<m.length;i++)
		{
			sum = 0.0;
			for (j=0; j<v.length; j++)
			{
				sum += m[i][j] * v[j];
			}
			ret[i] = sum;
		}

		//returns
		return ret;
	}

	/** Computes the multiplication of a matrix times a vector.
	 * @param			v		The vector.
	 * @param			m		The matrix.
	 * @return			The resulting vector.
	 */
	public static final double[] multVectorMatrix(double[] v, double[][] m)
	{
		int i,j;
		double sum;

		//TODO parameters checking

		//make array
		double[] ret = new double[m[0].length];

		//compute multiplication
		for (i=0; i<m[0].length;i++)
		{
			sum = 0.0;
			for (j=0; j<v.length; j++)
			{
				sum += m[j][i] * v[j];
			}
			ret[i] = sum;
		}

		//returns
		return ret;
	}

	/** Computes the multiplication of a vector times a vector.
	 *  It is assume that the first vector is a column vector
	 *  (and the second one a row vector).
	 * @param			v1		The first (column) vector.
	 * @param			v2		The second (row) vector.
	 * @return			The resulting matrix.
	 */
	public static final double[][] multVectorVector(double[] v1, double[] v2)
	{
		int i,j;
		double sum;

		//TODO parameters checking

		//make array
		double[][] ret = new double[v1.length][v2.length];

		//compute multiplication
		for (i=0; i<v1.length;i++)
		{
			for (j=0; j<v2.length; j++)
			{
				ret[i][j] = v1[i] * v2[j];
			}
		}

		//returns
		return ret;
	}

	/** Computes the multiplication of two matrices.
	 * @param			m1		The first matrix.
	 * @param			m2		The second matrix.
	 * @return			The resulting matrix.
	 */
	public static final double[][] multMatrixMatrix(double[][] m1, double[][] m2)
	{
		int i,j,k;
		double sum;

		//TODO more parameters cheking
		if (m1[0].length != m2.length)
			throw new IllegalArgumentException("Matrices not compatible for multiplication!");

		//make array
		double[][] ret = new double[m1.length][m2[0].length];

		//compute multiplication
		for (i=0; i<m1.length; i++)
		{
			for (j=0; j<m2[0].length; j++)
			{
				sum = 0.0;
				for (k=0; k<m2.length; k++)
				{
					sum += m1[i][k] * m2[k][j];
				}
				ret[i][j] = sum;
			}
		}

		//returns
		return ret;
	}

	/** Computes the subtraction of two vectors.
	 * @param		v1		The first vector.
	 * @param		v2		The second vector.
	 * @return		The resulting vector <code>v1-v2</code>;
	 *
	 */
	public static final double[] subVectors(double[] v1, double[] v2)
	{
		int i;
		//TODO parameter checking
		int n = v1.length;
		double[] ret = new double[n];

		for (i=0; i<n; i++)
		{
			ret[i] = v1[i] - v2[i];
		}

		return ret;
	}

	/** Computes the subtraction of two matrices.
	 * @param		m1		The first matrix.
	 * @param		m2		The second matrix.
	 * @return		The resulting matrix <code>m1-m2</code>;
	 *
	 */
	public static final double[][] subMatrices(double[][] m1, double[][] m2)
	{
		int i;
		//TODO parameter checking
		int n = m1.length;
		int m = m1[0].length;
		double[][] ret = new double[n][m];

		for (i=0; i<n; i++)
		{
			for (int j=0; j<m; j++)
			{
				ret[i][j] = m1[i][j] - m2[i][j];
			}
		}

		return ret;
	}


	/** Computes the addition of two vectors.
	 * @param		v1		The first vector.
	 * @param		v2		The second vector.
	 * @return		The resulting vector <code>v1+v2</code>;
	 *
	 */
	public static final double[] addVectors(double[] v1, double[] v2)
	{
		int i;
		//TODO parameter checking
		int n = v1.length;
		double[] ret = new double[n];

		for (i=0; i<n; i++)
		{
			ret[i] = v1[i] + v2[i];
		}

		return ret;
	}

	/** Computes the addition of a vector to another one.
	 * @param		v		The source and target vector.
	 * @param		va		The vector to add to it
	 * @return		The resulting vector <code>v+=va</code> (v);
	 *
	 */
	public static final double[] addeVectors(double[] v, double[] va)
	{
		int i;
		//TODO parameter checking
		int n = v.length;

		for (i=0; i<n; i++)
		{
			v[i] += va[i];
		}

		return v;
	}


	/** Computes the addition of two matrices.
	 * @param		m1		The first matrix
	 * @param		m2		The second matrix.
	 * @return		The resulting matrix <code>m1+m2</code>;
	 *
	 */
	public static final double[][] addMatrices(double[][] m1, double[][] m2)
	{
		int i;
		//TODO parameter checking
		int n = m1.length;
		int m = m1[0].length;
		double[][] ret = new double[n][m];

		for (i=0; i<n; i++)
		{
			for (int j=0; j<m; j++)
			{
			    ret[i][j] = m1[i][j] + m2[i][j];
			}
		}

		return ret;
	}

	/** Computes the addition of a matrix to another one.
	 * @param		m		The source and target matrix.
	 * @param		ma		The matrix to add to it
	 * @return		The resulting vector <code>m+=ma</code> (m);
	 *
	 */
	public static final double[][] addeMatrices(double[][] m, double[][] ma)
	{
		//get dimension
		int n = m.length;
		int mm = m[0].length;

        //check argument
		if (!isMatrix(m, n, mm)) {
			throw new IllegalArgumentException("m is not a matrix!");
		}
		if (!isMatrix(ma, n, mm)) {
			throw new IllegalArgumentException("Matrices not compatible, they must have the same dimensions!");
		}

		//do it
		for (int i=0; i<n; i++)
		{
			for (int j=0; j<mm; j++)
			{
			    m[i][j] += ma[i][j];
			}
		}

		//return
		return m;
	}

	/** Copies a matrix into another matrix.
	 * @param		target			Matrix that will be overwitten.
	 * @param		source			Matrix that will be read.
	 */
	public static final void eMatrix(double[][] target, double[][] source)
	{
		//TODO param check
		overwriteSubMatrix(0, 0, source, target);
	}


	/** Computes the element wise addition of two 3D matrices.
	 * @param		m1		The first 3D matrix
	 * @param		m2		The second 3D matrix.
	 * @return		The resulting matrix <code>m1+m2</code>;
	 *
	 */
	public static final double[][][] add3DMatrices(double[][][] m1, double[][][] m2)
	{
		//get dimensions
		int n = m1.length;
		int m = m1[0].length;
		int l = m1[0][0].length;

		//check argument
		if (!is3DMatrix(m1)) {
			throw new IllegalArgumentException("m1 is not a 3Dmatrix!");
		}
		if (!is3DMatrix(m2)) {
			throw new IllegalArgumentException("m2 is not a 3Dmatrix!");
		}
		//TODO with (is3DMatrix(m2, n,m,l))
		if ((n != m2.length) ||
			(m != m2[0].length) ||
		    (l != m2[0][0].length))	{
			throw new IllegalArgumentException("Matrices not compatible, they must have the same dimensions!");
		}

		//do it
		double[][][] ret = new double[n][m][l];
		for (int i=0; i<n; i++)
		{
			for (int j=0; j<m; j++)
			{
				for (int k=0; k<l; k++)
				{
			        ret[i][j][k] = m1[i][j][k] + m2[i][j][k];
				}
			}
		}

		return ret;
	}

	/** Computes the addition of a 3Dmatrix to another one.
	 * @param		m		The source and target 3D matrix.
	 * @param		ma		The 3D matrix to add to it
	 * @return		The resulting vector <code>m+=ma</code> (m);
	 *
	 */
	public static final double[][][] adde3DMatrices(double[][][] m, double[][][] ma)
	{
		int i;
		//TODO parameter checking
		int n = m.length;
		int mm = m[0].length;
		int l = m[0][0].length;

		for (i=0; i<n; i++)
		{
			for (int j=0; j<mm; j++)
			{
				for (int k=0; k<l; k++)
				{
			        m[i][j][k] += ma[i][j][k];
				}
			}
		}

		return m;
	}

	/** Computes the 2D rotated vector by the given angle.
	 * @param		v		The vector to rotate.
	 * @param		angle	The angle of rotation in radian.
	 * @return		The resulting vector;
	 *
	 */
	public static final double[] rotateVector(double[] v, double angle)
	{
		//TODO parameter checking
		double[] ret = new double[2];
		ret[0] = v[0]*Math.cos(angle) - v[1]*Math.sin(angle);
		ret[1] = v[0]*Math.sin(angle) + v[1]*Math.cos(angle);
		return ret;
	}

	/** Creates the transposed matrix.
	 *  @param      m       The matrix to be transposed.
	 *  @return     m transposed.
	 */
	public static double[][] transposeMatrix(double[][] m)
	{
		//TODO parameter checking
		int rowCount = m.length;
		int colCount = m[0].length;
		double[][] ret = new double[colCount][rowCount];

		for (int i=0; i<rowCount; i++)
		{
			for (int j=0; j<colCount; j++)
			{
				ret[j][i] = m[i][j];
			}
		}

		return ret;
	}

	/** Sums elementwise a vector of matrices.
	 *  @param      Ms      Array of matrices.
	 *  @return     The matrix whose elements are the sum of the corresponding
	 *              elements of all the given matrices.
	 */
	public static double[][] sumMatrices(double[][][] Ms)
	{
		int matCount = Ms.length;
		int rowCount = Ms[0].length;
		int colCount = Ms[0][0].length;

		//Check argument
		for (int k=1; k<matCount; k++)
		{
			if (Ms[k].length != rowCount) {
				throw new IllegalArgumentException("Matrices not compatible, they must have the same dimensions!");
			}
			if (Ms[k][0].length != colCount) {
				throw new IllegalArgumentException("Matrices not compatible, they must have the same dimensions!");
			}
		}

		//Compute their sum
		double[][] ret = new double[rowCount][colCount];
		for (int k=0; k<matCount; k++)
		{
			for (int i=0; i<rowCount; i++)
			{
				for (int j=0; j<colCount; j++)
				{
					ret[i][j] += Ms[k][i][j];
				}
			}
		}

		return ret;
	}

	/** Computes a weighted sum of scalars. Given two vector of scalars, it
	 *  computes a weighted sum (like a dot product of two vectors).
	 *  @param          ws      Vector of scalars (or weights)
	 *  @param          Ss      Vector of scalars.
	 *  @return         The weighted sum (dot product).
	 */
	public static double weightedSum(double[] ws, double[] Ss)
	{
		//Check argument
		if (ws.length != Ss.length) {
			throw new IllegalArgumentException("Vectors sizes incompatible, they must have the same dimension!");
		}

		//Compute dot product
		double ret = 0;
		for (int i=0; i<ws.length; i++)
		{
			ret += ws[i] * Ss[i];
		}

		return ret;
	}

	/** Computes a weighted summation of vectors. Given a vector of scalar and
	 *  a vector of vectors, it computes the {@link #multScalarVector} of each
	 *  vector multiplied by its associated scalar and then call
	 *  {@link #sumVectors}.
	 *  @param          ws      Vector of scalars (or weights)
	 *  @param          Vs      Vector of vectors.
	 *  @return         The weighted sum of the vectors weighted by the scalars.
	 */
	public static double[] weightedSum(double[] ws, double[][] Vs)
	{
		int vecCount = ws.length;

		//Check argument
		if (ws.length != Vs.length) {
			throw new IllegalArgumentException("Array of Vectors and array of Weights not compatible, they must have the same dimension!");
		}
		for (int k=1; k<vecCount; k++)
		{
			if (Vs[k].length != Vs[0].length) {
				throw new IllegalArgumentException("Matrices not compatible, they must have the same dimensions!");
			}
		}

		//Compute their sum
		double[] ret = new double[Vs[0].length];
		for (int k=0; k<vecCount; k++)
		{
			for (int i=0; i<Vs[0].length; i++)
			{
				ret[i] += ws[k] * Vs[k][i];
			}
		}

		return ret;
	}

	/** Computes a weighted summation of matrices. Given a vector of scalar and
	 *  a vector of matrices, it computes the {@link #multScalarMatrix} of each
	 *  matrix multiplied by its associated scalar and then call
	 *  {@link #sumMatrices}.
	 *  @param          ws      Vector of scalars (or weights)
	 *  @param          Ms      Vector of matrices.
	 *  @return         The weighted sum of the matrices weighted by the scalars.
	 */
	public static double[][] weightedSum(double[] ws, double[][][] Ms)
	{
		int matCount = Ms.length;
		int rowCount = Ms[0].length;
		int colCount = Ms[0][0].length;

		//Check argument
		if (ws.length != Ms.length) {
		    throw new IllegalArgumentException("Array of Matrices and array of Weights not compatible, they must have the same dimension!");
		}
		for (int k=1; k<matCount; k++)
		{
			if (!isMatrix(Ms[k], rowCount, colCount)) {
				throw new IllegalArgumentException("Matrices not compatible, they must have the same dimensions!");
			}
		}

		//Compute their sum
		double[][] ret = new double[rowCount][colCount];
		for (int k=0; k<matCount; k++)
		{
			for (int i=0; i<rowCount; i++)
			{
				for (int j=0; j<colCount; j++)
				{
					ret[i][j] += ws[k] * Ms[k][i][j];
				}
			}
		}

		return ret;
	}


        /*********************************************************************/
        //Norms operation


        /** Computes the sum of the squares of each element of a vector.
         * @param    v    The vector to compute the norm.
         * @return   The norm of the vector.
         */
        public static double sumSquares(double[] v) {
          double ret = 0;
          for (int i = 0; i<v.length; i++) {
            ret += v[i]*v[i];
          }
          return ret;
        }

        /** Computes the sum of the squares of each element of a matrix.
         * @param    m    The matrix to compute the norm.
         * @return   The norm of the matrix.
         */
        public static double sumSquares(double[][] m) {
          double ret = 0;
          for (int i = 0; i<m.length; i++) {
            for (int j =0; j<m[i].length; j++) {
              ret +=  m[i][j]*m[i][j];
            }
          }
          return ret;
        }

        /** Computes the sum of the squares of each element of a 3D matrix.
         * @param    m    The 3D matrix to compute the norm.
         * @return   The norm of the 3D matrix.
         */
        public static double sumSquares(double[][][] m) {
          double ret = 0;
          for (int i = 0; i<m.length; i++) {
            for (int j =0; j<m[i].length; j++) {
              for (int k = 0; k<m[i][j].length; k++) {
                ret += m[i][j][k]*m[i][j][k];
              }
            }
          }
          return ret;
        }

	/** Normalize a vector so that its (euclidien) norm sum to 1. Done in-place.
	 * @param    v    The vector to normalize.
	 */
	public static void normalizeVector(double[] v) {
		//Compute norm
		double sum = 0.0;
		for (int i=0; i<v.length; i++)
		{
			sum += v[i]*v[i];
		}
		sum = Math.sqrt(sum);
		//Normalize
		for (int i=0; i<v.length; i++)
		{
			v[i] /= sum;
		}
	}




	/*********************************************************************/
	//Matrix and vector batch operators

	/** Applies {@link #subVectors} on a batch of vector pairs. That is,
	 *  <code>SetA[0]-SetB[0], SetA[1]-SetB[1], ... </code>
	 *  @param      setA        Array of first vectors in substraction.
	 *  @param      setB        Array of second vectors in subtraction.
	 *  @return     Array of results.
	 */
	public static final double[][] batchSubVectors(double[][] setA, double[][] setB)
	{
		int i;
		//TODO parameter checking
		int patternCount = setA.length;
		double[][] ret = new double[patternCount][];

		for (i=0; i<patternCount; i++)
		{
			ret[i] = subVectors(setA[i], setB[i]);
		}

		return ret;
	}

	/*********************************************************************/
	//Methods to play with structures


        /** Create a vector that is the concatenation of two other vectors.
         * @param       v1      The first vector.
         * @param       v2      The second vector.
         * @return      The resulting concatenation <code>[v1|v2]</code>.
         */
        public static final double[] concatenateVectors(double[] v1, double[] v2)
        {
                double[] ret = new double[v1.length + v2.length];
                for (int i=0; i<v1.length; i++)
                {
                        ret[i] = v1[i];
                }
                for (int i=0; i<v2.length; i++)
                {
                        ret[v1.length+i] = v2[i];
                }
                return ret;
        }


        /** Creates a vector by extract a contiguous portion of a larger vector.
         *  @param      v       The source vector.
         *  @param      start   Index from where to begin the extraction, that
         *                      is, index of the first element of the new vector.
         *  @param      stop    Index from where to stop the extraction, that
         *                      is, index of the last element of the new vector.
         *  @return     The extracted vector <code>v'=v[start..stop]</code>.
         */
        public static final double[] extractVector(double[] v, int start, int stop)
        {
                //param check
                if (start < 0 || start > v.length-1) {
                        throw new java.lang.IllegalArgumentException("start must be a valid index!");
                }
                if (stop < 0 || stop > v.length-1) {
                        throw new java.lang.IllegalArgumentException("stop must be a valid index!");
                }
                if (start > stop) {
                        throw new java.lang.IllegalArgumentException("stop must be no smaller than start!");
                }
                //extract
                double[] ret = new double[stop-start+1];
                for (int i=start; i<=stop; i++)
                {
                        ret[i-start] = v[i];
                }
                return ret;
        }


	/** Creates a new matrix which is like the original but with a few more
	 * columns (0's initialized).
	 * @param			m		The matrix.
	 * @param			count	The number of new columns.
	 * @return			The expended matrix.
	 */
	public static final double[][] appendNewColumns(double[][] m, int count)
	{
		int i,j;

		//TODO parameter check

		//make new matrix
		double[][] ret = new double[m.length][m[0].length+count];

		//copy the original values
		for (i=0; i<m.length; i++)
		{
			for (j=0; j<m[0].length; j++)
			{
				ret[i][j] = m[i][j];
			}
		}

		//return
		return ret;
	}

	/** Creates a new matrix which is like the original but with only the
	 * requested middle columns. Note that elements are copied one by one.
	 * @param			m		A matrix.
	 * @param			start	First column to kept.
	 * @param           count   The number of column to kept.
	 * @return			The reduced matrix.
	 */
	public static final double[][] extractColumns(double[][] m, int start, int count)
	{
		int i,j;

		//Parameter check
		if (!isMatrix(m)) {
			throw new IllegalArgumentException("Must be a square matrix!");
		}
		//TODO, check indexes

		//make new matrix
		double[][] ret = new double[m.length][count];

		//copy the original values
		for (i=0; i<ret.length; i++)
		{
			for (j=0; j<count; j++)
			{
				ret[i][j] = m[i][start+j];
			}
		}

		//return
		return ret;
	}

	/** Creates a new matrix which is like the original but with only the
	 * requested middle columns. Note that elements are copied one by one.
	 * @param			m		A matrix.
	 * @param			start	First column to kept.
	 * @param           count   The number of column to kept.
	 * @return			The reduced matrix.
	 */
	public static final int[][] extractColumns(int[][] m, int start, int count)
	{
		int i,j;

		//Parameter check
//          if (!isMatrix(m)) {
//                  throw new IllegalArgumentException("Must be a square matrix!");
//          }
		//TODO, check indexes

		//make new matrix
		int[][] ret = new int[m.length][count];

		//copy the original values
		for (i=0; i<ret.length; i++)
		{
			for (j=0; j<count; j++)
			{
				ret[i][j] = m[i][start+j];
			}
		}

		//return
		return ret;
	}


	/** Creates a new matrix which is like the original but with only the
	 * requested middle rows. Note that elements are copied one by one.
	 * @param			m		A matrix.
	 * @param			start	First column to kept.
	 * @param           count   The number of column to kept.
	 * @return			The reduced matrix.
	 */
	public static final double[][] extractRows(double[][] m, int start, int count)
	{

		int i, j;

		//Parameter check
		if (!isMatrix(m)) {
		    throw new IllegalArgumentException("Must be a square matrix!");
		}
		//TODO, check indexes

		//make new matrix
		double[][] ret = new double[count][m[0].length];

		for(i=0; i<count; i++)
		{
			for (j = 0; j<ret[0].length; j++)
			{
				ret[i][j] = m[start+i][j];
			}
		}

		//return
		return ret;
	}

	/** Creates a new matrix which is like the original but with only the
	 * requested middle rows. Note that elements are copied one by one.
	 * @param			m		A matrix.
	 * @param			start	First column to kept.
	 * @param           count   The number of column to kept.
	 * @return			The reduced matrix.
	 */
	public static final int[][] extractRows(int[][] m, int start, int count)
	{

		int i, j;

		//Parameter check
		if (!isMatrix(m)) {
			throw new IllegalArgumentException("Must be a square matrix!");
		}
		//TODO, check indexes

		//make new matrix
		int[][] ret = new int[count][m[0].length];

		for(i=0; i<count; i++)
		{
			for (j = 0; j<ret[0].length; j++)
			{
				ret[i][j] = m[start+i][j];
			}
		}

		//return
		return ret;
	}

	/** Creates a new matrix made of two matrices one above the other.
	 * @param		m1		The upper matrix.
	 * @param		m2		The lower matrix.
	 * @return		The resulting matrix
	 *
	 * Note: Cols of matrices m1 and m2 will be reused, not copied.
	 */
	public static final double[][] appendMatrixBelow(double[][] m1, double[][] m2)
	{
		int i;
		//TODO deal with 0 matrix, parameter check
		int rows = m1.length + m2.length;
		int cols = m2[0].length;

		double[][] ret = new double[rows][];
		for (i=0; i<m1.length; i++)
		{
			ret[i] = m1[i];
		}
		for (i=0; i<m2.length; i++)
		{
			ret[m1.length+i] = m2[i];
		}

		return ret;
	}

	/** Creates a new matrix made of two matrices one above the other.
	 * @param		m1		The upper matrix (or block).
	 * @param		m2		The lower matrix (or block).
	 * @return		The resulting matrix
	 *
	 * Note: Slabs of matrices m1 and m2 will be reused, not copied.
	 */
	public static final double[][][] appendMatrixBelow(double[][][] m1, double[][][] m2)
	{
		int i;
		//TODO deal with 0 matrix, parameter check
		int rows = m1.length + m2.length;
		int colsI = m2[0].length;
		int colsJ = m2[0][0].length;

		double[][][] ret = new double[rows][][];
		for (i=0; i<m1.length; i++)
		{
			ret[i] = m1[i];
		}
		for (i=0; i<m2.length; i++)
		{
			ret[m1.length+i] = m2[i];
		}

		return ret;
	}

	/** Creates a new matrix made of two matrices one beside the other.
	 * @param		m1		The left matrix.
	 * @param		m2		The right matrix.
	 * @return		The resulting matrix
	 *
	 * Note: Elements are copided one by one.
	 */
	 public static final double[][] appendMatrixBesides(double[][] m1, double[][] m2) {

	   //TODO: parameter check
	   int i,j,k;
	   int rows = m1.length;
	   int cols = m1[0].length + m2[0].length;

	   double[][] ret = new double[rows][cols];

	   for (i = 0; i<rows; i++) {
		 for (j =0 ; j<m1[0].length; j++) {

		   ret[i][j] = m1[i][j];

		 }

	   }
	   for (i = 0; i<rows; i++) {

		 k = 0;
		 for (j=m1[0].length; j<m1[0].length+m2[0].length; j++) {

		   ret[i][j] = m2[i][k];
		   k++;
		 }

	   }

	   return ret;
	 }

	 /** Copies a sub vector into a large vector at a given position.
	  * @param		start		    Index of the first element to overwrite.
	  * @param		source			Small vector that will be read.
	  * @param		target			Large vector that will be partly overwritten.
	  */
	 public static final void overwriteSubVector(int start, double[] source, double[] target)
	 {
		//Parameter checking
		if (start < 0) {
			throw new IllegalArgumentException("start must be non-negative!");
		}
		if (target.length < start + source.length) {
			throw new IllegalArgumentException("target vector is too short!");
		}

		//Overwrite
		 for (int i=0; i<source.length; i++)
		 {
			 target[start+i] = source[i];
		 }
	 }

	 /** Copies a sub vector into another vector at a given position.
	  * @param		start		    Index of the first element to overwrite
	  * @param      length          Length of the source vector to read
	  * @param		source			Small vector that will be read.
	  * @param		target			Large vector that will be partly overwritten.
	  */
	 public static final void overwriteSubVector(int start, int length,
												 double[] source, double[] target)
	 {
		//Parameter checking
		if (start < 0) {
			throw new IllegalArgumentException("start must be non-negative!");
		}
		if (length < 0) {
			throw new IllegalArgumentException("length must be non-negative!");
		}
		if (source.length < length) {
			throw new IllegalArgumentException("source vector is too short!");
		}
		if (target.length < start + length) {
			throw new IllegalArgumentException("target vector is too short!");
		}

		//Overwrite
		 for (int i=0; i<length; i++)
		 {
			 target[start+i] = source[i];
		 }
	 }

	/** Copies a sub matrix into a large matrix at a given position.
	 * @param		startRow		Index of the first row to overwrite.
	 * @param		startCol		Index of the second row to overwrite.
	 * @param		source			Small matrix that will be read.
	 * @param		target			Large matrix that will be partly overwritten.
	 */
	public static final void overwriteSubMatrix(int startRow, int startCol, double[][] source, double[][] target)
	{
		//Parameter checking
		//TODO

		//Overwrite
		int rowCount = source.length, colCount=0;
		if (rowCount != 0) {colCount = source[0].length;}
		for (int row=0; row<rowCount; row++)
		{
			for (int col=0; col<colCount; col++)
			{
				target[startRow+row][startCol+col] = source[row][col];
			}
		}
	}

	/** Creates a copy of a given vector.
	 *  @param      v       The vector to be copied.
	 *  @return     A copy of the given vector.
	 */
	public static final double[] copyVector(double[] v)
	{
		double[] ret = new double[v.length];

		for (int i=0; i<v.length; i++)
		{
			ret[i] = v[i];
		}

		return ret;
	}


	/** Creates a copy of a given matrix.
	 *  @param      m       The matrix to be copied.
	 *  @return     A copy of the given matrix.
	 */
	public static final double[][] copyMatrix(double[][] m)
	{
		int i,j;
		//TODO parameter checking
		int rows = m.length,
			cols = m[0].length;
		double[][] ret = new double[rows][cols];

		for (i=0; i<rows; i++)
		{
			for (j=0; j<cols; j++)
			{
				ret[i][j] = m[i][j];
			}
		}

		return ret;
	}

	/** Creates a new vector from the concatenation of the rows of the given
	 * matrix. The reverse operation is {@link #vectorToMatrix}.
	 * @param		m			The matrix to convert.
	 * @return		The resulting vector.
	 * @deprecated
	 */
	public static final double[] matrixToVector(double[][] m)
	{
		return concatenateRows(m);
	}

	/** Creates a new vector from the concatenation of the rows of the given
	 * matrix. The reverse operation is {@link #cutInRows}.
	 * @param		m			The matrix to convert.
	 * @return		The resulting vector.
	 */
	public static final double[] concatenateRows(double[][] m)
	{
		int i,j;

		//TODO parameter checking
		int rows = m.length;
		int cols = m[0].length;
		double[] ret = new double[rows*cols];

		for (i=0; i<rows; i++)
		{
			for (j=0; j<cols; j++)
			{
				ret[i*cols + j] = m[i][j];
			}
		}

		return ret;
	}

	/** Creates a new vector from the concatenation of the columns of the given
	 * matrix. The reverse operation is {@link #cutInCols}.
	 * @param		m			The matrix to convert.
	 * @return		The resulting vector.
	 */
	public static final double[] concatenateCols(double[][] m)
	{
		int i,j;

		//TODO parameter checking
		int rows = m.length;
		int cols = m[0].length;
		double[] ret = new double[rows*cols];

		for (i=0; i<cols; i++)
		{
			for (j=0; j<rows; j++)
			{
				ret[i*rows + j] = m[j][i];
			}
		}

		return ret;
	}

	/** Creates a matrix from a vector. This operation is basically
	 * the reverse operation of {@link #matrixToVector}.
	 * @param		v			The vector to convert.
	 * @param		cols		Length of each rows extracted of the vector.
	 * @return		The resulting matrix
	 * @deprecated
	 */
	public static final double[][] vectorToMatrix(double[] v, int cols)
	{
		return cutInRows(v, cols);
	}

	/** Creates a matrix from a vector by cutting in rows. This operation is
	 * basically the reverse operation of {@link #concatenateRows}.
	 * @param		v			The vector to convert.
	 * @param		cols		Length of each rows extracted from the vector.
	 * @return		The resulting matrix
	 */
	public static final double[][] cutInRows(double[] v, int cols)
	{
		int i, j;

		//TODO parameter checking
		int rows = v.length / cols;
		double[][] ret = new double[rows][cols];

		for (i=0; i<rows; i++)
		{
			for (j=0; j<cols; j++)
			{
				ret[i][j] = v[i*cols + j];
			}
		}

		return ret;
	}

	/** Creates a matrix from a vector by cutting in columns. This operation is
	 * basically the reverse operation of {@link #concatenateCols}.
	 * @param		v			The vector to convert.
	 * @param		rows		Length of each columns extracted from the vector.
	 * @return		The resulting matrix
	 */
	public static final double[][] cutInCols(double[] v, int rows)
	{
		int i, j;

		//TODO parameter checking
		int cols = v.length / rows;
		double[][] ret = new double[rows][cols];

		for (i=0; i<cols; i++)
		{
			for (j=0; j<rows; j++)
			{
				ret[j][i] = v[i*rows + j];
			}
		}

		return ret;
	}

        //this function performs a matrix transpose according to the transposeOrder
        public static double[][][] transposeMatrix(double[][][] m, int[] transposeOrder)
        {

        //param check
        if (isSquareMatrix(m) == false)
          throw new IllegalArgumentException("Argument is not a square matrix !");

        //get the size of the square matrix argument
        int dim = m.length;

        //create the new returning square matrix
        double[][][] ret = new double[dim][dim][dim];

        //compute ret matrix according to the transpose order

        for (int i = 0; i<dim; i++) {

          for (int j = 0; j<dim; j++) {

            for (int k = 0; k<dim; k++) {

              if ((transposeOrder[0] == 1) && (transposeOrder[1] == 2) && (transposeOrder[2] == 3))
                ret[i][j][k] = m[i][j][k];

              if ((transposeOrder[0] == 1) && (transposeOrder[1] == 3) && (transposeOrder[2] == 2))
              ret[i][j][k] = m[i][k][j];

              if ((transposeOrder[0] == 2) && (transposeOrder[1] == 1) && (transposeOrder[2] == 3))
              ret[i][j][k] = m[j][i][k];

              if ((transposeOrder[0] == 2) && (transposeOrder[1] == 3) && (transposeOrder[2] == 1))
              ret[i][j][k] = m[j][k][i];

              if ((transposeOrder[0] == 3) && (transposeOrder[1] == 1) && (transposeOrder[2] == 2))
              ret[i][j][k] = m[k][i][j];

              if ((transposeOrder[0] == 3) && (transposeOrder[1] == 2) && (transposeOrder[2] == 1))
              ret[i][j][k] = m[k][j][i];

            }

          }

        }

          return ret;
        }


         //this function performs a matrix transpose according to the transposeOrder
        public static double[][][][] transposeMatrix(double[][][][] m, int[] transposeOrder)
        {

        //param check
        if (isSquareMatrix(m) == false)
          throw new IllegalArgumentException("Argument is not a square matrix !");

        //get the size of the square matrix argument
        int dim = m.length;

        //create the new returning square matrix
        double[][][][] ret = new double[dim][dim][dim][dim];

        //compute ret matrix according to the transpose order

        for (int i = 0; i<dim; i++) {

          for (int j = 0; j<dim; j++) {

            for (int k = 0; k<dim; k++) {

              for (int p = 0; p<dim; p++) {

                if ((transposeOrder[0] == 1) && (transposeOrder[1] == 2) && (transposeOrder[2] == 3) && (transposeOrder[3] == 4))
                  ret[i][j][k][p] = m[i][j][k][p];

                if ((transposeOrder[0] == 1) && (transposeOrder[1] == 2) && (transposeOrder[2] == 4) && (transposeOrder[3] == 3))
                  ret[i][j][k][p] = m[i][j][k][p];

                if ((transposeOrder[0] == 1) && (transposeOrder[1] == 3) && (transposeOrder[2] == 2) && (transposeOrder[3] == 4))
                  ret[i][j][k][p] = m[i][j][k][p];

                if ((transposeOrder[0] == 1) && (transposeOrder[1] == 3) && (transposeOrder[2] == 4) && (transposeOrder[3] == 2))
                  ret[i][j][k][p] = m[i][j][k][p];

                if ((transposeOrder[0] == 1) && (transposeOrder[1] == 4) && (transposeOrder[2] == 2) && (transposeOrder[3] == 3))
                  ret[i][j][k][p] = m[i][j][k][p];

                if ((transposeOrder[0] == 1) && (transposeOrder[1] == 4) && (transposeOrder[2] == 3) && (transposeOrder[3] == 2))
                  ret[i][j][k][p] = m[i][j][k][p];


                if ((transposeOrder[0] == 2) && (transposeOrder[1] == 1) && (transposeOrder[2] == 3) && (transposeOrder[3] == 4))
                  ret[i][j][k][p] = m[i][j][k][p];

                if ((transposeOrder[0] == 2) && (transposeOrder[1] == 1) && (transposeOrder[2] == 4) && (transposeOrder[3] == 3))
                  ret[i][j][k][p] = m[i][j][k][p];

                if ((transposeOrder[0] == 2) && (transposeOrder[1] == 3) && (transposeOrder[2] == 1) && (transposeOrder[3] == 4))
                  ret[i][j][k][p] = m[i][j][k][p];

                if ((transposeOrder[0] == 2) && (transposeOrder[1] == 3) && (transposeOrder[2] == 4) && (transposeOrder[3] == 1))
                  ret[i][j][k][p] = m[i][j][k][p];

                if ((transposeOrder[0] == 2) && (transposeOrder[1] == 4) && (transposeOrder[2] == 3) && (transposeOrder[3] == 1))
                  ret[i][j][k][p] = m[i][j][k][p];

                if ((transposeOrder[0] == 2) && (transposeOrder[1] == 4) && (transposeOrder[2] == 1) && (transposeOrder[3] == 3))
                  ret[i][j][k][p] = m[i][j][k][p];


                if ((transposeOrder[0] == 3) && (transposeOrder[1] == 1) && (transposeOrder[2] == 2) && (transposeOrder[3] == 4))
                  ret[i][j][k][p] = m[i][j][k][p];

                if ((transposeOrder[0] == 3) && (transposeOrder[1] == 1) && (transposeOrder[2] == 4) && (transposeOrder[3] == 2))
                  ret[i][j][k][p] = m[i][j][k][p];

                if ((transposeOrder[0] == 3) && (transposeOrder[1] == 2) && (transposeOrder[2] == 1) && (transposeOrder[3] == 4))
                  ret[i][j][k][p] = m[i][j][k][p];

                if ((transposeOrder[0] == 3) && (transposeOrder[1] == 2) && (transposeOrder[2] == 4) && (transposeOrder[3] == 1))
                  ret[i][j][k][p] = m[i][j][k][p];

                if ((transposeOrder[0] == 3) && (transposeOrder[1] == 4) && (transposeOrder[2] == 1) && (transposeOrder[3] == 2))
                  ret[i][j][k][p] = m[i][j][k][p];

                if ((transposeOrder[0] == 3) && (transposeOrder[1] == 4) && (transposeOrder[2] == 2) && (transposeOrder[3] == 1))
                  ret[i][j][k][p] = m[i][j][k][p];


                if ((transposeOrder[0] == 4) && (transposeOrder[1] == 1) && (transposeOrder[2] == 3) && (transposeOrder[3] == 2))
                  ret[i][j][k][p] = m[i][j][k][p];

                if ((transposeOrder[0] == 4) && (transposeOrder[1] == 1) && (transposeOrder[2] == 2) && (transposeOrder[3] == 3))
                  ret[i][j][k][p] = m[i][j][k][p];

                if ((transposeOrder[0] == 4) && (transposeOrder[1] == 3) && (transposeOrder[2] == 1) && (transposeOrder[3] == 2))
                  ret[i][j][k][p] = m[i][j][k][p];

                if ((transposeOrder[0] == 4) && (transposeOrder[1] == 3) && (transposeOrder[2] == 2) && (transposeOrder[3] == 1))
                  ret[i][j][k][p] = m[i][j][k][p];

                if ((transposeOrder[0] == 4) && (transposeOrder[1] == 2) && (transposeOrder[2] == 3) && (transposeOrder[3] == 1))
                  ret[i][j][k][p] = m[i][j][k][p];

                if ((transposeOrder[0] == 4) && (transposeOrder[1] == 2) && (transposeOrder[2] == 1) && (transposeOrder[3] == 3))
                  ret[i][j][k][p] = m[i][j][k][p];

              }

            }

          }

        }

          return ret;
        }





}

