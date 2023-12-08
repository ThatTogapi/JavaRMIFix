package Server;

import Common.AuctionItem;
import Common.ClientInt;
import org.jgroups.Address;
import org.jgroups.Message;
import org.jgroups.MessageListener;

import java.io.*;
import java.util.List;
import java.util.Map;

public class StateSyncMessage extends Message implements Externalizable, MessageListener, StateSyncMessageInt {
    private Map<Integer, AuctionItem> auctionItems;
    private Map<String, List<AuctionItem>> doubleAuctions;
    private Map<Integer, ClientInt> clients;
    private Address senderAddress;
    private long auctionItemsVersion;
    private long doubleAuctionsVersion;
    private long clientsVersion;

    public StateSyncMessage() {
        // Default constructor for Externalizable
    }

    public StateSyncMessage(Address senderAddress, Map<Integer, AuctionItem> auctionItems, Map<String, List<AuctionItem>> doubleAuctions, Map<Integer, ClientInt> clients,
                            long auctionItemsVersion, long doubleAuctionsVersion, long clientsVersion) {
        this.senderAddress = senderAddress;
        this.auctionItems = auctionItems;
        this.doubleAuctions = doubleAuctions;
        this.clients = clients;
        this.auctionItemsVersion = auctionItemsVersion;
        this.doubleAuctionsVersion = doubleAuctionsVersion;
        this.clientsVersion = clientsVersion;
    }

    public Address getSenderAddress() {
        return senderAddress;
    }

    public Map<Integer, AuctionItem> getAuctionItems() {
        return auctionItems;
    }

    public Map<String, List<AuctionItem>> getDoubleAuctions() {
        return doubleAuctions;
    }

    public Map<Integer, ClientInt> getClients() {
        return clients;
    }

    public long getAuctionItemsVersion() {
        return auctionItemsVersion;
    }

    public long getDoubleAuctionsVersion() {
        return doubleAuctionsVersion;
    }

    public long getClientsVersion() {
        return clientsVersion;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(senderAddress);
        out.writeObject(auctionItems);
        out.writeObject(doubleAuctions);
        out.writeObject(clients);
        out.writeLong(auctionItemsVersion);
        out.writeLong(doubleAuctionsVersion);
        out.writeLong(clientsVersion);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        senderAddress = (Address) in.readObject();
        auctionItems = (Map<Integer, AuctionItem>) in.readObject();
        doubleAuctions = (Map<String, List<AuctionItem>>) in.readObject();
        clients = (Map<Integer, ClientInt>) in.readObject();
        auctionItemsVersion = in.readLong();
        doubleAuctionsVersion = in.readLong();
        clientsVersion = in.readLong();
    }

    @Override
    public void receive(Message msg) {
        System.out.println("Received a message: " + msg.getObject());
    }

    @Override
    public void getState(OutputStream outputStream) throws Exception {

    }

    @Override
    public void setState(InputStream inputStream) throws Exception {

    }
}
