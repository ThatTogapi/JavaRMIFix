package Server;

import Common.AuctionItem;
import Common.ClientInt;
import Common.Interface;
import Seller.SellerClient;
import org.jgroups.*;
import org.jgroups.util.Util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AuctionServerImpl extends UnicastRemoteObject implements Interface, Receiver {

    static JChannel channel;

    static {
        try {
            channel = new JChannel();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Map<Integer, ClientInt> clients = new HashMap<>();
    private Map<Integer, AuctionItem> auctionItems = new HashMap<>();
    private Map<String, List<AuctionItem>> doubleAuctions = new HashMap<>();

    private long auctionItemsVersion = 0;
    private long doubleAuctionsVersion = 0;
    private long clientsVersion = 0;
    private static List<Object> replicas = new ArrayList<>();


    public AuctionServerImpl() throws Exception {
        super();
    }

    void start(boolean isReplica) throws Exception {
        if (!isReplica) {
            if (channel.isConnected()) {
                channel.setReceiver(this);
            } else {
                channel.setReceiver(this);
                channel.connect("GroupCluster");
            }
        }

        if (isReplica) {
            if (channel.isConnected()) {
                channel.setReceiver(this);
            } else {
                channel.connect("GroupCluster");
            }

            Thread syncThread = new Thread(() -> {
                while (true) {
                    try {
                        if (channel.isConnected()) {
                            byte[] stateBytes = Util.objectToByteBuffer(new StateSyncMessage(channel.getAddress(),auctionItems, doubleAuctions, clients,
                                    auctionItemsVersion, doubleAuctionsVersion, clientsVersion));

                            Message syncMessage = new Message(null, null, stateBytes);
                            channel.send(syncMessage);
                        }

                        Thread.sleep(5000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            syncThread.setDaemon(true);
            syncThread.start();
        }
    }

    private static void eventLoop() {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            try {
                System.out.print("> ");
                System.out.flush();
                String line = in.readLine().toLowerCase();

                if (line.startsWith("quit") || line.startsWith("exit")) {
                    break;
                } else {
                    Message msg = new Message(null, null, line);
                    channel.send(msg);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public boolean verifySignature(String data, String signature, String secretKey) throws Exception {
        String newSignature = SellerClient.generateSignature(data, secretKey);
        return newSignature.equals(signature);
    }

    public Map<Integer, AuctionItem> getAuctionItems() {
        return auctionItems;
    }

    public Map<String, List<AuctionItem>> getDoubleAuctions() {
        return doubleAuctions;
    }

    public void putDoubleAuctions(String itemName, AuctionItem item) {
        if (doubleAuctions.containsKey(itemName)) {
            List<AuctionItem> auction = doubleAuctions.get(itemName);
            if (auction != null) {
                auction.add(item);
                doubleAuctions.put(itemName, auction);
            } else {
                List<AuctionItem> newAuction = new ArrayList<>();
                newAuction.add(item);
                doubleAuctions.put(itemName, newAuction);
            }
        } else {
            List<AuctionItem> newAuction = new ArrayList<>();
            newAuction.add(item);
            doubleAuctions.put(itemName, newAuction);
        }
        doubleAuctionsVersion++;
    }

    @Override
    public void updateAuctionItem(int itemId, AuctionItem updatedItem) throws RemoteException {
        auctionItems.put(itemId, updatedItem);
        auctionItemsVersion++;
    }

    public String generateRandomKey() {
        SecureRandom random = new SecureRandom();
        byte[] keyBytes = new byte[16];
        random.nextBytes(keyBytes);
        return bytesToHex(keyBytes);
    }

    public String bytesToHex(byte[] bytes) {
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
            boolean signatureVerified = verifySignature(registrationData, clientSignature, secretKey);

            if (!signatureVerified) {
                System.out.println("Registration failed. Signature verification unsuccessful.");
                return false;
            }
            System.out.println(clientSignature + " " + secretKey);
            putClient(client);

            return true;
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            return false;
        }
    }

    public void putAuctionItems(AuctionItem auctionItem) {
        auctionItems.put(auctionItems.size(), auctionItem);
        auctionItemsVersion++;
        System.out.println("Client " + auctionItem.getOwnerID() + " listed " + auctionItem.getItemId() + auctionItem.getItemName());
    }

    public Map<Integer, ClientInt> getClients() {
        return clients;
    }

    public void putClient(ClientInt client) throws RemoteException {
        clients.put(client.getClientId(), client);
        clientsVersion++;
        System.out.println("Client " + client.getClientId() + client.getClientPass() + " added.");
    }

    private State getCurrentState() {
        return new State(auctionItems, doubleAuctions, clients, auctionItemsVersion, doubleAuctionsVersion, clientsVersion);
    }


    private static void addReplica() throws Exception {
        AuctionServerImpl replica = new AuctionServerImpl();

        // Dynamically generate a unique RMI name for each replica
        Naming.rebind("rmi://localhost:1099/auction" + replicas.size(), replica);

        // Set up JChannel and start the replica
        replica.channel = new JChannel();
        replica.channel.setReceiver(replica);
        replica.channel.connect("GroupCluster");
        replica.start(true);

        replicas.add(replica);

        Thread.sleep(2000);

        eventLoop();
    }


    public static void main(String[] args) {
        try {
            if (args.length == 0) {
                startOriginalServer();
            } else if (args.length == 1 && args[0].equalsIgnoreCase("add")) {
                addReplica();
            } else {
                System.out.println("Invalid command-line arguments.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void startOriginalServer() throws Exception {
        LocateRegistry.createRegistry(1099);

        AuctionServerImpl originalServer = new AuctionServerImpl();
        Naming.rebind("rmi://localhost:1099/auction", originalServer);
        System.out.println("Original auction server started");
        originalServer.start(false);

        Thread.sleep(2000);

        eventLoop();
    }

    @Override
    public void viewAccepted(View view) {
        System.out.println("Current View: " + view);
    }

    @Override
    public void suspect(Address address) {
        // Implement if needed
    }

    @Override
    public void block() {
        // Implement if needed
    }

    @Override
    public void unblock() {
        // Implement if needed
    }

    @Override
    public void receive(Message message) {
        Object messageObject = message.getObject();

        if (messageObject instanceof State) {
            // Handle the received entire state message
            State receivedState = (State) messageObject;

            // Update the state on the replica
            auctionItems = receivedState.getAuctionItems();
            doubleAuctions = receivedState.getDoubleAuctions();
            clients = receivedState.getClients();

            auctionItemsVersion = receivedState.getAuctionItemsVersion();
            doubleAuctionsVersion = receivedState.getDoubleAuctionsVersion();
            clientsVersion = receivedState.getClientsVersion();

            System.out.println("Received the entire state from " + message.getSrc() + " ClSize: " + clients.size());
        } else if (messageObject instanceof StateSyncMessage) {
            // Handle synchronization with the received data
            StateSyncMessage syncMessage = (StateSyncMessage) messageObject;
            Address senderAddress = syncMessage.getSenderAddress();

            // Check if the sender is a replica
            if (replicas.contains(senderAddress)) {
                // Handle synchronization with the received data
                synchronizeWithReplica(syncMessage);

                // Broadcast the most populated versions to other replicas
                broadcastMostPopulatedVersions();
            }
        } else {
            // Handle other types of messages
            System.out.println(messageObject + " by " + message.getSrc() + " ClSize: " + clients.size());
        }
    }



    private void synchronizeWithReplica(StateSyncMessage syncMessage) {
        try {
            // If the replica has an older version, send the entire state to the replica
            if (syncMessage.getAuctionItemsVersion() < auctionItemsVersion ||
                    syncMessage.getDoubleAuctionsVersion() < doubleAuctionsVersion ||
                    syncMessage.getClientsVersion() < clientsVersion) {

                State currentState = getCurrentState();
                byte[] stateBytes = Util.objectToByteBuffer(currentState);

                Message stateMessage = new Message(syncMessage.getSenderAddress(), null, stateBytes);
                channel.send(stateMessage);

                System.out.println("Sent the entire state to " + syncMessage.getSenderAddress());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("State synchronized from " + syncMessage.getSenderAddress() + " ClSize: " + clients.size());
    }

    private void broadcastMostPopulatedVersions() {
        try {
            StateSyncMessage syncMessage = new StateSyncMessage(channel.getAddress(), auctionItems, doubleAuctions, clients, auctionItemsVersion, doubleAuctionsVersion, clientsVersion);
            channel.send(syncMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void getState(OutputStream outputStream) throws Exception {
        // Implement if needed
    }

    @Override
    public void setState(InputStream inputStream) throws Exception {
        // Implement if needed
    }
}
