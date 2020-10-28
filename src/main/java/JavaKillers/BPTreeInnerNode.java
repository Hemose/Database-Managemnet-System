package JavaKillers;

import java.io.File;
import java.io.Serializable;
import java.util.Arrays;
import java.util.TreeSet;
import java.util.Vector;

import org.w3c.dom.Node;

public class BPTreeInnerNode<T extends Comparable<T>> extends BPTreeNode<T> implements Serializable {
	/**
	 * 
	 */	
//	private BPTreeNode<T>[] children;
	public long[] children;

	/**
	 * create BPTreeNode given order.
	 * 
	 * @param n
	 */
	@SuppressWarnings("unchecked")
	public BPTreeInnerNode(int n, BPTree x) {
		super(n, x);
		keys = new Comparable[n];
		children = new long[n + 1];
	}

	/**
	 * get child with specified index
	 * 
	 * @return Node which is child at specified index
	 * @throws DBAppException
	 */
	public BPTreeNode getChild(int index) throws DBAppException {
		try {
			return this.deserialize(children[index]);
		} catch (DBAppException e) {
			DBAppException exception = new DBAppException(
					"The binary file has not been created !!  or does not exist !!");
			throw (exception);
		}
	}

	/**
	 * creating child at specified index
	 */
	public void setChild(int index, long child) {
		children[index] = child;
	}

	/**
	 * get the first child of this node.
	 * 
	 * @return first child node.
	 * @throws DBAppException
	 */
	public long getFirstChild() throws DBAppException {
		return children[0];

	}

	/**
	 * get the last child of this node
	 * 
	 * @return last child node.
	 * @throws DBAppException
	 */
	public long getLastChild() throws DBAppException {
		return children[numberOfKeys];

	}

	/**
	 * @return the minimum keys values in InnerNode
	 */
	public int minKeys() {
		if (this.isRoot())
			return 1;
		return (order + 2) / 2 - 1;
	}

	/**
	 * insert given key in the corresponding index.
	 * 
	 * @param key    key to be inserted
	 * @param Ref    reference which that inserted key is located
	 * @param parent parent of that inserted node
	 * @param ptr    index of pointer in the parent node pointing to the current
	 *               node
	 * @return value to be pushed up to the parent.
	 * @throws DBAppException
	 */
	public PushUp<T> insert(T key, Ref recordReference, BPTreeInnerNode<T> parent, int ptr) throws DBAppException {
		int index = findIndex(key);
		BPTreeNode child = this.getChild(index);
		PushUp<T> pushUp = child.insert(key, recordReference, this, index);
		child.Serialize();
		if (pushUp == null)
			return null;
		
		if (this.isFull()) {
			pushUp.newNode.Serialize();
			BPTreeInnerNode<T> newNode = this.split(pushUp);
			Comparable<T> newKey = newNode.getFirstKey();
			newNode.deleteAt(0, 0);
			return new PushUp<T>(newNode, newKey);
		} else {
			index = 0;
			while (index < numberOfKeys && getKey(index).compareTo(key) < 0)
				++index;
			this.insertRightAt(index, pushUp.key, pushUp.newNode.getSerialVersionUID());
			if (pushUp.newNode instanceof BPTreeLeafNode) {
				System.out.println(((BPTreeLeafNode) pushUp.newNode).getParent() + "xsd");
				((BPTreeLeafNode) pushUp.newNode).setParent(this.getSerialVersionUID());
				System.out.println(((BPTreeLeafNode) pushUp.newNode).getParent() + "xsd");
			}
			pushUp.newNode.Serialize();
			return null;
		}
	}

