package Server;

import Common.AuctionItem;
import Common.ClientInt;

import java.util.List;
import java.util.Map;

public interface StateSyncMessageInt {
    Map<Integer, AuctionItem> getAuctionItems();

    Map<String, List<AuctionItem>> getDoubleAuctions();

    Map<Integer, ClientInt> getClients();
}
