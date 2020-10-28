package JavaKillers;

import java.io.File;
import java.io.Serializable;
import java.util.TreeSet;
import java.util.Vector;

public class BPTreeLeafNode<T extends Comparable<T>> extends BPTreeNode<T> implements Serializable {

	private Ref[] records;
	private long next;
	private long parent;

	@SuppressWarnings("unchecked")
	public BPTreeLeafNode(int n, BPTree x, long p) {
		super(n, x);
		keys = new Comparable[n];
		records = new Ref[n];
		parent = p;
	}

	public void setParent(long p) {
		parent = p;
	}

	public long getParent() {
		return parent;
	}

	/**
	 * @return the next leaf node
	 * @throws DBAppException
	 */
	public BPTreeLeafNode<T> getNext() throws DBAppException {
		if (next == 0)
			return null;
		return (BPTreeLeafNode<T>) this.deserialize(next);
	}

	/**
	 * sets the next leaf node
	 * 
	 * @param node the next leaf node
	 */
	public void setNext(BPTreeLeafNode<T> node) {
		if (node != null)
			this.next = node.getSerialVersionUID();
	}

	/**
	 * @param index the index to find its record
	 * @return the reference of the queried index
	 */
	public Ref getRecord(int index) {
		return records[index];
	}

	/**
	 * sets the record at the given index with the passed reference
	 * 
	 * @param index           the index to set the value at
	 * @param recordReference the reference to the record
	 */
	public void setRecord(int index, Ref recordReference) {
		records[index] = recordReference;
	}

	/**
	 * @return the reference of the last record
	 */
	public Ref getFirstRecord() {
		return records[0];
	}

	/**
	 * @return the reference of the last record
	 */
	public Ref getLastRecord() {
		return records[numberOfKeys - 1];
	}

	/**
	 * finds the minimum number of keys the current node must hold
	 */
	public int minKeys() {
		if (this.isRoot())
			return 1;
		return (order + 1) / 2;
	}

	/**
	 * insert the specified key associated with a given record refernce in the B+
	 * tree
	 * 
	 * @throws DBAppException
	 */
	public PushUp<T> insert(T key, Ref recordReference, BPTreeInnerNode<T> parent, int ptr) throws DBAppException {
		if (this.isFull()) {
			BPTreeNode<T> newNode = this.split(key, recordReference);
			Comparable<T> newKey = newNode.getFirstKey();
			return new PushUp<T>(newNode, newKey);
		} else {
			int index = 0;
			while (index < numberOfKeys && getKey(index).compareTo(key) <= 0)
				++index;
			this.insertAt(index, key, recordReference);
			return null;
		}
	}

	/**
	 * inserts the passed key associated with its record reference in the specified
	 * index
	 * 
	 * @param index           the index at which the key will be inserted
	 * @param key             the key to be inserted
	 * @param recordReference the pointer to the record associated with the key
	 */
	private void insertAt(int index, Comparable<T> key, Ref recordReference) {
		for (int i = numberOfKeys - 1; i >= index; --i) {
			this.setKey(i + 1, getKey(i));
			this.setRecord(i + 1, getRecord(i));
		}

		this.setKey(index, key);
		this.setRecord(index, recordReference);
		++numberOfKeys;
	}

	/**
	 * splits the current node
	 * 
	 * @param key             the new key that caused the split
	 * @param recordReference the reference of the new key
	 * @return the new node that results from the split
	 * @throws DBAppException
	 */
	public BPTreeNode<T> split(T key, Ref recordReference) throws DBAppException {
		int keyIndex = this.findIndex(key);
		int midIndex = numberOfKeys / 2;
		if ((numberOfKeys & 1) == 1 && keyIndex > midIndex) // split nodes evenly
			++midIndex;

		int totalKeys = numberOfKeys + 1;
		// move keys to a new node
		BPTreeLeafNode<T> newNode = new BPTreeLeafNode<T>(order, this.btree, -1);
		for (int i = midIndex; i < totalKeys - 1; ++i) {
			newNode.insertAt(i - midIndex, this.getKey(i), this.getRecord(i));
			numberOfKeys--;
		}
		// insert the new key
		if (keyIndex < totalKeys / 2) {
			this.insertAt(keyIndex, key, recordReference);
		} else
			newNode.insertAt(keyIndex - midIndex, key, recordReference);

		// set next pointers

		newNode.setNext(this.getNext());
		this.setNext(newNode);

		return newNode;
	}