	/**
	 * split the inner node and adjust values and pointers.
	 * 
	 * @param pushup key to be pushed up to the parent in case of splitting.
	 * @return Inner node after splitting
	 * @throws DBAppException
	 */
	@SuppressWarnings("unchecked")
	public BPTreeInnerNode<T> split(PushUp<T> pushup) throws DBAppException {
		int keyIndex = this.findIndex((T) pushup.key);
		int midIndex = numberOfKeys / 2 - 1;
		if (keyIndex > midIndex) // split nodes evenly
			++midIndex;

		int totalKeys = numberOfKeys + 1;
		// move keys to a new node
		BPTreeInnerNode<T> newNode = new BPTreeInnerNode<T>(order, this.btree);
		for (int i = midIndex; i < totalKeys - 1; ++i) {
			newNode.insertRightAt(i - midIndex, this.getKey(i), children[i + 1]);
			BPTreeNode child = deserialize(this.children[i + 1]);
			if (child instanceof BPTreeLeafNode) {
				((BPTreeLeafNode) child).setParent(newNode.getSerialVersionUID());
				child.Serialize();
			}
			numberOfKeys--;
		}
		newNode.setChild(0, children[midIndex]);
		BPTreeNode child = deserialize(this.children[midIndex]);
		if (child instanceof BPTreeLeafNode) {
			((BPTreeLeafNode) child).setParent(this.getSerialVersionUID());
			child.Serialize();
		}

		// insert the new key
		if (keyIndex < totalKeys / 2) {
			this.insertRightAt(keyIndex, pushup.key, pushup.newNode.getSerialVersionUID());
			child = deserialize(pushup.newNode.getSerialVersionUID());
			if (child instanceof BPTreeLeafNode) {
				((BPTreeLeafNode) child).setParent(this.getSerialVersionUID());
				child.Serialize();
			}
		} else {
			newNode.insertRightAt(keyIndex - midIndex, pushup.key, pushup.newNode.getSerialVersionUID());
			child = deserialize(pushup.newNode.getSerialVersionUID());
			if (child instanceof BPTreeLeafNode) {
				((BPTreeLeafNode) child).setParent(newNode.getSerialVersionUID());
				child.Serialize();
			}
		}

		return newNode;
	}

	/**
	 * find the correct place index of specified key in that node.
	 * 
	 * @param key to be looked for
	 * @return index of that given key
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
	 * insert at given index a given key
	 * 
	 * @param index where it inserts the key
	 * @param key   to be inserted at index
	 * @throws DBAppException
	 */
	private void insertAt(int index, Comparable<T> key) throws DBAppException {
		for (int i = numberOfKeys; i > index; --i) {
			this.setKey(i, this.getKey(i - 1));
			this.setChild(i + 1, children[i]);
		}
		this.setKey(index, key);
		numberOfKeys++;
	}

	/**
	 * insert key and adjust left pointer with given child.
	 * 
	 * @param index     where key is inserted
	 * @param key       to be inserted in that index
	 * @param leftChild child which this node points to with pointer at left of that
	 *                  index
	 * @throws DBAppException
	 */
	public void insertLeftAt(int index, Comparable<T> key, long leftChild) throws DBAppException {
		insertAt(index, key);
		this.setChild(index + 1, children[index]);
		this.setChild(index, leftChild);
	}

	/**
	 * insert key and adjust right pointer with given child.
	 * 
	 * @param index      where key is inserted
	 * @param key        to be inserted in that index
	 * @param rightChild child which this node points to with pointer at right of
	 *                   that index
	 * @throws DBAppException
	 */
	public void insertRightAt(int index, Comparable<T> key, long rightChild) throws DBAppException {
		insertAt(index, key);
		this.setChild(index + 1, rightChild);
	}

