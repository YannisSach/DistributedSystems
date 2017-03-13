import java.net.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.io.*;


public class Server extends Thread{
	public int idx;
	public int myId;
	public int nextId;
	public int prevId;
	public int prevPort, nextPort, myPort;
	public static int Nodes = 0;
	public ServerSocket ss;
	public static int SRC=0,CMD=1,KEY=2,VAL=3,CNT=3,ID=2,PORT=3,CON=4;
	public HashMap<Integer, Bucket> buckets;
	boolean departed;
	
	public Server(int idx, int myId,int MyPort){
		this.idx = idx;
		this.myId = myId;
		if (this.idx == 1){
			this.nextId = myId;
			this.prevId = myId;
		}
		this.prevPort = this.nextPort = 0;
	
		this.myPort = MyPort;
		this.departed = false;
		buckets = new HashMap<Integer,Bucket>();
		Server.Nodes++;
		this.ss=null;
		try {
			ss = new ServerSocket(myPort);
		}
		catch (IOException e) {
	           System.out.println(e);
	           
	    }
		
		
		
	}
	
	public void run() {
		Socket serviceSocket = null;
		
		while (true) {
			
			if (departed) {
				print("Goodbye");
				return;
			}
			
			//print("next: "+ nextId + ", prev: " + prevId);
			//wait for requests
		    try {
		       serviceSocket = ss.accept();
		    }
		    catch (IOException e) {
		       System.out.println(e);
		    }
		    
		    MySocket sock = new MySocket(serviceSocket);
		    String request = sock.read();
		    
		    debug(request);
		    String[] requestLst = request.split(",");
		    //got request
		    boolean isFirst = false;
		  
		    
		    if (requestLst[SRC].equals("INIT")){
		    	isFirst = true;
		    	requestLst[SRC] = "" + this.myPort;	
		    }
		    if (requestLst[CMD].equals("JOIN")){
		    	JoinRequest(requestLst,isFirst);
		    }
		    else if (requestLst[CMD].equals("CONNECT")){
		    	Connect(requestLst);
		    }
		    else if (requestLst[CMD].equals("DEPART")) {
		    	DepartRequest(requestLst); 
		    }
		    else if (requestLst[CMD].equals("INSERT")){
		    	this.insertRequest(requestLst);
		    	
		    }
		    else if (requestLst[CMD].equals("QUERY")){
		    	if (requestLst[KEY].equals("*")){
		    		this.queryStarRequest(requestLst);
		    	}
		    	else{
		    		this.queryRequest(requestLst);
		    	}		    	
		    }
		    else if (requestLst[CMD].equals("DELETE")){
		    	deleteRequest(requestLst);
		    }
		    else if (requestLst[CMD].equals("INSERTED")){
		    	this.printQueryAnswer(requestLst[CMD].toLowerCase() + ","+requestLst[KEY]+","+requestLst[VAL]);
		    	//Util.inserts.decrementAndGet();
		    }
		    else if (requestLst[CMD].equals("DIE")){
		    	return;
		    }
		    else if (requestLst[CMD].equals("FOUND")){
		    	this.printQueryAnswer(requestLst[CMD].toLowerCase() + ","+requestLst[KEY]+","+requestLst[VAL]);
		    }
		    else if (requestLst[CMD].equals("NOT_FOUND")){
		    	this.printQueryAnswer(requestLst[CMD].toLowerCase() + ","+requestLst[KEY]);
		    	
		    }
			
		}
	    
	    
	}
	
	public void DepartRequest (String[] requestLst) {
		
		int id = Integer.parseInt(requestLst[ID]);
		
		if (myId == id) {
			MySocket.send(this.nextPort, ""+this.myPort+",CONNECT," + this.prevId + "," + this.prevPort + ",PREV");
			MySocket.send(this.prevPort, ""+this.myPort+",CONNECT," + this.nextId + "," + this.nextPort + ",NEXT");
			this.TransferAll();
			//print("TranferAll == Depart");
			departed = true;
		}
		else {
			MySocket.send(this.nextPort, String.join(",", requestLst));
		}
		
	}
	