	/**
	 * finds the index at which the passed key must be located
	 * 
	 * @param key the key to be checked for its location
	 * @return the expected index of the key
	 */
	public int findIndex(T key) {
		for (int i = 0; i < numberOfKeys; ++i) {
			int cmp = getKey(i).compareTo(key);
			if (cmp > 0)
				return i;
		}
		return numberOfKeys;
	}

	/**
	 * returns the record reference with the passed key and null if does not exist
	 * 
	 * @throws DBAppException
	 */
	@Override
	public TreeSet<Ref> search(T key) throws DBAppException {
		TreeSet<Ref> pages = new TreeSet<Ref>();
		pages.addAll(getPagesInNode(key));
		BPTreeLeafNode<T> currentNode = this;
		while (currentNode.next != 0) {
			BPTreeLeafNode<T> node = currentNode.getNext();
			TreeSet<Ref> newPages = node.getPagesInNode(key);
			pages.addAll(newPages);
			currentNode = node;
		}
		return pages;
	}

	public TreeSet<Ref> getPagesInNode(T key) {
		TreeSet<Ref> pages = new TreeSet<Ref>();
		for (int i = 0; i < numberOfKeys; i++) {
			if (this.getKey(i).compareTo(key) == 0) {
				pages.add(records[i]);
			} else if (this.getKey(i).compareTo(key) > 0) {
				break;
			}
		}
		return pages;
	}

	public TreeSet<Ref> search(T key, int dif) throws DBAppException {
		System.out.println("Leaf Node");
		System.out.println("First Key: " + this.getFirstKey());
		TreeSet<Ref> pages = new TreeSet<Ref>();
		pages.addAll(getPagesInNode(key));
		BPTreeLeafNode<T> currentNode = this;
//		System.out.println(pages.size());
//		System.out.println(currentNode.next);
		int count = 0;

		while (currentNode.next != 0) {
			System.out.println(currentNode.next);
			BPTreeLeafNode<T> node = (BPTreeLeafNode<T>) deserialize(currentNode.next);
			TreeSet<Ref> newPages = node.getPagesInNode(key);
			pages.addAll(newPages);
			currentNode = node;
			count++;
//			System.out.println(pages.size());
		}
		System.out.println("Count: " + count);
		return pages;

//		for (int i = 0; i < numberOfKeys; ++i)
//			if (this.getKey(i).compareTo(key) == 0)
//				return this.getRecord(i);
//		return null;

	}

	public TreeSet<Ref> getPagesInNode(T key, int dif) {
		TreeSet<Ref> pages = new TreeSet<Ref>();
		if (dif == 1) {
			for (int i = 0; i < numberOfKeys; i++)
				if (this.getKey(i).compareTo(key) > 0)
					pages.add(records[i]);
		} else if (dif == 2) {

			for (int i = 0; i < numberOfKeys; i++)
				if (this.getKey(i).compareTo(key) >= 0)
					pages.add(records[i]);

		} else if (dif == 3) {
			for (int i = 0; i < numberOfKeys; i++)
				if (this.getKey(i).compareTo(key) < 0)
					pages.add(records[i]);
				else
					break;
		} else if (dif == 4) {
			for (int i = 0; i < numberOfKeys; i++)
				if (this.getKey(i).compareTo(key) <= 0)
					pages.add(records[i]);
				else
					break;
		} else if (dif == 5) {
			for (int i = 0; i < numberOfKeys; i++)
				if (this.getKey(i).compareTo(key) != 0)
					pages.add(records[i]);
		} else if (dif == 6) {
			for (int i = 0; i < numberOfKeys; i++) {
				if (this.getKey(i).compareTo(key) == 0) {
					pages.add(records[i]);
					// System.out.println(records[i].getPage());
				} else if (this.getKey(i).compareTo(key) > 0) {
					break;
				}
			}
		}

		return pages;
	}

	/**
	 * delete the passed key from the B+ tree
	 * 
	 * @throws DBAppException
	 */

	// we need to updata delete for dublicates

