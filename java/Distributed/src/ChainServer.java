
public class ChainServer extends Server{
	public static int k;
	
	
	public ChainServer (int idx, int myId,int MyPort,int k){
		super(idx, myId,MyPort);
		ChainServer.k = k;
	}
	
	
	@Override
	public void insert(Song song){
		
		Bucket bucket = buckets.remove(song.Key);
		if (bucket!=null){
			bucket.addOrUpdate(song);
			buckets.put(bucket.HashedKey, bucket);
		}
		else{
			bucket = new Bucket(song);
			buckets.put(bucket.HashedKey, bucket);
		}		
		
	}
	
	@Override
	public void insertRequest(String[] requestLst){
		int hashed = Util.hash(requestLst[KEY]);
		int srcPort = Integer.parseInt(requestLst[SRC]);
		if (isBetween(hashed,this.prevId,this.myId)){
			insert(new ReplicaSong(requestLst[KEY],requestLst[VAL],0));//1st replica
			MySocket.send(this.nextPort, "" + srcPort+ ",INSERT_R,"+requestLst[KEY]+","+requestLst[VAL]+"");
		}
		else{
			MySocket.send(this.nextPort,requestLst[SRC] + ",INSERT," + requestLst[KEY] + "," + requestLst[VAL]);
		}
	}

}
