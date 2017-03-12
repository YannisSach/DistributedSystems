import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class DHT {

	
	public static void main (String[] args) throws IOException{
		
		BufferedReader in;
		try{
			in = new BufferedReader(new FileReader("insert.txt"));
		}catch (Exception e){
			System.out.println("Unhandled exception");
			return;
		}
		
		ArrayList<Integer> ports = new ArrayList<Integer>();
		ServerSocket serv=null;
		ports.add(Util.getPort());
		//for (int i=0; i<10; i++)
			//Util.debug("" + i +": " + Util.hash(""+i));
		Server mys = new LazyServer(1,Util.hash("1"),ports.get(0));
		mys.start();
		//MySocket JoinSocket = new MySocket(port.get(0));
		
		
		for (int i=2; i<7; i++ ){
			MySocket JoinSocket = new MySocket(ports.get(0));
			ports.add(Util.getPort());
			Server s = new LazyServer(i,Util.hash(""+i),ports.get(i-1));
			s.start();
			JoinSocket.write("INIT,JOIN,"+Util.hash(""+i)+","+ports.get(i-1));
			JoinSocket.close();
			
		}
		
		Util.wait(4);
				
		
		String line;
		int ln = 10;
		int i=0;
		while((line = in.readLine()) != null && i<ln)
		{
			i++;
			int firstPort = ports.get(Util.getRandom(0, ports.size()-1));
		    MySocket.send(firstPort, formalizeInserts(line));
			
		}
		
		in.close();
		

		Util.wait(2);
		
		MySocket.send(ports.get(Util.getRandom(0, ports.size()-1)), "INIT,QUERY,*");
		
		Util.wait(2);
		
		MySocket.send(ports.get(0), "INIT,DEPART,"+Util.hash(""+5)+","+ports.get(5-1));
		
		Util.wait(2);
		
		MySocket.send(ports.get(Util.getRandom(0, ports.size()-1)), "INIT,QUERY,*");
	} 
	
	 public static String formalizeInserts(String rawRequest){
			return "INIT,INSERT," + rawRequest.replace('\n', ' ');
		}
	 
	 public static String formalizeQueries(String rawRequest){
			return "INIT,QUERY," + rawRequest.replace('\n', ' ');
		}
	 
	public static String formalizeRequests(String rawRequest){
		rawRequest.replace('\n', ' ');
		String[] rR = rawRequest.split(", ");
		rR[0].toUpperCase();
		return "INIT," + String.join(",", rR);
	}

}
