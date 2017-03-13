import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class DHT {

	
	public static void main (String[] args) throws IOException{
		//Simulation.debug(Util.SIMPLE);

		for (int ServerType = Util.SIMPLE; ServerType <= Util.LAZY; ServerType++){
			/*Metric k1 = Simulation.Case1(ServerType, 1);
			System.out.println("ServerType: " + ServerType + " k: " + 1);
			System.out.println("Time: " + k1.time);
			System.out.println("Lines: " + k1.lines);
			System.out.println("Throughput(lines/s): " + (k1.lines/k1.time));
			System.out.println("K:" + ChainServer.k);
			Util.wait(1);*/
			Metric k3 = Simulation.Case2(ServerType, 3);
			System.out.println("ServerType: " + ServerType + " k: " + 3);
			System.out.println("Time: " + k3.time);
			System.out.println("Lines: " + k3.lines);
			System.out.println("Throughput(lines/s): " + (k3.lines/k3.time));
			System.out.println("K:" + ChainServer.k);
			Util.wait(1);
			Metric k5 = Simulation.Case2(ServerType, 5);
			System.out.println("ServerType: " + ServerType + " k: " + 5);
			System.out.println("Time: " + k5.time);
			System.out.println("Lines: " + k5.lines);
			System.out.println("Throughput(lines/s): " + (k5.lines/k5.time));
			System.out.println("K:" + ChainServer.k);
			//break;
			
			
		}
		
		
	}

}
