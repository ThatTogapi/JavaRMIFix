package Server;

import Common.AuctionItem;
import Common.ClientInt;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class State implements Serializable {
    private Map<Integer, AuctionItem> auctionItems;
    private Map<String, List<AuctionItem>> doubleAuctions;
    private Map<Integer, ClientInt> clients;
    private long auctionItemsVersion;
    private long doubleAuctionsVersion;
    private long clientsVersion;

    public State(Map<Integer, AuctionItem> auctionItems, Map<String, List<AuctionItem>> doubleAuctions,
                 Map<Integer, ClientInt> clients, long auctionItemsVersion, long doubleAuctionsVersion, long clientsVersion) {
        this.auctionItems = auctionItems;
        this.doubleAuctions = doubleAuctions;
        this.clients = clients;
        this.auctionItemsVersion = auctionItemsVersion;
        this.doubleAuctionsVersion = doubleAuctionsVersion;
        this.clientsVersion = clientsVersion;
    }

    // Add getters for each field

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
}
