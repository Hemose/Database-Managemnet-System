package JavaKillers;

import java.io.Serializable;
import java.util.TreeSet;
import java.util.Vector;

public abstract class BPTreeNode<T extends Comparable<T>> implements Serializable {

	/**
	 * Abstract class that collects the common functionalities of the inner and leaf
	 * nodes
	 */

	public static long countSerilization = 1l;
	private long serialVersionUID;
	protected Comparable<T>[] keys;
	protected int numberOfKeys;
	protected int order;
	protected int index; // for printing the tree
	private boolean isRoot;
	private static int nextIdx = 0;
	public BPTree btree;

	public BPTreeNode(int order, BPTree x) {
		index = nextIdx++;
		numberOfKeys = 0;
		this.order = order;
		serialVersionUID = x.serialVersionUID++;
		btree = x;
	}

	public long getSerialVersionUID() {
		return serialVersionUID;
	}

	/**
	 * @return a boolean indicating whether this node is the root of the B+ tree
	 */

	public boolean isRoot() {
		return isRoot;
	}

	/**
	 * set this node to be a root or unset it if it is a root
	 * 
	 * @param isRoot the setting of the node
	 */
	public void setRoot(boolean isRoot) {
		this.isRoot = isRoot;
	}

	/**
	 * find the key at the specified index
	 * 
	 * @param index the index at which the key is located
	 * @return the key which is located at the specified index
	 */
	public Comparable<T> getKey(int index) {
		return keys[index];
	}

	/**
	 * sets the value of the key at the specified index
	 * 
	 * @param index the index of the key to be set
	 * @param key   the new value for the key
	 */
	public void setKey(int index, Comparable<T> key) {
		keys[index] = key;
	}

	/**
	 * @return a boolean whether this node is full or not
	 */
	public boolean isFull() {
		return numberOfKeys == order;
	}

	/**
	 * @return the last key in this node
	 */
	public Comparable<T> getLastKey() {
		return keys[numberOfKeys - 1];
	}

	/**
	 * @return the first key in this node
	 */
	public Comparable<T> getFirstKey() {
		return keys[0];
	}

	/**
	 * @return the minimum number of keys this node can hold
	 */
	public abstract int minKeys();

	/**
	 * insert a key with the associated record reference in the B+ tree
	 * 
	 * @param key             the key to be inserted
	 * @param recordReference a pointer to the record on the hard disk
	 * @param parent          the parent of the current node
	 * @param ptr             the index of the parent pointer that points to this
	 *                        node
	 * @return a key and a new node in case of a node splitting and null otherwise
	 * @throws DBAppException
	 */
	public abstract PushUp<T> insert(T key, Ref recordReference, BPTreeInnerNode<T> parent, int ptr)
			throws DBAppException;

	public abstract TreeSet<Ref> search(T key) throws DBAppException;
	public abstract TreeSet<Ref> search(T key,int dif) throws DBAppException;

	
	//public abstract TreeSet<Ref> getAllPages(T key) throws DBAppException;
	/**
	 * delete a key from the B+ tree recursively
	 * 
	 * @param key    the key to be deleted from the B+ tree
	 * @param parent the parent of the current node
	 * @param ptr    the index of the parent pointer that points to this node
	 * @return true if this node was successfully deleted and false otherwise
	 * @throws DBAppException
	 */
	public abstract boolean delete(T key, BPTreeInnerNode<T> parent, int ptr, Ref ref) throws DBAppException;

	/**
	 * A string represetation for the node
	 */
	public String toString() {
		String s = "(" + serialVersionUID + ")";

		s += "[";
		for (int i = 0; i < order; i++) {
			String key = " ";
			if (i < numberOfKeys)
				key = keys[i].toString();

			s += key;
			if (i < order - 1)
				s += "|";
		}
		s += "]";
		return s;
	}

	public void Serialize() throws DBAppException {
		DBApp.Serialize( "B+tree/" +btree.IndexName+"_"+ this.getSerialVersionUID() , this);
	}

	public BPTreeNode deserialize(long numofnode) throws DBAppException {
		BPTreeNode node=(BPTreeNode) DBApp.deserialize( "B+tree/" +btree.IndexName+"_"+ numofnode );
		node.btree=this.btree;
		return node;
	}
	
	
	
	
}
