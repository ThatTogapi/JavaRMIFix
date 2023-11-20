// AuctionServerImpl.java (server package)
package Server;

import Common.AuctionItem;
import Common.ClientInt;
import Common.Interface;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;

public class AuctionServerImpl extends UnicastRemoteObject implements Interface {

    private Map<Integer, ClientInt> clients = new HashMap<>();
    private Map<Integer, AuctionItem> auctionItems = new HashMap<>();

    public AuctionServerImpl() throws RemoteException {
        super();
    }

    public Map<Integer, AuctionItem> getAuctionItems() {
        return auctionItems;
    }

    @Override
    public void updateAuctionItem(int itemId, AuctionItem updatedItem) throws RemoteException {
        auctionItems.put(itemId, updatedItem);
    }

    public void putAuctionItems(AuctionItem auctionItem) {
        auctionItems.put(auctionItems.size(),auctionItem);
        System.out.println("Client " + auctionItem.getOwnerID() + " listed " + auctionItem.getItemId() + auctionItem.getItemName());
    }

    public Map<Integer, ClientInt> getClients() {
        return clients;
    }

    public void putClient(ClientInt client) throws RemoteException {
        clients.put(client.getClientId(), client);
        System.out.println("Client " + client.getClientId() + client.getClientPass() + " added.");
    }

    //    public AuctionItem getSpec(int itemId, int clientId) throws RemoteException {
//        AuctionItem item = auctionItems.get(itemId);
//        if (item != null) {
//            System.out.println("Client " + clientId + " requested item " + item);
//            return item;
//        } else {
//            throw new RemoteException("Item not found: " + itemId);
//        }
//    }

    public static void main(String[] args) {
        try {
            LocateRegistry.createRegistry(1099);
            AuctionServerImpl server = new AuctionServerImpl();
            Naming.rebind("rmi://localhost:1099/auction", server);
            System.out.println("Auction server started");
        } catch (RemoteException e) {
            System.err.println("Failed to start auction server: " + e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
