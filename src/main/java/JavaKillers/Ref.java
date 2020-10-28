package JavaKillers;

import java.io.Serializable;

public class Ref implements Serializable,Comparable {

	/**
	 * This class represents a pointer to the record. It is used at the leaves of
	 * the B+ tree
	 */
	private static final long serialVersionUID = 1L;
	private int pageNo;

	
	public Ref(int pageNo) {
		this.pageNo = pageNo;
	}

	/**
	 * @return the page at which the record is saved on the hard disk
	 */
	public int getPage() {
		return pageNo;
	}

	public int compareTo(Object o) {
		return this.pageNo-((Ref)o).pageNo;
	}
	
	public String toString() {
		return this.pageNo+"";
	}

}
