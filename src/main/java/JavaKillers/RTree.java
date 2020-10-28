package JavaKillers;

import java.util.TreeSet;

public class RTree  extends BPTree<RTObj>{

	public RTree(int order, String Name) throws DBAppException {
		super(order, Name);
	}
	
	public void insert(Polygon key, Ref recordReference) throws DBAppException {
		RTObj o = new RTObj(key);
		super.insert(o, recordReference);
	}
	
	public boolean delete(Polygon key, Ref ref) throws DBAppException {
		RTObj o = new RTObj(key);
		return super.delete(o, ref);
	}
	
	public TreeSet<Ref> search(Polygon key) throws DBAppException {
		RTObj o = new RTObj(key);
		return super.search(o);
	}

}
