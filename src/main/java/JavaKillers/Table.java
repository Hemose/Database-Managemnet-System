package JavaKillers;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

public class Table implements Serializable {

	String name;
	int size;
	HashMap<String, Object> indexperCol; // object for b+ and R indexes
	// we need a hash for Rtree
	HashMap<String, Integer> col_index;
//	transient Vector<Page> Pages;

	public Table(String name) {
		this.name = name;
//		Pages = new Vector<Page>();
		size = 0;
		indexperCol = new HashMap<String, Object>();
		col_index = new HashMap<String, Integer>();
	}

//
//	void CreatePage() {
//		Page p = new Page(20);//maxsize
//		size++;
//	}

	public int getSize() {
		return size;
	}

	static class Page implements Serializable {
		Vector<Vector<Object>> Records;
		int MaxSize;

		public Page(int maxsize) {
			Records = new Vector<Vector<Object>>();
			MaxSize = maxsize;
		}
	}

}
