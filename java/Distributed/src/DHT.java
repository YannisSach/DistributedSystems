import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class DHT {

	
	public static void main (String[] args) throws IOException{
		//Simulation.debug(Util.SIMPLE+2,3);
		
		Simulation.Test(1, 3);
		/*
		
		for (int ServerType = Util.SIMPLE; ServerType <= Util.LAZY; ServerType++){
			
			
			
			Metric k3 = Simulation.Case2(ServerType, 3);
			System.out.println("ServerType: " + ServerType + " k: 3");
			System.out.println("Time: " + k3.time);
			System.out.println("Lines: " + k3.lines);
			System.out.println("Throughput(lines/s): " + (k3.lines/k3.time));
			//System.out.println("Chain K:" + 3);
			//System.out.println("Lazy K:" + 3);
			Util.wait(1);
			
			Metric k5 = Simulation.Case2(ServerType, 5);
			System.out.println("ServerType: " + ServerType + " k: " + 8);
			System.out.println("Time: " + k5.time);
			System.out.println("Lines: " + k5.lines);
			System.out.println("Throughput(lines/s): " + (k5.lines/k5.time));
			//System.out.println("Chain K:" + 5);
			//System.out.println("Lazy K:" + 5);
		
	
	
			
			
		}
		/*
		
		float  sum = 0;
		for (int i = 0; i < 10 ; i++){
			System.out.println(i);
			Metric m = Simulation.Case2(2,3);
			sum += 1.0*(m.lines/m.time);
		}
		float avg = sum/10;
		System.out.println("Case 2 Simple Server Throughput(lines/sec):" + avg);
	
		*/
	}

}