	/**
	 * delete key and return true or false if it is deleted or not
	 * 
	 * @throws DBAppException
	 */
	public boolean delete(T key, BPTreeInnerNode<T> parent, int ptr, Ref ref) throws DBAppException {
		boolean done = false;
		BPTreeNode child = null;
		for (int i = 0; child == null && i < numberOfKeys; ++i)
			if (keys[i].compareTo(key) > 0) {
				child = getChild(i);
				done = child.delete(key, this, i, ref);
				if (child.numberOfKeys != 0)
					child.Serialize();
			}

		if (!done) {
			child = getChild(numberOfKeys);
			done = child.delete(key, this, numberOfKeys, ref);
			if (child.numberOfKeys != 0)
				child.Serialize();
		}
		if (numberOfKeys < this.minKeys()) {
			if (isRoot()) {
				BPTreeNode child1 = (BPTreeNode) getChild(0);
				child1.setRoot(true);
				child1.Serialize();
				this.setRoot(false);
				File file = new File("data/B+tree/" + this.btree.IndexName + "_" + this.getSerialVersionUID() + ".ser");
				file.delete();
				return done;
			}
			// 1.try to borrow
			if (borrow(parent, ptr)) {
//				System.out.println("borrow done");
				return done;
			}
			// 2.merge
			merge(parent, ptr);
//			System.out.println("merge done");
		}
		return done;
	}

	/**
	 * borrow from the right sibling or left sibling in case of overflow.
	 * 
	 * @param parent of the current node
	 * @param ptr    index of pointer in the parent node pointing to the current
	 *               node
	 * @return true or false if it can borrow form right sibling or left sibling or
	 *         it can not
	 * @throws DBAppException
	 */
	public boolean borrow(BPTreeInnerNode<T> parent, int ptr) throws DBAppException {
		// check left sibling

		if (ptr > 0) {
			BPTreeInnerNode<T> leftSibling = (BPTreeInnerNode<T>) parent.getChild(ptr - 1);
			if (leftSibling.numberOfKeys > leftSibling.minKeys()) {
				this.insertLeftAt(0, parent.getKey(ptr - 1), leftSibling.getLastChild());
				BPTreeNode child = deserialize(leftSibling.getLastChild());
				if (child instanceof BPTreeLeafNode) {
					((BPTreeLeafNode) child).setParent(this.getSerialVersionUID());
					child.Serialize();
				}
				parent.deleteAt(ptr - 1);
				parent.insertRightAt(ptr - 1, leftSibling.getLastKey(), this.getSerialVersionUID());
				leftSibling.deleteAt(leftSibling.numberOfKeys - 1);
				leftSibling.Serialize();
				this.Serialize();
				return true;
			}
		}

		// check right sibling
		if (ptr < parent.numberOfKeys) {
			BPTreeInnerNode<T> rightSibling = (BPTreeInnerNode<T>) parent.getChild(ptr + 1);
			if (rightSibling.numberOfKeys > rightSibling.minKeys()) {
				this.insertRightAt(this.numberOfKeys, parent.getKey(ptr), rightSibling.getFirstChild());
				BPTreeNode child = deserialize(rightSibling.getFirstChild());
				if (child instanceof BPTreeLeafNode) {
					((BPTreeLeafNode) child).setParent(this.getSerialVersionUID());
					child.Serialize();
				}
				parent.deleteAt(ptr);
				parent.insertRightAt(ptr, rightSibling.getFirstKey(), rightSibling.getSerialVersionUID());
				rightSibling.deleteAt(0, 0);
				rightSibling.Serialize();
				this.Serialize();
				return true;
			}
		}
		return false;
	}

	/**
	 * try to merge with left or right sibling in case of overflow
	 * 
	 * @param parent of the current node
	 * @param ptr    index of pointer in the parent node pointing to the current
	 *               node
	 * @throws DBAppException
	 */
	public void merge(BPTreeInnerNode<T> parent, int ptr) throws DBAppException {
		if (ptr > 0) {
			// merge with left
			BPTreeInnerNode<T> leftSibling = (BPTreeInnerNode<T>) parent.getChild(ptr - 1);
			leftSibling.merge(parent.getKey(ptr - 1), this);
			leftSibling.Serialize();
			parent.deleteAt(ptr - 1);
		} else {
			// merge with right
			BPTreeInnerNode<T> rightSibling = (BPTreeInnerNode<T>) parent.getChild(ptr + 1);
			this.merge(parent.getKey(ptr), rightSibling);
			parent.deleteAt(ptr);
		}
	}

