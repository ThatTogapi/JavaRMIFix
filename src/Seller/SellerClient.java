package Seller;

import Common.AuctionItem;
import Common.Client;
import Common.Interface;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.*;

public class SellerClient {


    public static Client client;
    public static boolean login_status = false;
    static List<Interface> connectedServers = new ArrayList<>();
    public static void main(String[] args) {
        try {
            Interface auctionServer = (Interface) Naming.lookup("rmi://localhost:1099/auction");
            connectedServers.add(auctionServer);

            System.out.println(auctionServer.getCurrentMemberAddresses().size());

            if (!auctionServer.getCurrentMemberAddresses().isEmpty()) {
                for (int i = 0; i < auctionServer.getCurrentMemberAddresses().size() - 1; i++) {
                    try {
                        Interface replicaCon = (Interface) Naming.lookup("rmi://localhost:1099/auction" + i);
                        connectedServers.add(replicaCon);
                        System.out.println(connectedServers);
                    } catch (Exception e) {
                        e.printStackTrace();
                        // Handle the exception as needed
                    }
                }
            }

            Scanner scanner = new Scanner(System.in);

            Client client = loginOrRegister(connectedServers);

            if (client != null) {
                sellerMenu(connectedServers);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String generateRandomKey() {
        // Generate a random 16-character secret key
        SecureRandom random = new SecureRandom();
        byte[] keyBytes = new byte[16];
        random.nextBytes(keyBytes);
        return bytesToHex(keyBytes);
    }

    public static String bytesToHex(byte[] bytes) {
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

    public static String generateSignature(String data, String secretKey) throws Exception {
        // Combine data with the secret key
        String dataToSign = data + secretKey;

        // Use SHA-256 for hashing
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(dataToSign.getBytes());

        // Convert the hash to a hexadecimal string
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }

        return hexString.toString();
    }

    public static Client loginOrRegister(List<Interface> connectedServers) throws RemoteException {
        Scanner scanner = new Scanner(System.in);
        Interface auctionServer = connectedServers.get(0);
        int switch_choice;
        do {
            System.out.println("Login or Register:");
            System.out.println("1. Login");
            System.out.println("2. Register");
            System.out.println("0. Exit");

            System.out.print("Enter your choice: ");
            switch_choice = scanner.nextInt();

            switch (switch_choice) {
                case 1:
                    client = performLogin(auctionServer);
                    break;
                case 2:
//                        for(Interface auctionServer : connectedServers) {
                    client = performRegistration(connectedServers);
                    if (client != null) {
                        login_status = true;
                    }
//                        }
                    break;
                case 0:
                    System.out.println("Exiting Seller Client...");
                    System.exit(0);
                default:
                    System.out.println("Invalid choice. Please try again.");

            }
        } while (!login_status);

        return client;
    }

    private static Client performRegistration(List<Interface> connectedServers) throws RemoteException {
        Scanner scanner = new Scanner(System.in);
        Interface auctionServer = connectedServers.get(0);
        try {
            System.out.print("Enter the login ID you want: ");
            int loginID = scanner.nextInt();
            if (auctionServer.getClients().containsKey(loginID)) {
                System.out.println("ID already in use please choose something else");
                return null;  // Return null to indicate registration failure
            }
            System.out.print("Enter your password: ");
            String password = scanner.next();
            System.out.print("Enter your name: ");
            String name = scanner.next();

            // Generate a random secret key during registration
            String secretKey = generateRandomKey();

            // Generate a signature for registration data
            String registrationData = loginID + password + name;
            String signature = generateSignature(registrationData, secretKey);

            client = new Client(loginID, password, name);

            // Send registration data, signature, and secret key to the server for verification
            for(Interface server : connectedServers) {
                boolean registrationSuccessful = server.registerClient(client, registrationData, signature, secretKey);

                if (!registrationSuccessful) {
                    System.out.println("Registration failed. Signature verification unsuccessful.");
                    return null;
                }
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            return null;
        }
        return client;
    }

    private static Client performLogin(Interface auctionServer) throws RemoteException {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter your login ID: ");
        int loginID = scanner.nextInt();
        System.out.print("Enter your password: ");
        String password = scanner.next();
        try {
            if (Objects.equals(auctionServer.getClients().get(loginID).getClientPass(), password)) {
                System.out.println("Welcome " + auctionServer.getClients().get(loginID).getClientName() + ".");
                client = (Client) auctionServer.getClients().get(loginID);
                login_status = true;
                return client;
            } else {
                System.out.println("Login ID or password incorrect try again.");
                performLogin(auctionServer);
                login_status = false;
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
        return null;
    }

    public static void listListings(Interface auctionServer) throws RemoteException {

        if(auctionServer.getAuctionItems() == null){
            System.out.println("No active auctions.");
            return;
        }
//        Map<Integer, ClientInt> clients = auctionServer.getClients();
        for(int i = 0; i < auctionServer.getAuctionItems().size(); i++){
//            AuctionItem item = auctionServer.getAuctionItems().get(i);
            String name;
            if (Objects.isNull(auctionServer.getAuctionItems().get(i).getCurrentBidder())) {
                name = "No Bidders.";
            } else {
                name = auctionServer.getAuctionItems().get(i).getCurrentBidder().getClientName();
            }
            if(auctionServer.getAuctionItems().get(i).getAuctionType() == 4 || auctionServer.getAuctionItems().get(i).getAuctionType() == 2) continue;
            System.out.println("ItemID: " + i + ": " + auctionServer.getAuctionItems().get(i).getItemName() + " \nCurrent Bid: " +
                    auctionServer.getAuctionItems().get(i).getCurrentBid() +  "\nCurrent Bidder: " + name +
                    "\nItem Description: " + auctionServer.getAuctionItems().get(i).getItemDesc());
            System.out.println("-----------------------");
        }
    }

    public static void sellerMenu(List<Interface> connectedServers) throws RemoteException {

        Interface auctionServer = connectedServers.get(0);

        Scanner scanner = new Scanner(System.in);
        int switch_choice;

        do {

            System.out.println("Seller Menu:");
            System.out.println("1. Create Listing");
            System.out.println("2. Manage Listing");
            System.out.println("0. Log Out");

            System.out.print("Enter your choice: ");
            switch_choice = scanner.nextInt();

            switch (switch_choice) {
                case 1:
                    createListing(connectedServers);
                    break;
                case 2:
                    manageListing(auctionServer);
                    break;
                case 0:
                    System.out.println("Logging Out of Seller Client...");
                    login_status = false;
                    loginOrRegister(connectedServers);
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");

            }
        } while (login_status);

    }

    private static void manageListing(Interface auctionServer) throws RemoteException {

        Scanner scanner = new Scanner(System.in);
        int switch_choice;

        do {

            System.out.println("Seller Menu:");
            System.out.println("1. Close auction");
            System.out.println("2. List all active auctions");
            System.out.println("3. List all active double auctions");
            System.out.println("0. Go back");

            System.out.print("Enter your choice: ");
            switch_choice = scanner.nextInt();

            switch (switch_choice) {
                case 1:
                    closeListing(auctionServer);
                    break;
                case 2:
                    listListings(auctionServer);
                    break;
                case 3:
                    listDoubleAuctions(auctionServer);
                    break;
                case 0:
                    System.out.println("Going back to the seller menu.");
                    sellerMenu(connectedServers);
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");

            }
        } while (login_status);

    }

    private static void listDoubleAuctions(Interface auctionServer) throws RemoteException {
        for (Map.Entry<String, List<AuctionItem>> entry : auctionServer.getDoubleAuctions().entrySet()) {
            String itemName = entry.getKey();
            List<AuctionItem> auctionList = entry.getValue();

            System.out.println("Double Auctions for item: " + itemName + " started by " + auctionServer.getClients().get(auctionServer.getDoubleAuctions().get(itemName).get(0).getOwnerID()).getClientName());

            if (auctionList != null) {
                for (AuctionItem auctionItem : auctionList) {
                    System.out.println("ItemID: " + auctionItem.getItemId() +
                            "\nItem Name: " + auctionItem.getItemName() +
                            "\nCurrent Bid: " + auctionItem.getCurrentBid() +
                            "\nCurrent Bidder: " + (auctionItem.getCurrentBidder() != null ? auctionItem.getCurrentBidder().getClientName() : "No Bidder") +
                            "\nItem Description: " + auctionItem.getItemDesc() +
                            "\nAuction Owner: " + auctionServer.getClients().get(auctionItem.getOwnerID()).getClientName());
                    System.out.println("-----------------------");
                }
            } else {
                System.out.println("No auctions for this item.");
            }
        }
    }

    private static void closeListing(Interface auctionServer) throws RemoteException {

        Scanner scanner = new Scanner(System.in);
        System.out.print("Which auction would you like to close:");
        int itemID = scanner.nextInt();
        int switch_choice;

        AuctionItem item = auctionServer.getAuctionItems().get(itemID);

        if (item.getOwnerID() != client.getClientId()) {
            System.out.println("This auction wasn't started by you.");
            sellerMenu(connectedServers);
        }

        if(auctionServer.getAuctionItems().isEmpty()){
            System.out.println("No active auctions.");
            sellerMenu(connectedServers);
        }

        String name;
        if (Objects.isNull(item.getCurrentBidder())) {
            name = "No Bidders.";
        } else {
            name = item.getCurrentBidder().getClientName();
        }

        System.out.println(item.getItemId() + ": " + item.getItemName() + " \nCurrent Bid: " +
                item.getCurrentBid() + "\nItem Description: " + item.getItemDesc() + "\nCurrent Bidder: " + name);
        System.out.println("------------------------------");
        System.out.println("Are you sure you want to close this listing?");
        System.out.println("1: Yes");
        System.out.println("2: No");
        System.out.println("0: Go back.");
        System.out.print("Your choice:");
        switch_choice = scanner.nextInt();

        switch (switch_choice) {
            case 1:
                if (item.getAuctionType() != 4 && item.getAuctionType() != 2) {
                    if (item.getCurrentBid() > item.getReservePrice() || item.getCurrentBidder() == null) {
                        System.out.println("Auction didn't beat the reserve price. Auction closed without a winner.");
                        item.setCurrentBidder(client);
                    } else {
                        System.out.println("The auction closed with a bid of " + item.getCurrentBid() + " and the winner is: " + item.getCurrentBidder().getClientName());
                    }
                    item.setAuctionType(4);
                    for(Interface server : connectedServers) {
                        server.updateAuctionItem(item.getItemId(), item);
                    }
                } else if (item.getAuctionType() == 2) {
                    System.out.println("Double auction closed, matching sellers with buyers.");
                    closeDoubleAuction(auctionServer, item.getItemName());
                } else {
                    System.out.println("This auction was already over.");
                }
                break;
            case 2:
                System.out.println("Going back to seller menu.");
                sellerMenu(connectedServers);
                break;
            case 0:
                System.out.println("Going back to seller menu.");
                sellerMenu(connectedServers);
                break;
            default:
                System.out.println("Invalid choice. Please try again.");
        }
    }

    private static void closeDoubleAuction(Interface auctionServer, String itemName) throws RemoteException {

//        List<AuctionItem> doubleAuctionItems = auctionServer.getDoubleAuctions().get(itemName).size();
        List<AuctionItem> doubleAuctionItems = auctionServer.getDoubleAuctions().get(itemName);
        System.out.println();

        // Check if the double auction has any items
        if (doubleAuctionItems != null && !doubleAuctionItems.isEmpty()) {
            if(doubleAuctionItems.get(0).getOwnerID() != client.getClientId()){
                System.out.println("This auction wasn't started by you.");
                return;
            }
            System.out.println("Closing double auction for item: " + itemName);
            // Iterate through each item in the double auction.
            for (AuctionItem doubleAuctionItem : doubleAuctionItems) {
                if (doubleAuctionItem.getCurrentBid() > doubleAuctionItem.getReservePrice() || doubleAuctionItem.getCurrentBidder() == null) {
                    doubleAuctionItem.setAuctionType(4);
                    for(Interface server :connectedServers) {
                        server.updateAuctionItem(doubleAuctionItem.getItemId(), doubleAuctionItem);
                    }
                    String bidderName = (doubleAuctionItem.getCurrentBidder() != null)
                            ? doubleAuctionItem.getCurrentBidder().getClientName()
                            : "No Bidder";
                    System.out.println("Auction below didn't beat the reserve price. Auction closed without a winner.");
                    System.out.println("ItemID: " + doubleAuctionItem.getItemId() +
                            "\nItem Name: " + doubleAuctionItem.getItemName() +
                            "\nCurrent Bid: " + doubleAuctionItem.getCurrentBid() +
                            "\nCurrent Bidder: " + bidderName +
                            "\nItem Description: " + doubleAuctionItem.getItemDesc() +
                            "\nItem Owner: " + auctionServer.getClients().get(doubleAuctionItem.getOwnerID()).getClientName());
                    System.out.println("-----------------------");
                    doubleAuctionItem.setCurrentBidder(client);
                    continue;
                }
                doubleAuctionItem.setAuctionType(4);
                for(Interface server : connectedServers) {
                    server.updateAuctionItem(doubleAuctionItem.getItemId(), doubleAuctionItem);
                }
                String bidderName = (doubleAuctionItem.getCurrentBidder() != null)
                        ? doubleAuctionItem.getCurrentBidder().getClientName()
                        : "No Bidder";
                System.out.println("Auction below was won by: " + doubleAuctionItem.getCurrentBidder().getClientName() + " with a bid of: " + doubleAuctionItem.getCurrentBid());
                System.out.println("ItemID: " + doubleAuctionItem.getItemId() +
                        "\nItem Name: " + doubleAuctionItem.getItemName() +
                        "\nItem Description: " + doubleAuctionItem.getItemDesc());
                System.out.println("-----------------------");
            }
            System.out.println("Double auction closed.");
        } else {
            System.out.println("No active double auctions for item: " + itemName);
        }
    }


    private static void createListing(List<Interface> connectedServers) throws RemoteException {

        Interface auctionServer = connectedServers.get(0);
        Scanner scanner = new Scanner(System.in);
        int switch_choice;

        System.out.println("Please Enter Item Details:");

        System.out.print("Item name: ");
        String itemName = scanner.nextLine();
        System.out.print("Item description: ");
        String itemDesc = scanner.nextLine();
        System.out.print("Starting price for the item: ");
        float startPrice = scanner.nextFloat();
        System.out.print("Reserve price for the item: ");
        float reservePrice = scanner.nextFloat();

        AuctionItem item = new AuctionItem(auctionServer.getAuctionItems().size(),itemName,itemDesc,startPrice,reservePrice, client.getClientId(),startPrice, null,0);


        do {

            System.out.println("Choose Auction Type:");
            System.out.println("1. Start as Forward Auction");
            System.out.println("2. Start as Double Auction");
            System.out.println("3. Start as Reverse Auction");
            System.out.println("0. Go Back");

            System.out.print("Enter your choice: ");
            switch_choice = scanner.nextInt();

            switch (switch_choice) {
                case 1:
                    for(Interface server : connectedServers) {
                        ForwardAuction(server, item);
                    }
                    sellerMenu(connectedServers);
                    break;
                case 2:
                    item.setAuctionType(2);
                    DoubleAuction(connectedServers, item);
                    System.out.println(auctionServer.getDoubleAuctions());
                    sellerMenu(connectedServers);
                    break;
                case 3:
                    for(Interface server : connectedServers) {
                        ForwardAuction(server, item);
                    }
                    sellerMenu(connectedServers);
                    break;
                case 0:
                    System.out.println("Going back to seller menu.");
                    sellerMenu(connectedServers);
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");

            }
        } while (switch_choice != 0);
    }

    private static void DoubleAuction(List<Interface> connectedServers, AuctionItem item) throws RemoteException {
        Scanner scanner = new Scanner(System.in);
        Interface auctionServer = connectedServers.get(0);

        Map<String, List<AuctionItem>> activeDoubles = auctionServer.getDoubleAuctions();
        if(activeDoubles.containsKey(item.getItemName().toLowerCase())){
            System.out.println("A similar item is already in a double auction. Would you like to join that auction?\nIt was started by: " +
                    auctionServer.getClients().get(activeDoubles.get(item.getItemName().toLowerCase()).get(0).getOwnerID()).getClientName());

            System.out.println("1: Yes");
            System.out.println("0: Go back.");
            System.out.print("Your choice:");
            int switch_choice = scanner.nextInt();

            switch (switch_choice) {
                case 1:
                    System.out.println("Joined the double auction.");
                    for(Interface server : connectedServers) {
                        server.putAuctionItems(item);
                        server.putDoubleAuctions(item.getItemName().toLowerCase(), item);
                    }
                    return;
                case 0:
                    System.out.println("Going back to seller menu.");
                    sellerMenu(connectedServers);
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
        System.out.println("Starting a new double auction.");
        for(Interface server : connectedServers) {
            server.putAuctionItems(item);
            server.putDoubleAuctions(item.getItemName().toLowerCase(), item);
        }
    }

    private static void ForwardAuction(Interface auctionServer, AuctionItem item) throws RemoteException {
        auctionServer.putAuctionItems(item);
    }
}