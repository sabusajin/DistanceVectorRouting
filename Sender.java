import java.io.*;
import java.net.*;
import java.util.*;
import java.util.Hashtable;
import java.util.Vector;

public class Sender extends Thread{
	
	private String filename;
	private DatagramSocket SenderSocket = null;	
	private int sequence = 0;
	
	
	Hashtable<String, Double> hclist = null;
	ArrayList<String> port = new ArrayList<String>();
	Hashtable<String, Vector<DistVect>> hosts = null;
	ArrayList<String> neighbors = null;
	
	public Sender(String filename, Hashtable<String, Double> hclist, ArrayList<String> neighbors, Hashtable<String, Vector<DistVect>> hosts) throws IOException {
		super("Sender");
		this.filename = filename;	
		SenderSocket = new DatagramSocket();		
		this.hclist = hclist;
		this.neighbors = neighbors;
		this.hosts = hosts;
	}
	
	public void run() { 
		// Infinite loop
		while(true) {						
			sequence++;
			System.out.println("\n Output number " + sequence + "\n");			

			// Send routing information			
			try {							   
				DatagramPacket clientPacket = null;
				byte[] buf = new byte[32768];
				String info = "";
				//Only for the first interval
				if(sequence == 1) {
					Hostinfo(filename);
					info = RoutingTable(hosts);					
					for(int n=0; n<neighbors.size(); n++) {
						buf = info.getBytes();
						InetAddress address = InetAddress.getByName(neighbors.get(n));
						String hostport = port.get(n);
						int hport = Integer.parseInt(hostport);
						//Construct packet and send
						clientPacket = new DatagramPacket(buf, buf.length, address, hport);						
						SenderSocket.send(clientPacket);
					}
					String printBuffer = "";		
					String[] lines = info.split("\\n");
					for(int i=0; i<lines.length; i++) {
						String[] array = lines[i].split(" ");
						String[] start = array[2].split("\\.");
						String[] dest = array[4].split("\\.");
						String[] via = array[9].split("\\.");
						printBuffer += "shortest path " + start[0] + " - " + dest[0] + ": the next hop is " + via[0] + " and the cost is " + array[14] + "\n";			

					}
					//String simpleString = printDetails(info);
					System.out.println(printBuffer);
				}

				//For later intervals
				else if(sequence > 1) {
					boolean isChanged = false;
					LinkedList<String> newPair = filechange(filename);
					for(int i=0; i<newPair.size(); i++) {
						String[] pairCost = newPair.get(i).split(" ");
						if(hclist.get(pairCost[0]) != Double.parseDouble(pairCost[1])) {
							isChanged = true;
							hclist.remove(pairCost[0]);
							hclist.put(pairCost[0], Double.parseDouble(pairCost[1]));							
						}
					}

					// If the data is send, then keep reading and send the data again
					if(isChanged) {
						neighbors = new ArrayList<String>();
						Hostinfo(filename);
						info = RoutingTable(hosts);						
						for(int n=0; n<neighbors.size(); n++) {
							buf = info.getBytes();
							InetAddress address = InetAddress.getByName(neighbors.get(n));
							String hostport = port.get(n);
							int hport = Integer.parseInt(hostport);							
							clientPacket = new DatagramPacket(buf, buf.length, address, hport);
							SenderSocket.send(clientPacket);
						}
						//String simpleString = printDetails(info);
						String printBuffer = "";		
						String[] lines = info.split("\\n");
						for(int i=0; i<lines.length; i++) {
							String[] array = lines[i].split(" ");
							String[] start = array[2].split("\\.");
							String[] dest = array[4].split("\\.");
							String[] via = array[9].split("\\.");
							printBuffer += "shortest path " + start[0] + " - " + dest[0] + ": the next hop is " + via[0] + " and the cost is " + array[14] + "\n";			

						}
						System.out.println(printBuffer);
						isChanged = false;
					}
					else {				
						info = RoutingTable(hosts);						
						for(int n=0; n<neighbors.size(); n++) {
							buf = info.getBytes();
							InetAddress address = InetAddress.getByName(neighbors.get(n));
							String hostport = port.get(n);
							int hport = Integer.parseInt(hostport);							
							clientPacket = new DatagramPacket(buf, buf.length, address, hport);
							SenderSocket.send(clientPacket);
						}						
						String printBuffer = "";		
						String[] lines = info.split("\\n");
						for(int i=0; i<lines.length; i++) {
							String[] array = lines[i].split(" ");
							String[] start = array[2].split("\\.");
							String[] dest = array[4].split("\\.");
							String[] via = array[9].split("\\.");
							printBuffer += "shortest path " + start[0] + " - " + dest[0] + ": the next hop is " + via[0] + " and the cost is " + array[14] + "\n";			

						}
						System.out.println(printBuffer);
					}
				}
				// sleep for fifteen seconds
				try {
					Thread.sleep(15000);
				} 
				catch(InterruptedException e) { 
					System.out.println(e);
				}
			}
			catch(IOException e) {
				e.printStackTrace();
				System.out.println(e);
			}			
		}		
	}

//Check if the file has changed
	private LinkedList<String> filechange(String filename) throws IOException {		
		FileInputStream FStream = null;
		InputStreamReader InpStream = null;
		BufferedReader BR = null;
		LinkedList<String> printBuffer = null;
		try {
			FStream = new FileInputStream(filename);
			InpStream = new InputStreamReader(FStream);
			BR = new BufferedReader(InpStream);
			printBuffer = new LinkedList<String>();
			String Line;
			while((Line = BR.readLine()) != null) {				
				String[] hostFile = filename.split("\\.dat");
				String[] part = Line.split(" ");
				if(part.length == 3) {					
					printBuffer.add(hostFile[0] + "-" + part[0] + " " + Double.parseDouble(part[1]));
				}
			}
		}
		catch(IOException e) {
			System.err.println(e);
		}		
		BR.close();
		InpStream.close();
		FStream.close();	
		return printBuffer;
	}	



