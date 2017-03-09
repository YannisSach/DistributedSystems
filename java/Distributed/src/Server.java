import java.net.*;
import java.util.HashMap;
import java.io.*;


public class Server extends Thread{
	public int idx;
	public int myId;
	public int nextId;
	public int prevId;
	public int prevPort, nextPort, myPort;
	public static int Nodes = 0;
	public ServerSocket ss;
	//public AssocArray
	public static int SRC=0,CMD=1,KEY=2,VAL=3,CNT=3,ID=2,PORT=3,CON=0;
	
	public Server(int idx, int myId,int MyPort){
		this.idx = idx;
		this.myId = myId;
		if (this.idx == 1){
			this.nextId = myId;
			this.prevId = myId+1;
			this.prevPort = this.nextPort = 0;
		}
		this.myPort = MyPort;
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
		System.out.println("Hello from a thread: " + myId);
		Socket serviceSocket = null;
		
		while (true) {
			
			if (nextPort == -1) {
				print("Goodbye");
				return;
			}
			
			print("next: "+ nextId + ", prev: " + prevId);
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
			
			
			
		}
	    
	    
	}
	
	public void DepartRequest (String[] requestLst) {
		
		int id = Integer.parseInt(requestLst[ID]);
		
		if (myId == id) {
			MySocket cl = new MySocket(this.nextPort);
			if (prevId == nextId)
				prevId++;
			cl.write("PREV"+",CONNECT," + this.prevId + "," + this.prevPort);
			cl.close();
			cl = new MySocket(this.prevPort);
			cl.write("NEXT"+",CONNECT," + this.nextId + "," + this.nextPort);
			cl.close();
			nextPort = -1;
		}
		else {
			MySocket cl = new MySocket(this.nextPort);
			cl.write(String.join(",", requestLst));
			cl.close();
		}
		
	}
	
	public void JoinRequest(String[] requestLst,boolean isFirst){
		
		int port = Integer.parseInt(requestLst[PORT]);
		int id = Integer.parseInt(requestLst[ID]);
		
		if (isAlone()) {
			debug("req arrived (alone): " + Integer.parseInt(requestLst[PORT]) + " " + Integer.parseInt(requestLst[ID]));
			this.nextPort =this.prevPort = port;
			this.nextId =this.prevId = id;
			MySocket cl = new MySocket(this.nextPort);
			cl.write("NEXT"+",CONNECT," + this.myId + "," + this.myPort);
			cl.close();
			MySocket cl2 = new MySocket(this.prevPort);
			cl2.write("PREV"+",CONNECT," + this.myId + "," + this.myPort);
			cl2.close();
			return;
		}
		
		if (isBetween(id, myId, nextId)) {
			debug("req arrived (between): " + Integer.parseInt(requestLst[PORT]) + " " + Integer.parseInt(requestLst[ID]));
			MySocket cl = new MySocket(this.nextPort);
			//send to next node
			cl.write("PREV"+",CONNECT," + id + "," + port);
			cl.close();
			
			//send to new node
			cl = new MySocket(port);
			cl.write("PREV"+",CONNECT," + this.myId + "," + this.myPort);
			cl.close();
			cl = new MySocket(port);
			cl.write("NEXT"+",CONNECT," + this.nextId + "," + this.nextPort);
			cl.close();
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
			this.nextPort = Integer.parseInt(requestLst[PORT]);
			this.nextId = Integer.parseInt(requestLst[ID]);
		}
		else if (requestLst[CON].equals("PREV")){
			this.prevPort = Integer.parseInt(requestLst[PORT]);
			this.prevId = Integer.parseInt(requestLst[ID]);
			
		}
	}
	
	
	public void debug(String msg){
		
		if (Util.debug)
			System.out.println("" + idx + "(" + myId + "): " + msg);
	}
	
	
	public void print(String msg){
		
		if (true)
			System.out.println("" + idx + "(" + myId + "): " + msg);
	}
	
	public static boolean isBetween (int key, int low, int high) {
	    if (low <= high) {
	        return low <= key && key <= high;
	    }
	    else
	        return low <= key || key <= high;
	}
	
	public boolean isAlone(){
		return this.prevId - this.nextId == 1;
	}

}
