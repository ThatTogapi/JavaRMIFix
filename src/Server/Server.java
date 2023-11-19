package Server;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

//Acts as a middle-man between seller and buyer clients.
public class Server {
    public static void main(String[] args) {
        try {
            // Create an instance of RMIInterfaceImpl
            AuctionServerImpl rmiImpl = new AuctionServerImpl();

            // Start the RMI registry on port 1099 (default RMI registry port)
            Registry registry = LocateRegistry.createRegistry(1099);

            // Bind the RMI object to the registry with the name "AuctionSystemService"
            registry.rebind("auction", rmiImpl);

            System.out.println("Auction Server is up");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
