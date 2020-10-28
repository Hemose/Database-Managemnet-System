package JavaKillers;

import java.io.*;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

import JavaKillers.Table.Page;

public class DBApp {
	
	static File file = new File("data/metadata.csv");
	HashMap<String, Table> tables;

	// add init method and let it create the metadata file ? //George
	public DBApp() throws DBAppException {
		tables = new HashMap<String, Table>();
		if (file.length() < 10) {
			BufferedWriter bw;
			try {
				bw = new BufferedWriter(new FileWriter(file, true));
				bw.write("Table Name");
				bw.write(",");
				bw.write("Column Name");
				bw.write(",");
				bw.write("Column Type");
				bw.write(",");
				bw.write("ClusteringKey");
				bw.write(",");
				bw.write("Indexed");
				bw.write("\n");
				bw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				DBAppException exception = new DBAppException("The meta data file does not exist");
				throw (exception);
			}
		}
		init();
	}

	/**
	 * <h2>checkDelete</h2> The checkDelete method takes the Vector cols that is the
	 * metadata related to the table from which we delete. Also, it takes the Hash
	 * table which contains the keys and values for the records we are supposed to
	 * delete. We check that the hashtable keys and values match those in the
	 * metaddata and the data types are the same also.
	 * 
	 * @param cols
	 * @param htblColNameValue
	 * @return
	 * @throws DBAppException
	 */
	public static boolean checkDelete(Vector<String> cols, Hashtable<String, Object> htblColNameValue)
			throws DBAppException {
		boolean valid = true;
		for (String key : htblColNameValue.keySet()) {
			boolean validForkey = false;
			for (int i = 0; i < cols.size(); i++) {
				String[] Arr = cols.get(i).split(",");
				if (Arr[1].equals(key)) {
					if (isInstatceOf(htblColNameValue.get(key), Arr[2]))
						validForkey = true;
					break;
				}
			}
			valid &= validForkey;
			if (!valid)
				return false;
		}
		return valid;
	}

	/**
	 * The checkRemoveRecord method is given a record from the table and compares
	 * the values of the attributes. If they match, then this record will be
	 * deleted.
	 * 
	 * @param record
	 * @param ht
	 * @param cols
	 * @return
	 */
	public static boolean checkRemoveRecord(Vector<Object> record, Hashtable<String, Object> ht, Vector<String> cols) {
		for (String key : ht.keySet()) {
			for (int i = 0; i < cols.size(); i++) {
				String[] arr = cols.get(i).split(",");
				if (arr[1].equals(key))
					if (((Comparable) record.get(i)).compareTo(((Comparable) ht.get(key))) != 0)
						return false;
			}
		}
		return true;
	}

	/**
	 * The deleteFromTable method takes the table name and the attributes of the
	 * records that should be deleted. Firstly, We deserialize the page from the
	 * hard disk and we also get the table columns from the metadata. Secondly, the
	 * method checks that the hashtable keys and values are valid by checking the
	 * data types of the attributes and that all the keys are present in the
	 * metadata. Thirdly, we loop over the records by deserializing the pages one by
	 * one and checking all the records in the page linearly. After deleting a
	 * record, we check if the page is empty and update the pages numbering
	 * accordingly. Finally, we deserialize each page after looping over it and at
	 * the end we deserialize the updated table.
	 * 
	 * @param strTableName
	 * @param htblColNameValue
	 * @throws DBAppException
	 */
	
