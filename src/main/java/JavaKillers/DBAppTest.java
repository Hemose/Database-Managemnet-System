package JavaKillers;

import java.util.Arrays;
import java.util.Hashtable;

import javax.crypto.spec.DESedeKeySpec;

import JavaKillers.Table.Page;

public class DBAppTest {

	public static void main(String[] args) throws Exception {

//		System.out.println(null instanceof Integer);
		DBApp db = new DBApp();

//		Table t = (Table) db.deserialize("data/professor.ser");
//		Page p = (Page) db.deserialize("data/Professor 1.ser");
//		System.out.println(p.Records.toString());
//		System.out.println(t.indexperCol.get("name"));
//		System.out.println(t.indexperCol.get("department"));


//		System.out.println(t.indexperCol.get("name"));
//		System.out.println(t.indexperCol.get("department"));
//		
//		for(int i=2;i<15;i++) {
//			Hashtable<String,Object> conditions = new Hashtable<String, Object>();
//			conditions.put("name","Samer"+i);
//			conditions.put("department","MET"+i);
//			db.deleteFromTable("Professor",conditions);
//		}
//		System.out.println(t.indexperCol.get("name"));
//		System.out.println(t.indexperCol.get("department"));

//		Page p = (Page) db.deserialize("data/Professor 1.ser");
//		System.out.println(p.Records.toString());

//		Hashtable htblColNameType = new Hashtable();
//		htblColNameType.put("id", "java.lang.Integer");
//		htblColNameType.put("name", "java.lang.String");
//		htblColNameType.put("department", "java.lang.String");
//		db.createTable("Professor", "id", htblColNameType);

//		

//		db.addIndexToCSV("Student","name");
//
		
		
		
//		String s = "Employee";
//		db.readFromCSV(s);
		// String[] values = (db.readFromCSV()).split(",");
//		for(int i=0;i<values.length;i++) {
//			System.out.println(values[i]);
//		}
//
//		db.testpage("Employee");
//		Hashtable<String,Object> htblColNameValue = new Hashtable();
//		htblColNameValue.put("name","Ashraf");
//		Vector<String> cols = db.readFromCSV("Employee");
//		db.deleteFromTable("Employee", htblColNameValue);
//		db.testpage("Employee");
////		// System.out.println(htblColNameValue.get("department") instanceof Integer);
//		db.updateTable("Employee", "100", htblColNameValue);

////		
//		for(int i=0;i<300;i++) {
//			 Hashtable<String,Object> htblColNameValue = new Hashtable();
//				htblColNameValue.put("id", i);
//				htblColNameValue.put("name", "georgetoo");
//				htblColNameValue.put("department", "A-");
//				// System.out.println(htblColNameValue.get("department") instanceof Integer);
//				db.insertIntoTable("Student", htblColNameValue);
//		}
		
//		Hashtable<String,Object> conditions = new Hashtable<String, Object>();
//		conditions.put("name","ashraf1");
//		db.deleteFromTable("ashraf",conditions);

//		Hashtable htblColNameType = new Hashtable();
//		htblColNameType.put("id", "java.lang.Integer");
//		htblColNameType.put("name", "java.lang.String");
//		htblColNameType.put("email", "java.lang.String");
//		db.createTable("ashraf5", "id", htblColNameType);

//		for (int i = 0; i < 4; i++) {
//			for (int j = 0; j < 3; j++) {
//				Hashtable<String, Object> values = new Hashtable<String, Object>();
//				values.put("name", "ashraf" + i);
//				values.put("email", "MET" + i);
//				values.put("id", i);
//				db.insertIntoTable("ashraf5", values);
//
//			}
//		}
//		
		
//		db.createBTreeIndex("ashraf5", "name");
//		BPTree x = (BPTree) db.tables.get("ashraf5").indexperCol.get("name");
//		x.toString();
		
//		BPTreeNode d=x.root.deserialize(19);
//		System.out.println(((BPTreeInnerNode)d).numberOfKeys);
//		System.out.println(Arrays.toString(((BPTreeInnerNode)d).children));
//		System.out.println(Arrays.toString(((BPTreeInnerNode)d).keys));

//		db.testpage("ahmed3");

	}
}