	/**
	 * merge the current node with the passed node and pulling the passed key from
	 * the parent to be inserted with the merged node
	 * 
	 * @param parentKey   the pulled key from the parent to be inserted in the
	 *                    merged node
	 * @param foreignNode the node to be merged with the current node
	 * @throws DBAppException
	 */
	public void merge(Comparable<T> parentKey, BPTreeInnerNode<T> foreignNode) throws DBAppException {
		this.insertRightAt(numberOfKeys, parentKey, foreignNode.getFirstChild());
		int size = foreignNode.numberOfKeys;
		for (int i = 0; i < size; ++i) {
			this.insertRightAt(numberOfKeys, foreignNode.getKey(i), foreignNode.children[i + 1]);
			BPTreeNode child = deserialize(foreignNode.children[i + 1]);
			if (child instanceof BPTreeLeafNode) {
				((BPTreeLeafNode) child).setParent(this.getSerialVersionUID());
				child.Serialize();
			}
			foreignNode.numberOfKeys--;
		}
		this.Serialize();
		File file = new File("data/B+tree/" + this.btree.IndexName + "_" + this.getSerialVersionUID() + ".ser");
		file.delete();
	}

	/**
	 * delete the key at the specified index with the option to delete the right or
	 * left pointer
	 * 
	 * @param keyIndex the index whose key will be deleted
	 * @param childPtr 0 for deleting the left pointer and 1 for deleting the right
	 *                 pointer
	 */
	public void deleteAt(int keyIndex, int childPtr) // 0 for left and 1 for right
	{
		for (int i = keyIndex; i < numberOfKeys - 1; ++i) {
			keys[i] = keys[i + 1];
			children[i + childPtr] = children[i + childPtr + 1];
		}
		if (childPtr == 0)
			children[numberOfKeys - 1] = children[numberOfKeys];
		numberOfKeys--;
	}

	/**
	 * searches for the record reference of the specified key
	 * 
	 * @throws DBAppException
	 */
	@Override
	public TreeSet<Ref> search(T key) throws DBAppException {
		return getChild(findFirstIndex(key)).search(key);
	}

	public TreeSet<Ref> search(T key, int dif) throws DBAppException {
		return getChild(findFirstIndex(key, dif)).search(key, dif);
	}

	public int findFirstIndex(T key) {
		for (int i = 0; i < numberOfKeys; i++) {
			if (getKey(i).compareTo(key) == 0) {
				return i;
			} else if (getKey(i).compareTo(key) > 0) {
				return i;
			}
		}
		return numberOfKeys;
	}

	public int findFirstIndex(T key, int dif) {
		if (dif == 3 || dif == 4 || dif == 5) {
			return 0;
		}
		for (int i = 0; i < numberOfKeys; i++) {
//			System.out.println("i "+i);
//			System.out.println("Key: "+getKey(i));
//			System.out.println(this.getKey(i).compareTo(key));
			if (getKey(i).compareTo(key) == 0) {
//				System.out.println("Geo 0");
				return i;
			} else if (getKey(i).compareTo(key) > 0) {
				if (i == 0) {
//					System.out.println("Geo 1");
					return 0;
				} else {
//					System.out.println("Geo 2");
					return i;
				}
			}
		}
		return numberOfKeys;
	}
	
//	public TreeSet<Ref> getAllPages(T key) throws DBAppException{
//		
//	}

	public Vector<Integer> getIndexesOfPages(T key) {
		Vector<Integer> indexes = new Vector<Integer>();
		for (int i = 0; i < numberOfKeys; i++) {
			if (getKey(i).compareTo(key) == 0) {
				indexes.add(i);
			}
		}
		return indexes;
	}

	/**
	 * delete the key at the given index and deleting its right child
	 */
	public void deleteAt(int index) {
		deleteAt(index, 1);
	}

}