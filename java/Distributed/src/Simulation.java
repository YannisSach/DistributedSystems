import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;

public class Simulation {
	
	
	public static Metric Case1 (int ServerType,int k)throws IOException{
		Util.k = k;
		BufferedReader in;
		try{
			in = new BufferedReader(new FileReader("insert.txt"));
		}catch (Exception e){
			System.out.println("Unhandled exception");
			return new Metric(0,0);
		}
		ArrayList<Integer> ports = new ArrayList<Integer>();

		ServerSocket serv=null;
		ports.add(Util.getPort());
		//for (int i=0; i<10; i++)
			//Util.debug("" + i +": " + Util.hash(""+i));
		Server mys = Util.getServer(ServerType, 1,0, ports);
		mys.start();
		//MySocket JoinSocket = new MySocket(port.get(0));
		
		
		for (int i=2; i<=10; i++ ){
			ports.add(Util.getPort());
			Server s = Util.getServer(ServerType, i, i-1,ports);
			s.start();
			MySocket.send(ports.get(0), "INIT,JOIN,"+Util.hash(""+i)+","+ports.get(i-1));
		}
		Util.wait(1);
		
		Util.inserts.set(0);
		long startTime = System.currentTimeMillis();
		
		String line;
		long lines=0;
		while((line = in.readLine()) != null)
		{
			lines++;
			int randPort = ports.get(Util.getRandom(0, ports.size()-1));
		    MySocket.send(randPort, formalizeInserts(line));
		    Util.inserts.incrementAndGet();
			
		}
		
		in.close();
		
		while (true){
			if (Util.inserts.get() == 0){
				break;
			}
		}
		
		
		long endTime = System.currentTimeMillis();
		
		
		for (int i=1; i<=10; i++){
			MySocket.send(ports.get(i-1), "dummy,DIE");
		}
		
		
		return (new Metric(endTime-startTime,lines));
		
		
	}
	
	public static void debug(int ServerType)throws IOException{
		
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
		Server mys = Util.getServer(ServerType, 1,0, ports);
		mys.start();
		//MySocket JoinSocket = new MySocket(port.get(0));
		
		
		for (int i=2; i<7; i++ ){
			ports.add(Util.getPort());
			Server s = Util.getServer(ServerType, i, i-1,ports);
			s.start();
			MySocket.send(ports.get(0), "INIT,JOIN,"+Util.hash(""+i)+","+ports.get(i-1));
			
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