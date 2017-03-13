import java.util.Collection;

public class LazyServer extends ChainServer{
	
	public LazyServer (int idx, int myId,int MyPort){
		super(idx, myId,MyPort);
		LazyServer.k = Util.k;
	}
	
	
	
	@Override
	public String query(ReplicaSong song){
		
		Bucket bucket = buckets.get(Util.hash(song.Key));
		if (bucket == null)
			return null;
		int i = bucket.indexOf(song);
		String songVal = null;
		int dist = song.distance;
		if(i == -1 ){//this will work if OR is short-cirquited
		}
		else{
			ReplicaSong s = ((ReplicaSong) bucket.get(i));
			if (s.distance <= LazyServer.k-1)///////////////////////////////////answers for a replica
				songVal = s.Val;
		}
		
		return songVal;
	
		//send answer to the requester
			
	}

}
