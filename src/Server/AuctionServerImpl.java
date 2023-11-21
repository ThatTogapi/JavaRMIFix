// AuctionServerImpl.java (server package)
package Server;

import Common.AuctionItem;
import Common.ClientInt;
import Common.Interface;
import Seller.SellerClient;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AuctionServerImpl extends UnicastRemoteObject implements Interface {

    private Map<Integer, ClientInt> clients = new HashMap<>();
    private Map<Integer, AuctionItem> auctionItems = new HashMap<>();
    private Map<String, List<AuctionItem>> doubleAuctions = new HashMap<>();

    public AuctionServerImpl() throws RemoteException {
        super();
    }

    public boolean verifySignature(String data, String signature, String secretKey) throws Exception {
        // Generate a new signature using the same secret key
        String newSignature = SellerClient.generateSignature(data, secretKey);

        // Compare the new signature with the provided signature
        return newSignature.equals(signature);
    }

    public Map<Integer, AuctionItem> getAuctionItems() {
        return auctionItems;
    }

    public Map<String, List<AuctionItem>> getDoubleAuctions() {
        return doubleAuctions;
    }

    public void putDoubleAuctions(String itemName, AuctionItem item) {
        // Check if the itemName already exists in the map
        if (doubleAuctions.containsKey(itemName)) {
            // If it does, retrieve the existing list and add the new item
            List<AuctionItem> auction = doubleAuctions.get(itemName);

            // Ensure that the list is not null before adding the item
            if (auction != null) {
                auction.add(item);
                doubleAuctions.put(itemName, auction);
            } else {
                // If the list is null, create a new list and add the item
                List<AuctionItem> newAuction = new ArrayList<>();
                newAuction.add(item);
                doubleAuctions.put(itemName, newAuction);
            }
        } else {
            // If itemName does not exist, create a new list and add the item
            List<AuctionItem> newAuction = new ArrayList<>();
            newAuction.add(item);
            doubleAuctions.put(itemName, newAuction);
        }
    }


    @Override
    public void updateAuctionItem(int itemId, AuctionItem updatedItem) throws RemoteException {
        auctionItems.put(itemId, updatedItem);
    }

    public String generateRandomKey() {
        // Generate a random 16-character secret key
        SecureRandom random = new SecureRandom();
        byte[] keyBytes = new byte[16];
        random.nextBytes(keyBytes);
        return bytesToHex(keyBytes);
    }

    public String bytesToHex(byte[] bytes) {
        // Convert byte array to hexadecimal string
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    @Override
    public boolean registerClient(ClientInt client, String registrationData, String clientSignature, String secretKey) throws RemoteException {
        try {
            // Verify the signature
            boolean signatureVerified = verifySignature(registrationData, clientSignature, secretKey);

            if (!signatureVerified) {
                System.out.println("Registration failed. Signature verification unsuccessful.");
                return false;
            }
            System.out.println(clientSignature + " " + secretKey);
            // Registration logic...
            putClient(client);

            return true;
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            return false;
        }
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
