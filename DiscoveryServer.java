import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map.Entry;

public class DiscoveryServer {

	private static HashMap<String, IPAddressAndPort> convAddressMap = new HashMap<String, IPAddressAndPort>();

	public static void main(String[] args) {

	
		  // check argument
		  if (args.length != 1) { 
		      System.out .println(  "Wrong argument. Run this server by inputing \"java DiscoryServer port\""); 
		      System.exit(-1); } 
		  // host a server 
		  int port =  Integer.parseInt(args[0]);
		 

		
		ServerSocket server = null;
		try {
			server = new ServerSocket(port);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		System.out.println("Server hosted at port " + port);
		try {
			while (true) {
				Socket socket = server.accept();
				System.out.println("Got Connection from:"
						+ socket.getInetAddress() + ":" + socket.getPort());
				process(socket);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Handle the request sent by socket
	 * 
	 * @param socket
	 */
	private static void process(Socket socket) {
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			out.println("You are connected to the Proxy Server!\n");
			String request = in.readLine();
			System.out.println("Recv. msg: " + request);
			if (request == null) {
				// wrong argument input by client
				System.out.println("No input recieved.");
				closeSocket(in, out, socket);
				return;
			}
			processCertainAction(request, out);
			closeSocket(in, out, socket);
		} catch (Exception e) {
			e.printStackTrace();
			try {
				socket.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}

	/**
	 * do certain action requested by client
	 * 
	 * @param request
	 */
	private static void processCertainAction(String request, PrintWriter out) {
		if (request.toLowerCase().startsWith("set ")) {
			// store address of server
			insertConvAddress(request.substring(4), out);
		} else if (request.toLowerCase().startsWith("delete ")) {
			// delete requested address
			deleteConvAddress(request.substring(7), out);
		} else if (request.toLowerCase().startsWith("get ")) {
			// get address for client
			getConvAddress(request.substring(4), out);
		} else {
			out.println("Unsupported command");
		}
	}

	private static void getConvAddress(String type, PrintWriter out) {
//		for (Entry<String, IPAddressAndPort> entry : convAddressMap.entrySet()) {
//			System.out.println(entry.getKey()+" key="+entry.getValue().address+":"+entry.getValue().port);
//		}
		String paras[] = type.split(" ");
		IPAddressAndPort addr = null;
		if (paras.length != 2) {
			out.println("Wrong argument.\nUsage: get ft in");
			return;
		}
		if ((addr = convAddressMap.get(paras[0] + " " + paras[1])) != null
				|| (addr = convAddressMap.get(paras[1] + " " + paras[0])) != null) {
			// before sending address to client, check if server is on
			try {
			    System.out.println("try:"+addr.address+":"+addr.port);
				Socket socket = new Socket(addr.address, addr.port);
				System.out.println("established");
				socket.close();
				System.out.println("closed");
				out.println("address "+addr.address + " " + addr.port);
			} catch (Exception e) {
				e.printStackTrace();
				// delete unavailable server from map
				deleteConvAddress(paras[1] + " " + paras[0], out);
				out.println("Unsupported conversion type.");
			}
		} else {
			// unsupported conversion type
			out.println("Unsupported conversion type.");
		}
	}

	/**
	 * Insert requested type into map
	 * 
	 * @param address
	 * @param out
	 */
	private static void insertConvAddress(String address, PrintWriter out) {
		String paras[] = address.split(" ");
		if (paras.length != 4) {
			out.println("Wrong argument.\nUsage: get ft in");
			return;
		}
		IPAddressAndPort ap = new IPAddressAndPort(paras[2],
				Integer.parseInt(paras[3]));
		// check existence
		if (convAddressMap.get(paras[0] + " " + paras[1]) != null) {

		}
		convAddressMap.put(paras[0] +" "+ paras[1], ap);
		out.println("Done.");

	}

	/**
	 * Delete requested type from map
	 * 
	 * @param type
	 *            type to delete
	 * @param out
	 */
	private static void deleteConvAddress(String type, PrintWriter out) {
		String paras[] = type.split(" ");
		if (paras.length != 2) {
			out.println("Wrong argument.\nUsage: get ft in");
			return;
		}
		IPAddressAndPort addr = new IPAddressAndPort();
		if ((addr = convAddressMap.get(paras[0] + " " + paras[1])) != null) {
			convAddressMap.remove(addr);
		} else if ((addr = convAddressMap.get(paras[1] + " " + paras[0])) != null) {
			convAddressMap.remove(addr);
		}
		out.println("Done.");
	}

	// close socket to the client
	private static void closeSocket(BufferedReader in, PrintWriter out,
			Socket socket) {
		try {
			out.close();
			out.flush();
			in.close();
			socket.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}

class IPAddressAndPort {

	public IPAddressAndPort() {
	}

	public IPAddressAndPort(String address, int port) {
		this.address = address;
		this.port = port;
	}

	String address;
	int port;
}