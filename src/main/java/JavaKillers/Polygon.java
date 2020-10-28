package JavaKillers;
import java.awt.Dimension;
import java.util.StringTokenizer;

public class Polygon extends java.awt.Polygon implements Comparable<Polygon>{

	public Polygon(int[] x ,int [] y , int size){
		super(x,y,size);
	}
	
	public long area() {
		Dimension d = getBounds().getSize();
		return 1l*d.width*d.height;
	}
	public int compareTo(Polygon arg0) {
		return Long.compare(area(), arg0.area());
	}
	
}