	public void JoinRequest(String[] requestLst,boolean isFirst){
		
		int port = Integer.parseInt(requestLst[PORT]);
		int id = Integer.parseInt(requestLst[ID]);	
		if (isAlone()) {
			debug("req arrived (alone): " + Integer.parseInt(requestLst[PORT]) + " " + Integer.parseInt(requestLst[ID]));
			this.nextPort =port;
			this.nextId = id;
			MySocket.send(this.nextPort, ""+this.myPort+",CONNECT," + this.myId + "," + this.myPort+ ",NEXT");
			MySocket.send(port, ""+this.myPort+",CONNECT," + this.myId + "," + this.myPort+ ",PREV");	
			return;
		}
		if (isBetween(id, myId, nextId)) {
			debug("req arrived (between): " + Integer.parseInt(requestLst[PORT]) + " " + Integer.parseInt(requestLst[ID]));	
			//send to the old prev -->not needed any more hopefully...
			//MySocket.send(this.nextPort, ""+this.myPort+ ",CONNECT," + id + "," + port+",PREV");	
			//send to new node
			MySocket.send(port, ""+this.myPort+",CONNECT," + this.myId + "," + this.myPort+ ",PREV");
			MySocket.send(port, ""+this.myPort+",CONNECT," + this.nextId + "," + this.nextPort+ ",NEXT");
			this.nextPort = port;
			this.nextId = id;
			return;
		}
		
		debug("req arrived: " + Integer.parseInt(requestLst[PORT]) + " " + Integer.parseInt(requestLst[ID]));
		MySocket cl = new MySocket(this.nextPort);
		cl.write(requestLst[SRC] + ",JOIN," + id + "," + port);
		cl.close();
		
	}
	
	public void Connect (String[] requestLst){
		if (requestLst[CON].equals("NEXT")){
			if (nextPort == 0){ //i am a new node that has to connect to the chord
				this.nextPort = Integer.parseInt(requestLst[PORT]);
				this.nextId = Integer.parseInt(requestLst[ID]);
				MySocket.send(this.nextPort, ""+this.myPort+ ",CONNECT," + this.myId + "," + this.myPort+",PREV");
				
			}
			else{ //my next node departs
				this.nextPort = Integer.parseInt(requestLst[PORT]);
				this.nextId = Integer.parseInt(requestLst[ID]);
				
			}
		}
		else if (requestLst[CON].equals("PREV")){
			int SrcPort = Integer.parseInt(requestLst[SRC]);
			//print("SRCPORT: " + SrcPort + " prevport: " + this.prevPort);
			if (SrcPort == this.prevPort){		//previous node departs
				this.prevPort = Integer.parseInt(requestLst[PORT]);
				this.prevId = Integer.parseInt(requestLst[ID]);
			}
			else{ //new node is previous
				//print(""+this.prevId);
				this.prevPort = Integer.parseInt(requestLst[PORT]);
				this.prevId = Integer.parseInt(requestLst[ID]);
				//print(""+this.prevId);
				this.TransferSome();
			}
		}
	}
	
	
	public void TransferSome(){
		//print("TransferSome");
		Collection<Bucket> bc = buckets.values();
		int port = this.prevPort;
		Iterator<Bucket> iter = (bc).iterator();
		while(iter.hasNext()){
			Bucket b = iter.next();
			if (!isBetween(b.HashedKey,this.prevId,this.myId)){
				for(Song s : b){
					MySocket.send(port, ""+this.myPort+",INSERT,"+""+s.Key+","+s.Val);
				}
				//this.buckets.remove(b.HashedKey); not needed since it will exit
				buckets.remove(b.HashedKey);
			}

		}
	}
	
	public void TransferAll(){
		Collection<Bucket> bc = buckets.values();
		int port = this.nextPort;
		for(Bucket b : bc){
				for(Song s : b){
					MySocket.send(port, ""+this.myPort+",INSERT,"+""+s.Key+","+s.Val);
				}
				//this.buckets.remove(b.HashedKey); not needed since it will exit

		}
	}
	
