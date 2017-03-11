import java.io.IOException;
import java.net.Socket;

public class ChainServer extends Server{
	public static int k,INSK=4;
	
	
	public ChainServer (int idx, int myId,int MyPort,int k){
		super(idx, myId,MyPort);
		ChainServer.k = k;
	}
	
	@Override
	public void run() {
		System.out.println("Hello from a thread: " + myId);
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
		    else if (requestLst[CMD].equals("INSERT_R")){
		    	this.insertReplica(requestLst);
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
		    	this.print(request + " Hash of song:" + Util.hash(requestLst[KEY]));
		    }
		    else if (requestLst[CMD].equals("FOUND")){
		    	this.print(request);
		    }
			
		}
	    
	    
	}
	
	

	@Override
	public void insertRequest(String[] requestLst){
		int hashed = Util.hash(requestLst[KEY]);
		int srcPort = Integer.parseInt(requestLst[SRC]);
		if (isBetween(hashed,this.prevId,this.myId)){
			insert(new ReplicaSong(requestLst[KEY],requestLst[VAL],0));//fst replica
			MySocket.send(this.nextPort, "" + srcPort+ ",INSERT_R,"+requestLst[KEY]+","+requestLst[VAL]+","+1);
		}
		else{
			MySocket.send(this.nextPort,requestLst[SRC] + ",INSERT," + requestLst[KEY] + "," + requestLst[VAL]);
		}
	}
	
	public void insertReplica(String[] requestLst){;
		int srcPort = Integer.parseInt(requestLst[SRC]);
		int distance = Integer.parseInt(requestLst[INSK]);
		if (distance < k){
			insert(new ReplicaSong(requestLst[KEY],requestLst[VAL],distance));//fst replica
			if (distance == k-1)
				MySocket.send(srcPort, "" + this.myId + ",INSERTED,"+requestLst[KEY]+","+requestLst[VAL]);
			MySocket.send(this.nextPort, "" + srcPort+ ",INSERT_R,"+requestLst[KEY]+","+requestLst[VAL]+","+(distance+1));			
		}
		else if (distance == k){
			this.delete(new ReplicaSong(requestLst[KEY],null,-1));
		}
	
	}

}
