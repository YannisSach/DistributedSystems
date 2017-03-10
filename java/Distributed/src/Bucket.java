import java.util.*;

public class Bucket extends ArrayList<Song>{
	int HashedKey;
	
	public Bucket(Song song){
		super();
		this.HashedKey = Util.hash(song.Key);
		this.add(song);
	}
	
	@Override
	public int hashCode(){
		return this.HashedKey;
	}
	
	public void addOrUpdate(Song s){
		int i = this.indexOf(s);
		if(i == -1){
			this.add(s);
		}
		else{
			this.get(i).Val = s.Val;
		}
				
	}

}
