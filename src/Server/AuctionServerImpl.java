package Server;

import Common.AuctionItem;
import Common.ClientInt;
import Common.Interface;
import Seller.SellerClient;
import org.jgroups.*;
import org.jgroups.util.Util;

import java.io.*;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AuctionServerImpl extends UnicastRemoteObject implements Interface, Receiver, Serializable {

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
    private static List<AuctionServerImpl> replicas = new ArrayList<>();

    public List<AuctionServerImpl> getReplicas() {
        return replicas;
    }

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
            requestStateFromExistingReplicas();
            Thread syncThread = new Thread(() -> {
                while (true) {
                    try {
                        if (channel.isConnected()) {
                            byte[] stateBytes = Util.objectToByteBuffer(new StateSyncMessage(channel.getAddress(), auctionItems, doubleAuctions, clients,
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


    private static void addReplica(AuctionServerImpl replica) throws Exception {
        Thread.sleep(2000);
        replicas.add(replica);
        eventLoop();
    }

    private void updateReplicasList(List<Address> currentMembers) {
        List<AuctionServerImpl> updatedReplicas = new ArrayList<>();

        // Iterate through all replicas and check if they are present in the current members
        for (AuctionServerImpl replica : replicas) {
            if (currentMembers.contains(replica.getChannel().getAddress()) && !updatedReplicas.contains(replica)) {
                updatedReplicas.add(replica);
            }
        }

        // Update the replicas list
        replicas = updatedReplicas;

        System.out.println("Updated Replicas List: " + replicas);
        System.out.println(currentMembers);
    }

    public JChannel getChannel() {
        return channel;
    }

    public List<String> getCurrentMemberAddresses() {
        List<Address> members = channel.getView().getMembers();
        List<String> addresses = new ArrayList<>();

        for (Address member : members) {
            addresses.add(member.toString());
        }

        return addresses;
    }
    private void requestStateFromExistingReplicas() {
        try {
            View currentView = channel.getView();
            List<Address> existingReplicas = new ArrayList<>(currentView.getMembers());

            // Remove the current replica's address
            existingReplicas.remove(channel.getAddress());

            // Iterate through existing replicas and request state from each
            for (Address replicaAddress : existingReplicas) {
                StateSyncMessage requestStateMessage = new StateSyncMessage(channel.getAddress(),
                        new HashMap<>(), new HashMap<>(), new HashMap<>(), 0, 0, 0);

                // Send a state request message to the existing replica
                Message requestStateMsg = new Message(replicaAddress, null, Util.objectToByteBuffer(requestStateMessage));
                channel.send(requestStateMsg);

                // Wait for a response containing the state
                // You may need to add some logic here to handle the response
                // and update the local state of the new replica
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        try {
            if (args.length == 0) {
                startOriginalServer();
            } else if (args.length == 1 && args[0].equalsIgnoreCase("add")) {
                AuctionServerImpl replica = new AuctionServerImpl();
                System.out.println(replicas.size());
                // Dynamically generate a unique RMI name for each replica
                Naming.rebind("rmi://localhost:1099/auction" + replicas.size(), replica);

                channel.setReceiver(replica);
                replica.start(true);

                addReplica(replica);
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

        replicas.add(originalServer);

        Thread.sleep(2000);

        eventLoop();
    }

    @Override
    public void viewAccepted(View view) {
        System.out.println("Current View: " + view);
        List<Address> currentMembers = view.getMembers();
        // Update the replicas list with the current members
        updateReplicasList(currentMembers);
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
