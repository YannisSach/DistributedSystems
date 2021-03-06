import java.util.Collection;

public class LazyServer extends ChainServer{


	
	public LazyServer (int idx, int myId,int MyPort,int k){
		super(idx, myId,MyPort,k);
		this.k = k;
	}
	
	
	
	@Override
	public String query(ReplicaSong song){
		
		Bucket bucket = buckets.get(Util.hash(song.Key));
		if (bucket == null)
			return null;
		int i = bucket.indexOf(song);
		String songVal = null;
		if(i == -1 ){//this will work if OR is short-cirquited
		}
		else{
			ReplicaSong s = ((ReplicaSong) bucket.get(i));
			//System.out.println("this,dist "+ s.distance + " k" + k);
			if (s.distance <= k-1)///////////////////////////////////answers for a replica
			{
				songVal = s.Val;
			//	System.out.println("this,dist "+ s.distance + " k" + k + songVal);
			}
		}
		
		return songVal;
	
		//send answer to the requester
			
	}
	
	
	
	@Override
	public void insertRequest(String[] requestLst){
		int hashed = Util.hash(requestLst[KEY]);
		int srcPort = Integer.parseInt(requestLst[SRC]);
		if (isBetween(hashed,this.prevId,this.myId)){
	
			insert(new ReplicaSong(requestLst[KEY],requestLst[VAL],0));//fst replica
			MySocket.send(srcPort, "" + this.myId + ",INSERTED,"+requestLst[KEY]+","+requestLst[VAL]);
			this.printQueryAnswer("inserted" + ","+requestLst[KEY]+","+requestLst[VAL]);
			this.print("inserted" + ","+requestLst[KEY]+","+requestLst[VAL]);
			Util.inserts.decrementAndGet();
			MySocket.send(this.nextPort, "" + srcPort+ ",INSERT_R,"+requestLst[KEY]+","+requestLst[VAL]+","+1);
		}
		else{
			MySocket.send(this.nextPort,requestLst[SRC] + ",INSERT," + requestLst[KEY] + "," + requestLst[VAL]);
		}
	}
	
	@Override
	public void insertReplica(String[] requestLst){;
		int srcPort = Integer.parseInt(requestLst[SRC]);
		int distance = Integer.parseInt(requestLst[INSK]);
		if (distance < k){
			insert(new ReplicaSong(requestLst[KEY],requestLst[VAL],distance));//fst replica
			if (distance == k-1){
			}
			MySocket.send(this.nextPort, "" + srcPort+ ",INSERT_R,"+requestLst[KEY]+","+requestLst[VAL]+","+(distance+1));			
		}
		else if (distance == k){
			this.delete(new ReplicaSong(requestLst[KEY],null,-1));
		}
	
	}
	
	@Override
	public void deleteRequest(String[] requestLst){
		int hashed = Util.hash(requestLst[KEY]);
		int srcPort = Integer.parseInt(requestLst[SRC]);
		if (isBetween(hashed,this.prevId,this.myId)){
			delete(new ReplicaSong(requestLst[KEY], null,-1));
			MySocket.send(srcPort, "" + this.myId + ",DELETED,"+requestLst[KEY]);
			MySocket.send(this.nextPort, "" + srcPort+ ",DELETE_R,"+requestLst[KEY]+","+1);
		}
		else{
			MySocket.send(this.nextPort, requestLst[SRC] + ",DELETE," + requestLst[KEY]);
		}
	}
	
	@Override
	public void deleteReplica(String[] requestLst){
		int srcPort = Integer.parseInt(requestLst[SRC]);
		int distance = Integer.parseInt(requestLst[DELK]);
		if (distance < k){
			delete(new ReplicaSong(requestLst[KEY],null,-1));//fst replica
			if (distance == k-1){//last replica node
				//MySocket.send(srcPort, "" + this.myId + ",DELETED,"+requestLst[KEY]);
			}
			else 
				MySocket.send(this.nextPort, "" + srcPort+ ",DELETE_R,"+requestLst[KEY]+","+(distance+1));			
		}
		
	}

}
