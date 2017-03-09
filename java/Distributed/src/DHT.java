import java.net.*;
import java.io.*;
import java.util.*;

public class DHT {

	
	public static void main (String[] args){
		ArrayList<Integer> port = new ArrayList<Integer>();
		ServerSocket serv=null;
		port.add(Util.getPort());
		//for (int i=0; i<10; i++)
			//Util.debug("" + i +": " + Util.hash(""+i));
		Server mys = new Server(1,Util.hash("1"),port.get(0));
		mys.start();
		MySocket JoinSocket = new MySocket(port.get(0));
		
		
		for (int i=2; i<3; i++ ){
			port.add(Util.getPort());
			Server s = new Server(i,Util.hash(""+i),port.get(i-1));
			s.start();
			JoinSocket.write("INIT,JOIN,"+Util.hash(""+i)+","+port.get(i-1));
		}
		
		
	   
	    
	    
	    

		
	}

}