	public void deleteFromTable(String strTableName, Hashtable<String, Object> htblColNameValue) throws DBAppException {
		Table table = tables.get(strTableName);
		Vector<String> cols = readFromCSV(strTableName);

//		 To check that the keys exist and that their values correspond to metadata
		boolean valid = checkDelete(cols, htblColNameValue);
		if (!valid)
			throw new DBAppException("Wrong input format!!"); // George
		else {
			boolean needLinearSearch = needLinearSearch(htblColNameValue, table.indexperCol);
			if (needLinearSearch) {
				System.out.println("Linear");
				for (int i = 1; i <= table.size; i++) {
					Table.Page page = (Table.Page) deserialize(table.name + " " + i);
					for (int j = 0; j < page.Records.size(); j++) {
						// check if current record will be deleted or not
						boolean checkRemove = checkRemoveRecord(page.Records.get(j), htblColNameValue, cols);
						if (checkRemove)
							page.Records.remove(j--); // decrementing j to check the record at j-th index after deletion
					}
					if (page.Records.size() == 0) {
						for (int j = i + 1; j <= table.size; j++) {
							Table.Page nxtPage = (Table.Page) deserialize(table.name + " " + j);
							Serialize(table.name + " " + (j - 1), nxtPage);
						}
						File file = new File("data/" + table.name + " " + table.size + ".ser");
						file.delete();
						i--; // decrementing i to check the page at the i-the index after deletion
						table.size--;
					} else
						Serialize(table.name + " " + i, page);
				}
			} else {
				Vector<Ref> pages = new Vector<Ref>();
				for (String key : htblColNameValue.keySet()) {
					if (table.indexperCol.containsKey(key)) {
						Object index = table.indexperCol.get(key);
						if (index instanceof BPTree) {
							BPTree btree = (BPTree) index;
							pages.addAll(btree.search((Comparable) htblColNameValue.get(key)));
						} else {
							RTree rtree = (RTree) index;
							pages.addAll(rtree.search((Polygon) htblColNameValue.get(key)));
						}
						break;
					}
				}
				TreeSet<Ref> uniquePages = new TreeSet<Ref>();
				for (int i = 0; i < pages.size(); i++) {
					uniquePages.add(pages.get(i));
				}
				deleteLinearlyFromAllPages(table, htblColNameValue, uniquePages);
			}
		}
		Serialize(strTableName, table);
	}

	public void deleteLinearlyFromAllPages(Table table, Hashtable<String, Object> conditions, TreeSet<Ref> pages)
			throws DBAppException {
		System.out.println(conditions.toString());
		System.out.println(pages.toString());
		for (Ref r : pages) {
			Page currentPage = (Page) deserialize(table.name + " " + r.getPage());
			for (int j = 0; j < currentPage.Records.size(); j++) {
				Vector<Object> currentRecord = currentPage.Records.get(j);
				Vector<String> cols = readFromCSV(table.name);
				boolean delete = checkRemoveRecord(currentRecord, conditions, cols);
				if (delete) {
					currentPage.Records.remove(j--);
					delete_in_allindex(table, currentRecord, r.getPage());
				}
			}
			if (currentPage.Records.size() == 0) {
				for (int j = r.getPage() + 1; j <= table.size; j++) {
					Table.Page nxtPage = (Table.Page) deserialize(table.name + " " + j);
					Serialize(table.name + " " + (j - 1), nxtPage);
				}
				File file = new File("data/" + table.name + " " + table.size + ".ser");
				file.delete();
				table.size--;
			} else
				Serialize(table.name + " " + r.getPage(), currentPage);
		}
	}

	// public TreeSet<Integer> getUniquePages(Vector<Ref> pages){
	// TreeSet<Integer> uniquePages
	// }

	public boolean needLinearSearch(Hashtable<String, Object> conditions, HashMap<String, Object> tableIndexes) {

		for (String condition : conditions.keySet())
			if (tableIndexes.containsKey(condition))
				return false;

		return true;
	}

	/**
	 * The createTable method takes the table name, the clustering key and a
	 * hashtable containing the columns' names and their datatypes. We create an
	 * instance of type Table and serialize it into the hard disk. Then we add
	 * the columns' names and data types to the metadata.csv file.
	 *
	 * @param strTableName
	 * @param strClusteringKeyColumn
	 * @param htblColNameType
	 * @throws DBAppException
	 */

	public void createTable(String strTableName, String strClusteringKeyColumn,
			Hashtable<String, String> htblColNameType) throws DBAppException {
		// serialize the tables array, so the data about the tables is saved
		HashSet<String> tabelsname = myTablesname();
		if (tabelsname.contains(strTableName)) {
			throw new DBAppException("This table name is already exist");
		} else {
			Table t = new Table(strTableName);
			int i = 0;
			Set<String> colTypes = htblColNameType.keySet();
			for (String x : colTypes) {
				t.col_index.put(x, i++);
			}
			Serialize(strTableName, t);
			tables.put(strTableName, t); // Should be serialized/////////////////////////////////////// // NO NO NO
			this.writeIntoCSV(strTableName, strClusteringKeyColumn, htblColNameType);
		}
	}

	/**
	 * The writeIntoCSV method takes the table name and the hashtable containing
	 * the columns' names and data types as parameters and add them in the
	 * metadata file in csv format.
	 *
	 * @param strTableName
	 * @param strClusteringKeyColumn
	 * @param htblColNameType
	 * @throws DBAppException
	 */

