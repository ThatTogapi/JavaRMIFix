package Common;

import java.io.Serializable;

public class AuctionItem implements Serializable {
    private int ItemId;
    private String ItemName;
    private String ItemDesc;
    private float startPrice;
    private float reservePrice;

    public AuctionItem(int ItemId, String ItemName, String ItemDesc, float startPrice, float reservePrice){
        this.ItemId = ItemId;
        this.ItemName = ItemName;
        this.ItemDesc = ItemDesc;
        this.startPrice = startPrice;
        this.reservePrice = reservePrice;
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

}
