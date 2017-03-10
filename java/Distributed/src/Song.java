
public class Song {
	public String Key;
	public String Val;
	
	public Song(String Key,String Val){
		this.Key = Key;
		this.Val = Val;
	}

	@Override 
	public int hashCode(){
		return Util.hash(Key);
	}
}
