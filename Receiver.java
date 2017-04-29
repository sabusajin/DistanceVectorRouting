
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Receiver extends Thread {
	
	private int port;
	protected DatagramSocket ReceiverSocket = null; 
	ArrayList<String> neighborslist = null;
	Hashtable<String, Double> ncpair = null;	
	private String file;
	Hashtable<String, Vector<DistVect>> hcpair = null;
	
	public Receiver(int port, String file, Hashtable<String, Double> ncpair, ArrayList<String> neighborslist, Hashtable<String, Vector<DistVect>> Host) throws IOException {
		super("Receiver");
		this.port = port;
		this.file = file;        
		ReceiverSocket = new DatagramSocket(port);		
		this.ncpair = ncpair;
		this.neighborslist = neighborslist;
		this.hcpair = Host;		
	}

	public void run() { 
		while(true) {
			try {
				byte[] recvBuffer = new byte[32768];                
				DatagramPacket receiverPacket = new DatagramPacket(recvBuffer, recvBuffer.length);
				ReceiverSocket.receive(receiverPacket); 
				Hashtable<String, Vector<DistVect>> latest = hcpair;
				String received = new String(receiverPacket.getData(), 0, receiverPacket.getLength());
				String[] hostFile = file.split("\\.dat");
				LinkedList<DistVect> recvList = readReceived(received);				
				String start = hostFile[0];
				for(int i=0; i<recvList.size(); i++) {
					DistVect table = recvList.get(i);
					if(latest.containsKey(start + "-" + table.getDestination())) {
						Vector<DistVect> distVect = latest.get(start + "-" + table.getDestination());
						int index = neighborslist.indexOf(table.getSource());
						double newCost = ncpair.get(start + "-" + table.getSource()) + table.getCost();
						if(distVect.get(index).getCost() > newCost) {
							distVect.get(index).setCost(newCost);
						}
					}
					else {
						if(start.equals(table.getDestination())) {
							continue;
						}
						// new destination
						else {
							Vector<DistVect> newVector = new Vector<DistVect>();							
							for(int k=0; k<neighborslist.size(); k++) {
								newVector.add(new DistVect(start, neighborslist.get(k), table.getDestination(), 99999.0));
							}
							int index = neighborslist.indexOf(table.getSource());
							newVector.set(index, new DistVect(start, table.getSource(), table.getDestination(), ncpair.get(start + "-" + table.getSource()) + table.getCost()));
							hcpair.put(start + "-" + table.getDestination(), newVector);
						}						
					}
				}			

			} 
			catch(IOException e) {
				e.printStackTrace();
			}			
		}
	}

	private LinkedList<DistVect> readReceived(String received) {
		
		//Match the string received and add it to the routelist
		String receivedReg = "(?:([A-Za-z\\s]+path ))([A-Za-z\\d\\.]+) - ([A-Za-z\\d\\.]+):(?:([A-Za-z\\s]+is ))([A-Za-z\\d\\.]+)(?:([A-Za-z\\s]+)+)([\\d.]+)";		
		LinkedList<DistVect> routeList = new LinkedList<DistVect>();
		String[] lines = received.split("\\n");			
		Pattern RdPattern = Pattern.compile(receivedReg, Pattern.CASE_INSENSITIVE|Pattern.DOTALL);
		for(int i=0; i<lines.length; i++) {			
			String Nodestart = "";
			String Nodeend = "";
			String Nodevia = "";
			String Nodecost = "";
			Matcher PattR = RdPattern.matcher(lines[i]);
			if(PattR.find()) {			
				Nodestart = PattR.group(2);
				Nodeend = PattR.group(3);
				Nodevia = PattR.group(5);
				Nodecost = PattR.group(7);
			}
			routeList.add(new DistVect(Nodestart, Nodevia, Nodeend, Double.parseDouble(Nodecost)));			
		}
		return routeList;
	}

}
