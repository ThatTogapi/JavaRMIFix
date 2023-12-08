package Server;


public class Server {
    public static void main(String[] args) {
        try {
            AuctionServerImpl.startOriginalServer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
