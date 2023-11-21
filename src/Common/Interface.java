package Common;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

public interface Interface extends Remote {
    Map<Integer, ClientInt> getClients() throws RemoteException;

    void putClient(ClientInt client) throws RemoteException;
    Map<Integer, AuctionItem> getAuctionItems() throws RemoteException;
    Map<String, List<AuctionItem>> getDoubleAuctions() throws RemoteException;
    void putDoubleAuctions(String itemName, AuctionItem item) throws  RemoteException;

    boolean registerClient(ClientInt client, String registrationData, String clientSignature, String secretKey) throws RemoteException;

    void putAuctionItems(AuctionItem auctionItem) throws RemoteException;

    void updateAuctionItem(int itemId, AuctionItem updatedItem) throws RemoteException;

    String bytesToHex(byte[] bytes) throws RemoteException;

    String generateRandomKey() throws RemoteException;
    boolean verifySignature(String data, String signature, String secretKey) throws Exception;

}