	// An exception should be thrown if there is no clustering key !!? //George
	public void writeIntoCSV(String strTableName, String strClusteringKeyColumn,
			Hashtable<String, String> htblColNameType) throws DBAppException {

		Set<String> colTypes = htblColNameType.keySet();
		String s = "";
		FileWriter fw;
		BufferedWriter bw;
		try {
			fw = new FileWriter(file.getAbsoluteFile(), true);
			bw = new BufferedWriter(fw);
			for (String key : colTypes) {
				if (key == strClusteringKeyColumn) {
					s += (strTableName + "," + key + "," + htblColNameType.get(key) + ",True,False" + "\n");
				} else {
					s += (strTableName + "," + key + "," + htblColNameType.get(key) + ",False,False" + "\n");

				}
			}
			bw.write(s);
			close(bw, fw);

		} catch (IOException e) {
			DBAppException exception = new DBAppException("The meta data file does not exist");
			throw (exception);
		}

	}

	private void close(BufferedWriter bw, FileWriter fw) {
		try {
			if (bw != null) {
				bw.close();
				bw = null;
			}
			if (fw != null) {
				fw.close();
				fw = null;
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * The readFromCSV method takes the table name as a parameter and returns
	 * the columns' names and data types so we can check the data and their
	 * types upon insertion,update or deletion.
	 *
	 * @param tableName
	 * @return
	 * @throws DBAppException
	 */
	public HashSet<String> myTablesname() throws DBAppException {
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(file));
			HashSet<String> output = new HashSet<String>();
			while (br.ready()) {
				String line = br.readLine();
				String[] values = line.split(",");
				output.add(values[0]);
			}
			return output;
		} catch (FileNotFoundException e) {
			DBAppException exception = new DBAppException("The meta data file does not exist");
			throw (exception);
		} catch (IOException e) {
			DBAppException exception = new DBAppException("The meta data file does not exist");
			throw (exception);

		}

	}

	public Vector<String> readFromCSV(String tableName) throws DBAppException {
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(file));
			Vector<String> output = new Vector<String>();
			while (br.ready()) {
				String line = br.readLine();
				String[] values = line.split(",");
				if (values[0].equals(tableName)) {
					output.add(line);
				}
			}
			return output;
		} catch (FileNotFoundException e) {
			DBAppException exception = new DBAppException("The meta data file does not exist");
			throw (exception);
		} catch (IOException e) {
			DBAppException exception = new DBAppException("The meta data file does not exist");
			throw (exception);

		}

	}

	/**
	 * The deserialize method takes the name of the file and gets the table or
	 * the page as object.
	 *
	 * @param name
	 * @return
	 * @throws DBAppException
	 */
	static Object deserialize(String name) throws DBAppException {
		FileInputStream file;
		try {
			file = new FileInputStream("data/" + name + ".ser");
			ObjectInputStream in = new ObjectInputStream(file);

			// Method for deserialization of object
			Object t = in.readObject();

			in.close();
			file.close();

			return t;
		} catch (FileNotFoundException e) {
			DBAppException exception = new DBAppException(
					"The binary file has not been created !!  or does not exist !!");
			throw (exception);
		} catch (IOException e) {
			// DBAppException exception = new DBAppException(
			// "The binary file has not been created !! or does not exist !!");
			DBAppException exception = new DBAppException(e.getMessage());
			throw (exception);
		} catch (ClassNotFoundException e) {
			DBAppException exception = new DBAppException("The class of ObjectInputStream does not exist");
			throw (exception);
		}
	}

	/**
	 * The Seralize method takes the name of the file and serialize the object
	 * into the hard disk.
	 *
	 * @param name
	 * @param obj
	 * @throws DBAppException
	 */
	public static void Serialize(String name, Serializable obj) throws DBAppException {
		FileOutputStream file;
		try {
			file = new FileOutputStream("data/" + name + ".ser");
			ObjectOutputStream out = new ObjectOutputStream(file);

			out.writeObject(obj);

			out.close();
			file.close();
		} catch (IOException e) {
			DBAppException exception = new DBAppException(
					"The binary file has not been created !!  or does not exist !!");
			throw (exception);
		}
	}

	/**
	 * The check method checks that all the keys in the hashtable are present in
	 * the metadata and that the size of both of them is the same. It also check
	 * the values of the keys if they match those in the metadata.
	 *
	 * @param htblColNameValue
	 * @param v
	 * @return
	 */
	boolean Check(Hashtable<String, Object> htblColNameValue, Vector<String> v) {
		boolean f = true;
		for (int i = 0; i < v.size(); i++) {
			String[] values = v.get(i).split(",");
			if (htblColNameValue.containsKey(values[1])) {
				if (values[2].equals("java.lang.Integer")) {
					f &= (htblColNameValue.get(values[1]) instanceof Integer);
				}
				if (values[2].equals("java.lang.String")) {
					f &= (htblColNameValue.get(values[1]) instanceof String);
				}
				if (values[2].equals("java.lang.Double")) {
					f &= (htblColNameValue.get(values[1]) instanceof Double);
				}
				if (values[2].equals("java.lang.Boolean")) {
					f &= (htblColNameValue.get(values[1]) instanceof Boolean);
				}
				if (values[2].equals("java.util.Date")) {
					f &= (htblColNameValue.get(values[1]) instanceof Date);
				}
				if (values[2].equals("java.awt.Polygon")) {
					f &= (htblColNameValue.get(values[1]) instanceof java.awt.Polygon);
				}
			} else {
				f = false;
			}
		}
		return f; 
	}

	/**
	 * The insertIntoTable method takes parameters: the table name, the tuple to be
	 * inserted in the form of a hashtable. It deserializes the table and loop over
	 * the pages using binary search to find the position of the new tuple in all
	 * pages of the table according to the clustering key. Then, it inserts the new
	 * tuple added to it the current date in the touch date column in its correct
	 * position. If all pages are full, it creates a new page with the extra tuple.
	 * Finally, it serializes the pages one by one after using it and we serialize
	 * the table again at the end.
	 * 
	 * @param strTableName
	 * @param htblColNameValue
	 * @throws DBAppException
	 */

	public void insertIntoTable(String strTableName, Hashtable<String, Object> htblColNameValue) throws DBAppException {

		Table table = tables.get(strTableName);
		Vector<String> cols = readFromCSV(strTableName);
		boolean valid = Check(htblColNameValue, cols);
		if (!valid || htblColNameValue.size() != cols.size()) {
			DBAppException exception = new DBAppException("The input is not valid for insertion");
			throw (exception);
		}
		// creating tuple for insertion
		Vector data = new Vector<Object>();
		Comparable Clust = null;
		int idx = -1;
		for (int i = 0; i < cols.size(); i++) {
			String[] vals = cols.get(i).split(",");
			Object o = htblColNameValue.get(vals[1]);
			if (o instanceof java.awt.Polygon)
				o = (Polygon) o;
			data.add(o);
			if (vals[3].toLowerCase().equals("true")) {
				Clust = (Comparable) htblColNameValue.get(vals[1]);
				idx = i;
			}
		}
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		data.add(timestamp);

		// We do not enter this loop now it is ok
		// //////////////////////////////////////////////////////////////
		boolean done = false;
		for (int i = 1; data != null && i <= table.size; i++) {
			Table.Page Page = (Table.Page) deserialize(table.name + " " + i);
			// loop over records in a page
			int loc = BinarySearch(Page, Clust, idx);
			if (loc != -1 || i == table.size) {
				if (loc == -1) // this is for the biggest element and last bag
					loc = Page.Records.size();
				Page.Records.insertElementAt(data, loc); // change cur to page.records
				insert_in_allindex(table, data, i);
				// done = true;
				data = null;
				if (Page.Records.size() > Page.MaxSize) {
					data = Page.Records.remove(Page.MaxSize);
					delete_in_allindex(table, data, i);
					Clust = (Comparable) data.get(idx);
				}
			}
			// serialization
			Serialize(table.name + " " + i, Page);
		}
		if (data != null) {
			int size = Integer.parseInt(getPropValues("MaximumRowsCountingPage"));
			Table.Page NewPage = new Table.Page(size);
			NewPage.Records.add(data);
			table.size++;
			// George .. because first element in each page was not in the bptree
			insert_in_allindex(table, data, table.size);
			String name = table.name + " " + (table.size);
			// serialization
			Serialize(table.name + " " + table.size, NewPage);
		}
		Serialize(table.name, table); /// table serialization samer touch // ahmed drop it down after the above condithion
	}

	public void insert_in_allindex(Table table, Vector<Object> recordinseted, int pageofRec) throws DBAppException {
		Set<String> colTypes = table.indexperCol.keySet();
		for (String x : colTypes) {
			Object index = table.indexperCol.get(x);
			if (index instanceof BPTree) {
				BPTree btree = (BPTree) index;
				Comparable key = (Comparable) recordinseted.get(table.col_index.get(x));
				btree.insert(key, new Ref(pageofRec));
			} else { // for Rtree

			}
		}
	}

	public void delete_in_allindex(Table table, Vector<Object> recordinseted, int pageofRec) throws DBAppException {
		Set<String> colTypes = table.indexperCol.keySet();
		for (String x : colTypes) {
			Object index = table.indexperCol.get(x);
			if (index instanceof BPTree) {
				BPTree btree = (BPTree) index;
				Comparable key = (Comparable) recordinseted.get(table.col_index.get(x));
				btree.delete(key, new Ref(pageofRec)); // we need to pass pageofrec to delete specific record
			} else { // this for R tree
				RTree rtree = (RTree) index;
				Comparable key = (Comparable) recordinseted.get(table.col_index.get(x));
				rtree.delete((Polygon) key, new Ref(pageofRec));
			}
		}
	}

	/**
	 * The updateTable method searches for all the tuples that have same clustering
	 * key and update the values according to the hashtable. We use binary search to
	 * find the tuples with the given clustering key.We use the castKey method to
	 * know the data type of the strClusteringKey to use it to find the tuples that
	 * should be updated. We update the date touch of the updated tuples as well as
	 * the values in the hashtable.
	 * 
	 * @param strTableName
	 * @param strClusteringKey
	 * @param htblColNameValue
	 * @throws Exception
	 */
	// Ahmed and Ashraf
	public void updateTable(String strTableName, String strClusteringKey, Hashtable<String, Object> htblColNameValue)
			throws Exception {
		Table table = tables.get(strTableName);
		Vector<String> cols = readFromCSV(strTableName);
		boolean valid = Check(htblColNameValue, cols);
		if (!valid && htblColNameValue.size() + 1 != cols.size()) {
			DBAppException exception = new DBAppException("The Data that sould be Updated is not valid  !! ");
			throw (exception);
		}
		Vector data = new Vector<Object>();
		int idx = -1;
		Comparable Clust = null;
		String ClustName = "";
		for (int i = 0; i < cols.size(); i++) {
			String[] vals = cols.get(i).split(",");
			if (vals[3].toLowerCase().equals("true")) {
				idx = i;
				Clust = castKey(strClusteringKey, vals[2]);
				ClustName = vals[1];
				data.add(Clust);
			} else {
				data.add(htblColNameValue.get(vals[1]));
			}
		}
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		data.add(timestamp);
	// using BPTree if found
		if (table.indexperCol.containsKey(ClustName)) {
			BPTree tree = (BPTree) (table.indexperCol.get(ClustName));
			TreeSet<Ref> ts = tree.search(Clust);
			for (Ref r : ts) {
				Page Page = (Page) deserialize(table.name + " " + r.getPage());
				int loc = BinarySearch(Page, Clust, idx);
				int num = r.getPage();
				if (loc != -1) {
					for (int j = loc; j < Page.Records.size(); j++) {
						Vector<Object> cur = Page.Records.get(j);
						Comparable key = (Comparable) cur.get(idx);
						if (key.compareTo(Clust) == 0) { // found the Position
							Page.Records.remove(j);
							
							delete_in_allindex(table, data, num);
							
							Page.Records.insertElementAt(data, j); // change cur
																	// to
																	// //
																	// page.records
							insert_in_allindex(table, data, num);
						} else {
							break;
						}
					}
					Serialize(table.name + " " + num, Page);
				}
			}
			Serialize(table.name, table);
			return;
		}
		
		// linear update
		boolean done = false;
		for (int i = 1; i <= table.size && !done; i++) {
			Table.Page Page = (Table.Page) deserialize(table.name + " " + i);
			// binary search over records in a page
			int loc = BinarySearch(Page, Clust, idx);
			if (loc != -1) {
				for (int j = loc; j < Page.Records.size() && !done; j++) {
					Vector<Object> cur = Page.Records.get(j);
					Comparable key = (Comparable) cur.get(idx);
					if (key.compareTo(Clust) == 0) { // found the Position
						Page.Records.remove(j);
						delete_in_allindex(table, data, i);
						Page.Records.insertElementAt(data, j); // change cur to
																// page.records
						insert_in_allindex(table, data, i);
					} else {
						done = true;
					}
				}
			}
			// serialization
			Serialize(table.name + " " + i, Page);
		}
		Serialize(table.name, table);
	}

	/**
	 * The castKey method takes the clustering key as string in the updateTable
	 * method and get its type then returns it as Comparable, so we can use it to
	 * find the position of the tuples that should be updated.
	 * 
	 * @param value
	 * @param type
	 * @return
	 */
	public static Comparable castKey(String value, String type) {
		if (type.equals("java.lang.Integer")) {
			return Integer.parseInt(value);
		}
		if (type.equals("java.lang.String")) {
			return value;
		}
		if (type.equals("java.lang.Double")) {
			Double.parseDouble(value);
		}
		if (type.equals("java.lang.Boolean")) {
			Boolean.parseBoolean(value);
		}
		if (type.equals("java.util.Date")) {
			// change it because it is Depricated. ///////////////////////////////
			return Date.parse(value);
		}
		if (type.equals("java.awt.Polygon")) {
			return CreatePolygon(value);
		}
		return null;
	}

	public static boolean isInstatceOf(Object value, String type) {
		if (type.equals("java.lang.Integer")) {
			return value instanceof Integer;
		}
		if (type.equals("java.lang.String")) {
			return value instanceof String;
		}
		if (type.equals("java.lang.Double")) {
			return value instanceof Double;
		}
		if (type.equals("java.lang.Boolean")) {
			return value instanceof Boolean;
		}
		if (type.equals("java.util.Date")) {
			return value instanceof Date;
		}
		if (type.equals("java.awt.Polygon")) {
			return value instanceof Polygon;
		}
		return false;
	}

	/**
	 * The testPages method prints the records in every page in a certain table.
	 * 
	 * @param tablename
	 * @throws DBAppException
	 */
	public void testpage(String tablename) throws DBAppException { // Ahmed and Ashraf
		Table table = tables.get(tablename);
		System.out.println(table.size);
		for (int i = 1; i <= table.size; i++) {
			Table.Page Page = (Table.Page) deserialize("data/" + table.name + " " + i + ".ser");
			for (int j = 0; j < Page.Records.size(); j++) {
				System.out.println(Page.Records.get(j));
			}
			System.out.println("End of Page");
		}
	}

	/**
	 * The BinarySearch method is used to search for the position of tuple based on
	 * the clustering key upon which data is sorted. It is used in the insert and
	 * update methods. It finds the position of tuples in O(log n) instead of O(n)
	 * in linear search.
	 * 
	 * @param page
	 * @param clust
	 * @param idx
	 * @return
	 */
	public int BinarySearch(Table.Page page, Comparable clust, int idx) { // Ahmed and Ashraf
		int lo = 0;
		int hi = page.Records.size() - 1;
		int ans = -1;

		while (lo <= hi) {
			int mid = (lo + hi) >> 1;
			if (clust.compareTo(page.Records.get(mid).get(idx)) <= 0) {
				ans = mid;
				hi = mid - 1;
			} else {
				lo = mid + 1;
			}
		}
		return ans;
	}

	public static Polygon CreatePolygon(String coo) {
		String c = coo.replaceAll(",()", " ");
		StringTokenizer st = new StringTokenizer(c);
		int size = st.countTokens() / 2;
		int[] x = new int[size];
		int[] y = new int[size];
		for (int i = 0; i < size; i++) {
			x[i] = Integer.parseInt(st.nextToken());
			y[i] = Integer.parseInt(st.nextToken());
		}
		return new Polygon(x, y, size);
	}

	// Ahmed some modifications
	public void init() throws DBAppException {
		fill_DB_hash();
	}

	public void fill_DB_hash() throws DBAppException {
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(file));
			br.readLine();
			br.readLine();
			Table t = null;
			while (br.ready()) {
				String line = br.readLine();
				String[] values = line.split(",");
				if (!tables.containsKey(values[0])) {
					t = (Table) deserialize(values[0]);
					tables.put(values[0], t);
				}
				// filltablehash(t, values); // already serialized
			}
		} catch (FileNotFoundException e) {
			DBAppException exception = new DBAppException("The meta data file does not exist");
			throw (exception);
		} catch (IOException e) {
			DBAppException exception = new DBAppException("The meta data file does not exist");
			throw (exception);

		}
	}

	public void filltablehash(Table table, String[] values) throws DBAppException {
		if (values[4].equals("True")) {
			BPTree tree = (BPTree) deserialize(table.name + values[1]);
			table.indexperCol.put(values[1], tree);
		}
	}

	public String getPropValues(String Key) throws DBAppException {
		Properties prop = new Properties();
		String propFileName = "config/DBApp.properties";
		FileInputStream inputStream;
		try {
			inputStream = new FileInputStream(propFileName);

		} catch (IOException e) {
			throw new DBAppException("property file '" + propFileName + "' not found in the classpath");
		}
		try {
			prop.load(inputStream);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return prop.getProperty(Key);
	}

///////////////////////////////////////////////////////////////////////////// mile 2 ///////////////////////////

	public void createBTreeIndex(String strTableName, String strColName) throws DBAppException, IOException {
		Table t = tables.get(strTableName);
		if (!t.indexperCol.containsKey(strColName)) {
			int NodeSize = Integer.parseInt(getPropValues("NodeSize"));
			BPTree index = new BPTree(NodeSize, strTableName + strColName);
			index.root.btree = index;
			index.root.Serialize();
			t.indexperCol.put(strColName, index);
			addIndexToCSV(strTableName, strColName);// here we need to do thaaaaaaaat //DONE-GEORGE
			fillindex(t, index, t.col_index.get(strColName));
			DBApp.Serialize("B+tree/" + index.IndexName, index);
			DBApp.Serialize(t.name, t);
		} else {
			DBAppException ex = new DBAppException("the column already has an index on it");
			throw (ex);
		}
	}

	public Vector<String> readAllCSV() throws FileNotFoundException {
		Vector<String> out = new Vector<String>();
		Scanner sc = new Scanner(file);
		while (sc.hasNextLine()) {
			out.add(sc.nextLine());
		}
		return out;
	}

	public void createRTreeIndex(String strTableName, String strColName) throws DBAppException, IOException {
		Table t = tables.get(strTableName);
		if (!t.indexperCol.containsKey(strColName)) {
			int NodeSize = Integer.parseInt(getPropValues("NodeSize"));
			RTree index = new RTree(NodeSize, strTableName + strColName);
			index.root.btree = index;
			index.root.Serialize();
			t.indexperCol.put(strColName, index);
			addIndexToCSV(strTableName, strColName);// here we need to do
													// thaaaaaaaat //DONE-GEORGE
			fillindex(t, index, t.col_index.get(strColName));
			DBApp.Serialize("B+tree/" + index.IndexName, index);
			DBApp.Serialize(t.name, t);
		} else {
			DBAppException ex = new DBAppException("the column already has an index on it");
			throw (ex);
		}
	}
	
	public void addIndexToCSV(String tableName, String colName) throws IOException {
		Vector<String> csv = readAllCSV();
		int index = -1;
		String newLine = "";
		for (int i = 0; i < csv.size(); i++) {
			String[] lineArray = csv.get(i).split(",");
			if (lineArray[0].equals(tableName) && lineArray[1].equals(colName)) {
				index = i;
				lineArray[lineArray.length - 1] = "TRUE";
				newLine += lineArray[0];
				for (int j = 1; j < lineArray.length; j++) {
					newLine += ("," + lineArray[j]);
				}
				break;
			}
		}
		csv.remove(index);
		csv.add(index, newLine);
		String newCSV = "";
		FileWriter fw = new FileWriter(file);
		BufferedWriter bw = new BufferedWriter(fw);
		for (int i = 0; i < csv.size(); i++) {
			newCSV += (csv.get(i) + "\n");
		}
		bw.write(newCSV);
		bw.close();
		fw.close();
	}

//
//	public void modifymetaforindex(String strTableName, String strColName) throws DBAppException {
//		String s = "";
//		BufferedReader br;
//		BufferedWriter bw;
//		try {
//			br = new BufferedReader(new FileReader(file));
//			s += br.readLine() + "\n";
//			while (br.ready()) {
//				String line = br.readLine();
//				String[] values = line.split(",");
//				if (values[0].equals(strTableName) && values[1].equals(strColName)) {
//					values[4] = "True";
//					StringBuilder sb = new StringBuilder();
//					sb.append(values[0]);
//					for (int i = 1; i < values.length; i++) {
//						sb.append(',' + values[i]);
//					}
//					if (br.ready())
//						s += sb.toString() + "\n";
//					else
//						s += sb.toString();
//				} else {
//					if (br.ready())
//						s += line + "\n";
//					else
//						s += line;
//				}
//			}
//			br.close();
//			br = null;
//			System.out.println(file.delete());
//			file = new File("data/metadata.csv");
////			bw = new BufferedWriter(new FileWriter(file, true));
////			bw.write(s);
////			bw.close();
////			bw=null;
////			System.out.println("Successfully created");
//		} catch (IOException e) {
//			DBAppException exception = new DBAppException("The meta data file does not exist");
//			throw (exception);
//		}
//	}

	public void fillindex(Table table, BPTree index, int col_index) throws DBAppException {
		for (int i = 1; i <= table.size; i++) {
			Page page = (Page) deserialize(table.name + " " + i);
			for (Vector<Object> x : page.Records) {
				Ref ref = new Ref(i);
				index.insert((Comparable) x.get(col_index), ref);
			}

		}
	}
	////////////// SELECT ///////////////////////////

	public static int operatorsConvention(String opr) {
		if (opr.equals(">"))
			return 1;
		else if (opr.equals(">="))
			return 2;
		else if (opr.equals("<"))
			return 3;
		else if (opr.equals("<="))
			return 4;
		else if (opr.equals("!="))
			return 5;
		else if (opr.equals("="))
			return 6;
		return 6;
	}

	public static boolean operatorsConvention(String opr, Comparable obj1, Comparable obj2) {
		if (opr.equals(">"))
			return obj1.compareTo(obj2) > 0;
		else if (opr.equals(">="))
			return obj1.compareTo(obj2) >= 0;
		else if (opr.equals("<"))
			return obj1.compareTo(obj2) < 0;
		else if (opr.equals("<="))
			return obj1.compareTo(obj2) <= 0;
		else if (opr.equals("!="))
			return obj1.compareTo(obj2) != 0;
		else if (opr.equals("="))
			return obj1.compareTo(obj2) == 0;
		return true;
	}

	public static <T> TreeSet union(TreeSet<T> ts1, TreeSet<T> ts2) {
		for (T x : ts2)
			ts1.add(x);
		return ts1;
	}

	public static <T> TreeSet inter(TreeSet<T> ts1, TreeSet<T> ts2) {
		TreeSet<T> ans = new TreeSet<T>();
		for (T x : ts2)
			if (ts1.contains(x))
				ans.add(x);
		return ans;
	}

	public static boolean checkSelect(SQLTerm[] arrSQLTerms, String[] strarrOperators, Vector<Object> rec,
			Table table) {
		HashMap<String, Integer> map = table.col_index;
		System.out.println(map);
		SQLTerm sql = arrSQLTerms[0];
		Integer idx = map.get(sql._strColumnName);
		boolean ans = operatorsConvention(sql._strOperator, (Comparable) rec.get(idx), sql._objValue);
		for (int i = 1; i < arrSQLTerms.length; i++) {
			sql = arrSQLTerms[i];
			idx = map.get(sql._strColumnName);
			boolean ansx = operatorsConvention(sql._strOperator, (Comparable) rec.get(idx), sql._objValue);
			if (strarrOperators[i - 1].equals("AND"))
				ans &= ansx;
			else if (strarrOperators[i - 1].equals("OR"))
				ans |= ansx;
			else
				ans ^= ansx;
		}
		return ans;
	}

	public Iterator<Vector> selectFromTable(SQLTerm[] arrSQLTerms, String[] strarrOperators) throws DBAppException {
		// Table table= (Table) deserialize("data/" +
		// arrSQLTerms[0]._strTableName+".ser");
		Table table = tables.get(arrSQLTerms[0]._strTableName);
		HashMap<String, Object> map = table.indexperCol;
		boolean hasBtree = true;
		TreeSet<Ref> ans = new TreeSet();
		SQLTerm sql = arrSQLTerms[0];
		BPTree btree;
		if (!map.containsKey(sql._strColumnName))
			hasBtree = false;
		else {
			btree = (BPTree) map.get(sql._strColumnName);
			int dif = operatorsConvention(sql._strOperator);
			ans = btree.search(sql._objValue, dif);
			for (int i = 1; i < arrSQLTerms.length; i++) {
				sql = arrSQLTerms[i];
				if (!map.containsKey(sql._strColumnName)) {
					if (strarrOperators[i - 1].equals("AND"))
						continue;
					hasBtree = false;
					break;
				}
				btree = (BPTree) map.get(sql._strColumnName);
				dif = operatorsConvention(sql._strOperator);
				if (strarrOperators[i - 1].equals("AND"))
					ans = inter(ans, btree.search(sql._objValue, dif));
				else
					ans = union(ans, btree.search(sql._objValue, dif));

			}
		}
		HashSet<Vector> records = new HashSet<Vector>();
		if (hasBtree) {
			for (Ref x : ans) {
				Page p = (Page) deserialize(table.name + " " + x.getPage());
				for (Vector rec : p.Records)
					if (checkSelect(arrSQLTerms, strarrOperators, rec, table))
						records.add(rec);
			}
		} else {
			for (int i = 1; i <= table.getSize(); i++) {
				Page p = (Page) deserialize(table.name + " " + i);
				for (Vector rec : p.Records)
					if (checkSelect(arrSQLTerms, strarrOperators, rec, table))
						records.add(rec);
			}
		}
		return records.iterator();
	}
}