	public boolean delete(T key, BPTreeInnerNode<T> parent, int ptr, Ref refs) throws DBAppException {
		while (true) {
			for (int i = 0; i < numberOfKeys; ++i) {
				if (keys[i].compareTo(key) == 0 && records[i].compareTo(refs) == 0) {
					this.deleteAt(i);
					if (i == 0 && ptr > 0) {
						// update key at parent
						parent.setKey(ptr - 1, this.getFirstKey());
					}
					// check that node has enough keys
					if (!this.isRoot() && numberOfKeys < this.minKeys()) {
						// 1.try to borrow
						if (borrow(parent, ptr)) {
							System.out.println("borrow done");
							return true;
						}
						// 2.merge
						merge(parent, ptr);
						System.out.println("merge done");
					}
					return true;
				}
			}
			BPTreeLeafNode me = getNext();
			if (me == null || me.getFirstKey().compareTo(key) > 0)
				break;
			parent = (BPTreeInnerNode<T>) deserialize(me.parent);
			for (int i = 0; i < parent.children.length; i++) {
				if (parent.children[i] == me.getSerialVersionUID()) {
					ptr = i;
					break;
				}
			}
		}
		return false;
	}

	/**
	 * delete a key at the specified index of the node
	 * 
	 * @param index the index of the key to be deleted
	 */
	public void deleteAt(int index) {
		for (int i = index; i < numberOfKeys - 1; ++i) {
			keys[i] = keys[i + 1];
			records[i] = records[i + 1];
		}
		numberOfKeys--;
	}

	/**
	 * tries to borrow a key from the left or right sibling
	 * 
	 * @param parent the parent of the current node
	 * @param ptr    the index of the parent pointer that points to this node
	 * @return true if borrow is done successfully and false otherwise
	 * @throws DBAppException
	 */

	public boolean borrow(BPTreeInnerNode<T> parent, int ptr) throws DBAppException {
		// check left sibling
		if (ptr > 0) {
			BPTreeLeafNode<T> leftSibling = (BPTreeLeafNode<T>) parent.getChild(ptr - 1);
			if (leftSibling.numberOfKeys > leftSibling.minKeys()) {
				this.insertAt(0, leftSibling.getLastKey(), leftSibling.getLastRecord());
				leftSibling.deleteAt(leftSibling.numberOfKeys - 1);
				parent.setKey(ptr - 1, keys[0]);
				leftSibling.Serialize();
				this.Serialize();
				return true;
			}
		}

		// check right sibling
		if (ptr < parent.numberOfKeys) {
			BPTreeLeafNode<T> rightSibling = (BPTreeLeafNode<T>) parent.getChild(ptr + 1);
			if (rightSibling.numberOfKeys > rightSibling.minKeys()) {
				this.insertAt(numberOfKeys, rightSibling.getFirstKey(), rightSibling.getFirstRecord());
				rightSibling.deleteAt(0);
				parent.setKey(ptr, rightSibling.getFirstKey());
				rightSibling.Serialize();
				this.Serialize();
				return true;
			}
		}
		return false;
	}

	/**
	 * merges the current node with its left or right sibling
	 * 
	 * @param parent the parent of the current node
	 * @param ptr    the index of the parent pointer that points to this node
	 * @throws DBAppException
	 */

	public void merge(BPTreeInnerNode<T> parent, int ptr) throws DBAppException {
		if (ptr > 0) {
			// merge with left
			BPTreeLeafNode<T> leftSibling = (BPTreeLeafNode<T>) parent.getChild(ptr - 1);
			leftSibling.merge(this);
			leftSibling.Serialize();
			File file = new File("data/B+tree/" + this.btree.IndexName + "_" + this.getSerialVersionUID() + ".ser");
			file.delete();
			parent.deleteAt(ptr - 1);
		} else {
			// merge with right
			BPTreeLeafNode<T> rightSibling = (BPTreeLeafNode<T>) parent.getChild(ptr + 1);
			this.merge(rightSibling);
			File file = new File(
					"data/B+tree/" + rightSibling.btree.IndexName + "_" + rightSibling.getSerialVersionUID() + ".ser");
			file.delete();
			parent.deleteAt(ptr);
		}
	}

	/**
	 * merge the current node with the specified node. The foreign node will be
	 * deleted
	 * 
	 * @param foreignNode the node to be merged with the current node
	 * @throws DBAppException
	 */
	public void merge(BPTreeLeafNode<T> foreignNode) throws DBAppException {
		int size = foreignNode.numberOfKeys;
		for (int i = 0; i < size; ++i) {
			this.insertAt(numberOfKeys, foreignNode.getKey(i), foreignNode.getRecord(i));
			foreignNode.numberOfKeys--;
		}
		this.setNext(foreignNode.getNext());
	}
}