	private void Hostinfo(String filename) throws IOException {		
		FileInputStream FStream = null;
		InputStreamReader InpStream = null;
		BufferedReader BR = null;
		try {
			FStream = new FileInputStream(filename);
			InpStream = new InputStreamReader(FStream);
			BR = new BufferedReader(InpStream);
			String Line;
			while((Line = BR.readLine()) != null) {				
				String[] hostFile = filename.split("\\.dat");
				String[] part = Line.split(" ");
				if(part.length == 3) {					
					neighbors.add(part[0]);
					port.add(part[2]);
					hclist.put(hostFile[0] + "-" + part[0], Double.parseDouble(part[1]));					
				}
			}
			FStream = new FileInputStream(filename);
			InpStream = new InputStreamReader(FStream);
			BR = new BufferedReader(InpStream);
			while((Line = BR.readLine()) != null) {
				String[] hostFile = filename.split("\\.dat");
				String[] part = Line.split(" ");
				Vector<DistVect> dvLine = new Vector<DistVect>();
				if(part.length == 3) {				
					Iterator<String> itr = neighbors.iterator();
					while (itr.hasNext()) {
						String element = itr.next();
						if(element.equals(part[0])) {
							dvLine.add(new DistVect(hostFile[0], element, part[0], Double.parseDouble(part[1])));
						}
						else {							
							dvLine.add(new DistVect(hostFile[0], element, part[0], 99999.0));
						}											
					}

					hosts.put(hostFile[0] + "-" + part[0], dvLine);
				}				
			}
		}		
		catch(IOException e) {
			System.err.println(e);
		}            
		BR.close();
		InpStream.close();
		FStream.close();			        
	}	

	private String RoutingTable(Hashtable<String, Vector<DistVect>> disVect) {
		Double min = Double.MAX_VALUE;
		String printBuffer = "";		
		for(Vector<DistVect> vector : disVect.values()) {
			String source = "";
			String via = "";
			String dest = "";
			double[] costs = new double[vector.size()];			
			for(int j=0; j<vector.size(); j++) {
				costs[j] = vector.get(j).getCost();				
			}
			source = vector.get(findMinimum(costs)).getSource();
			dest = vector.get(findMinimum(costs)).getDestination();
			via = vector.get(findMinimum(costs)).getVia();
			min = vector.get(findMinimum(costs)).getCost();

			printBuffer += "shortest path " + source + " - " + dest + ": the next hop is " + via + " and the cost is " + min + "\n";
		}

		return printBuffer;			
	}

	private int findMinimum(double[] d) {
		double minimum = Double.MAX_VALUE;
		for(int i=0; i<d.length; i++) {
			if(minimum > d[i]) {
				minimum = d[i];
			}
		}

		for(int i=0; i<d.length; i++) {
			if(minimum == d[i]) {
				return i;
			}
		}
		return -1;
	}



}