	public void debug(String msg){
		
		if (Util.debug)
			System.out.println("" + idx + "(" + myId + "): " + msg);
	}
	
	
	public void print(String msg){
		
		if (Util.prints)
			System.out.println("" + idx + "(" + myId + "): " + msg);
	}
	
	public void printQueryAnswer (String msg){
		
		if (Util.printQueryAnswer)
			System.out.println(msg);
	}
	
	public static boolean isBetween (int key, int low, int high) {
	    if (low <= high) {
	        return low < key && key <= high;
	    }
	    else
	        return low < key || key <= high;
	}
	
	public boolean isAlone(){
		return this.prevId == this.nextId && this.myId==this.prevId;
	}
	
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
	
	public void insertRequest(String[] requestLst){
		int hashed = Util.hash(requestLst[KEY]);
		int srcPort = Integer.parseInt(requestLst[SRC]);
		if (isBetween(hashed,this.prevId,this.myId)){
			insert(new Song(requestLst[KEY],requestLst[VAL]));
			MySocket.send(srcPort, "" + this.myId + ",INSERTED,"+requestLst[KEY]+","+requestLst[VAL]);
			Util.inserts.decrementAndGet();
		}
		else{
			MySocket.send(this.nextPort,requestLst[SRC] + ",INSERT," + requestLst[KEY] + "," + requestLst[VAL]);
		}
	}
		
	public void queryRequest(String[] requestLst){
		int hashed = Util.hash(requestLst[KEY]);
		if (isBetween(hashed,this.prevId,this.myId)){
			int port = Integer.parseInt(requestLst[SRC]);
			String val = query(new Song(requestLst[KEY], null));
			//Send response to requester
			if(val == null){
				MySocket.send(port,""+this.myId + ",NOT_FOUND," + requestLst[KEY]);
				Util.inserts.decrementAndGet();
			}
			else{ 
				MySocket.send(port, ""+this.myId + ",FOUND," + requestLst[KEY] + "," + val);
				Util.inserts.decrementAndGet();
			}
			
		}
		
		else{
			MySocket.send(this.nextPort,requestLst[SRC] + ",QUERY," + requestLst[KEY]);
		}
	}
	
	public void queryStarRequest(String[] requestLst){
		Collection<Bucket> bc = buckets.values();
		int port = Integer.parseInt(requestLst[SRC]);
		for(Bucket b : bc){
			for(Song s : b){
				//send to next node
				//print(requestLst[SRC] + ",FOUND," + s.Key + "," + s.Val);
				MySocket.send(port, "" + this.myId + ",FOUND," + s.Key + "," + s.Val);
			}
		}
		if (port != this.nextPort){
			MySocket.send(this.nextPort, requestLst[SRC] + ",QUERY,*");
		}
		
	}
	
	public String query(Song song){
		
		Bucket bucket = buckets.get(Util.hash(song.Key));
		if (bucket == null)
			return null;
		
		int i = bucket.indexOf(song);
		String songVal = null;
		if(i == -1){
		}
		else{
			
			songVal = bucket.get(i).Val;
		}
		
		return songVal;
	
		//send answer to the requester
		
		
	}
	
	public void deleteRequest(String[] requestLst){
		int hashed = Util.hash(requestLst[KEY]);
		if (isBetween(hashed,this.prevId,this.myId)){
			delete(new Song(requestLst[KEY], null));
		}
		else{
			MySocket.send(this.nextPort, requestLst[SRC] + ",DELETE," + requestLst[KEY]);;
		}
	}
	
	public void delete(Song song){
		Bucket bucket = buckets.get(Util.hash(song.Key));
		if (bucket == null)
			return;
		int i = bucket.indexOf(song);
		if(i == -1){
			return;
		}
		else{
			bucket.remove(i);
		}
	}
	

	

}
