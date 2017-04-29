import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Vector;

public class Main_Activity {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		int port = Integer.parseInt(args[0]);;
		String filename = args[1];
		
		Hashtable<String, Double> ncpair = new Hashtable<String, Double>();
		ArrayList<String> neighborslist = new ArrayList<String>();
		Hashtable<String, Vector<DistVect>> Host = new Hashtable<String, Vector<DistVect>>();		
	
		if(port <= 1024) {
			System.out.println("Port numbers above 1024 is preferred.");
			System.exit(-1);
		}			

		//Call receiver to listen on particular port
		new Receiver(port, filename, ncpair, neighborslist, Host).start();

		//Call sender to send to particular port
		new Sender(filename, ncpair, neighborslist, Host).start();
		

	}

}
