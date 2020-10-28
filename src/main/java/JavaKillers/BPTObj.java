package JavaKillers;

import java.sql.Date;

public class BPTObj <T extends Comparable<T>> implements Comparable<BPTObj<T>> {
	T O;
	Date time;
	public BPTObj(T O ,Date d) {
		this.O=O;
		time = d;
	}
	public int compareTo(BPTObj o) {
		if (O.compareTo((T) o.O)==0) {
			if (o.time==null) {
				return 0;
			}
			return time.compareTo(o.time);
		}
		return O.compareTo((T) o.O);
	}
	
}
