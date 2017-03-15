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

}
