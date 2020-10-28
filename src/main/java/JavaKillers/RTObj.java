package JavaKillers;

public class RTObj implements Comparable<RTObj>{
	Polygon P;
	RTObj(Polygon a){
		P=a;	
	}
	
	public int compareTo(RTObj o) {
		if(P.compareTo(o.P)==0){
			int[]a1= new int[4],a2= new int[4];
			a1[0]=P.getBoundingBox().x;
			a1[1]=P.getBoundingBox().y;
			a1[2]=P.getBoundingBox().width;
			a1[3]=P.getBoundingBox().height;
			a2[0]=o.P.getBoundingBox().x;
			a2[1]=o.P.getBoundingBox().y;
			a2[2]=o.P.getBoundingBox().width;
			a2[3]=o.P.getBoundingBox().height;
			int dx = a1[0]-a2[0];
			int dy = a1[1]-a2[1];
			for (int i = 0; i < 1; i++) {
				for (int j = 0; j < 1; j++) {
					int xx=a1[0]+i*a1[2]-a2[0]-i*a2[2];
					int yy=a1[1]+j*a1[3]-a2[1]-j*a2[3];
					if(dx!=xx||dy!=yy){
						return a1[0]==a2[0]?a1[1]-a2[1]:a1[0]-a2[0];
					}
				}
			}
			return 0;
		}
		return P.compareTo(o.P);
	}

}
