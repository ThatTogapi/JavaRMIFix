package Common;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

public interface Interface extends Remote {
    Map<Integer, ClientInt> getClients() throws RemoteException;

    void putClient(ClientInt client) throws RemoteException;
    Map<Integer, AuctionItem> getAuctionItems() throws RemoteException;

    void putAuctionItems(AuctionItem auctionItem) throws RemoteException;

}
