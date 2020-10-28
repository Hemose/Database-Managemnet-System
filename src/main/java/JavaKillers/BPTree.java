package JavaKillers;

import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.TreeSet;
import java.util.Vector;

public class BPTree<T extends Comparable<T>> implements Serializable {

	/**
	 * 
	 */
	public long serialVersionUID = 1L;
	private int order;
	public BPTreeNode<T> root;
	public String IndexName;

	/**
	 * Creates an empty B+ tree
	 * 
	 * @param order the maximum number of keys in the nodes of the tree
	 * @throws DBAppException
	 */
	public BPTree(int order, String Name) throws DBAppException {
		this.order = order;
		root = new BPTreeLeafNode<T>(this.order, this,-1);
		root.setRoot(true);
		IndexName = Name;
	}

	/**
	 * Inserts the specified key associated with the given record in the B+ tree
	 * 
	 * @param key             the key to be inserted
	 * @param recordReference the reference of the record associated with the key
	 * @throws DBAppException
	 */
	public void insert(T key, Ref recordReference) throws DBAppException {
		PushUp<T> pushUp = root.insert(key, recordReference, null, -1);
		if (pushUp != null) {
			BPTreeInnerNode<T> newRoot = new BPTreeInnerNode<T>(order, this);
			newRoot.insertLeftAt(0, pushUp.key, root.getSerialVersionUID());
			newRoot.setChild(1, pushUp.newNode.getSerialVersionUID());
			BPTreeNode child = root;
			if (child instanceof BPTreeLeafNode) {
				System.out.println(((BPTreeLeafNode) child).getParent());
				((BPTreeLeafNode) child).setParent(newRoot.getSerialVersionUID());
				System.out.println(((BPTreeLeafNode) child).getParent());
			}
			child = pushUp.newNode;
			if (child instanceof BPTreeLeafNode) {
				((BPTreeLeafNode) child).setParent(newRoot.getSerialVersionUID());
			}
			root.setRoot(false);
			root.Serialize();
			root = newRoot;
			root.setRoot(true);
			pushUp.newNode.Serialize();
		}

		root.Serialize();
		DBApp.Serialize("B+tree/" + this.IndexName , this);
	}

	/**
	 * Looks up for the record that is associated with the specified key
	 * 
	 * @param key the key to find its record
	 * @return the reference of the record associated with this key
	 * @throws DBAppException
	 */
	public TreeSet<Ref> search(T key) throws DBAppException {
		return root.search(key);
	}
	public TreeSet<Ref> search(T key,int dif) throws DBAppException {
		return root.search(key,dif);
	}

	
//	public TreeSet<Ref> getAllPages(T key) throws DBAppException{
//		return root.getAllPages(key);
//	}

	/**
	 * Delete a key and its associated record from the tree.
	 * 
	 * @param key the key to be deleted
	 * @return a boolean to indicate whether the key is successfully deleted or it
	 *         was not in the tree
	 * @throws DBAppException
	 */
	public boolean delete(T key, Ref ref) throws DBAppException {
		boolean done = root.delete(key, null, -1, ref);
		// go down and find the new root in case the old root is deleted
		while (root instanceof BPTreeInnerNode && !root.isRoot())
			root = ((BPTreeInnerNode<T>) root).getChild(0);
		DBApp.Serialize("data/B+tree/" + IndexName + ".ser", this);
		root.Serialize();
		return done;
	}

	/**
	 * Returns a string representation of the B+ tree.
	 */

	public String toString() {

		// <For Testing>
		// node : (id)[k1|k2|k3|k4]{P1,P2,P3,}
		String s = "";
		Queue<BPTreeNode<T>> cur = new LinkedList<BPTreeNode<T>>(), next;
		cur.add(root);
		while (!cur.isEmpty()) {
			next = new LinkedList<BPTreeNode<T>>();
			while (!cur.isEmpty()) {
				BPTreeNode<T> curNode = cur.remove();
				System.out.print(curNode);
				if (curNode instanceof BPTreeLeafNode)
					System.out.print(((BPTreeLeafNode)curNode).getParent()+ "->");
				else {
					System.out.print("{");
					BPTreeInnerNode<T> parent = (BPTreeInnerNode<T>) curNode;
					for (int i = 0; i <= parent.numberOfKeys; ++i) {
						try {
							System.out.print(parent.getChild(i).getSerialVersionUID() + ",");
							next.add(parent.getChild(i));
						} catch (DBAppException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					System.out.print("} ");
				}

			}
			System.out.println();
			cur = next;
		}
		// </For Testing>
		return s;
	}
}