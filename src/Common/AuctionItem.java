package Common;

import java.io.Serializable;

public class AuctionItem implements Serializable {
    private int ItemId;
    private String ItemName;
    private String ItemDesc;
    private float startPrice;
    private float reservePrice;
    private int ownerID;
    private float currentBid;
    private Client currentBidder;
    private int auctionType;

    public AuctionItem(int ItemId, String ItemName, String ItemDesc, float startPrice, float reservePrice, int ownerID, float currentBid, Client currentBidder, int auctionType){
        this.ItemId = ItemId;
        this.ItemName = ItemName;
        this.ItemDesc = ItemDesc;
        this.startPrice = startPrice;
        this.reservePrice = reservePrice;
        this.ownerID = ownerID;
        this.currentBid = currentBid;
        this.currentBidder = currentBidder;
        this.auctionType = auctionType;
    }

    public int getItemId() {
        return ItemId;
    }

    public String getItemName() {
        return ItemName;
    }

    public String getItemDesc() {
        return ItemDesc;
    }

    public float getStartPrice() {
        return startPrice;
    }

    public float getReservePrice() {
        return reservePrice;
    }

    public int getOwnerID() {
        return ownerID;
    }

    public float getCurrentBid() {
        return currentBid;
    }

    public Client getCurrentBidder() {
        return currentBidder;
    }

    public int getAuctionType() {
        return auctionType;
    }

    public void setItemId(int itemId) {
        ItemId = itemId;
    }

    public void setItemName(String itemName) {
        ItemName = itemName;
    }

    public void setItemDesc(String itemDesc) {
        ItemDesc = itemDesc;
    }

    public void setStartPrice(float startPrice) {
        this.startPrice = startPrice;
    }

    public void setReservePrice(float reservePrice) {
        this.reservePrice = reservePrice;
    }

    public void setOwnerID(int ownerID) {
        this.ownerID = ownerID;
    }

    public void setCurrentBid(float currentBid) {
        this.currentBid = currentBid;
    }

    public void setCurrentBidder(Client currentBidder) {
        this.currentBidder = currentBidder;
    }

    public void setAuctionType(int auctionType) {
        this.auctionType = auctionType;
    }
}
