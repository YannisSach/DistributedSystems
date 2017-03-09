import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class DHT {

	
	public static void main (String[] args){
		ArrayList<Integer> port = new ArrayList<Integer>();
		ArrayList<Server> servers = new ArrayList<Server>();
		ServerSocket serv=null;
		port.add(Util.getPort());
		//for (int i=0; i<10; i++)
			//Util.debug("" + i +": " + Util.hash(""+i));
		Server mys = new Server(1,Util.hash("1"),port.get(0));
		mys.start();
		//MySocket JoinSocket = new MySocket(port.get(0));
		
		
		for (int i=2; i<7; i++ ){
			MySocket JoinSocket = new MySocket(port.get(0));
			port.add(Util.getPort());
			Server s = new Server(i,Util.hash(""+i),port.get(i-1));
			s.start();
			servers.add(s);
			JoinSocket.write("INIT,JOIN,"+Util.hash(""+i)+","+port.get(i-1));
			JoinSocket.close();
		}
		
		try {
			TimeUnit.SECONDS.sleep(10);
		}
		catch (Exception e) {
	           System.out.println(e);
	    }
		
		System.out.println("Going to sleep");
		MySocket JoinSocket = new MySocket(port.get(0));
		JoinSocket.write("INIT,DEPART,"+Util.hash(""+5)+","+port.get(5-1));
		JoinSocket.close();
		
	}

}
