package Buyer;

import Common.AuctionItem;
import Common.Client;
import Common.ClientInt;
import Common.Interface;
import Seller.SellerClient;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;


public class BuyerClient {


    public static Client client;
    public static boolean login_status = false;

    public static void main(String[] args) {
        try {
            Interface auctionServer = (Interface) Naming.lookup("rmi://localhost:1099/auction");

            Scanner scanner = new Scanner(System.in);

            client = loginOrRegister(auctionServer);

            if (client != null) {
                buyerMenu(auctionServer);
//                System.out.println("anan");
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

    public static Client loginOrRegister(Interface auctionServer) throws RemoteException {
        Scanner scanner = new Scanner(System.in);
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
                    client = performRegistration(auctionServer);
                    if (client != null) {
                        login_status = true;
                    }
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

    private static Client performRegistration(Interface auctionServer) throws RemoteException {
        Scanner scanner = new Scanner(System.in);
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
            boolean registrationSuccessful = auctionServer.registerClient(client, registrationData, signature, secretKey);

            if (!registrationSuccessful) {
                System.out.println("Registration failed. Signature verification unsuccessful.");
                return null;
            }

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            return null;
        }
        return client;
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

    public static void buyerMenu(Interface auctionServer) throws RemoteException {

        Scanner scanner = new Scanner(System.in);
        int switch_choice;

        do {

            System.out.println("Buyer Menu:");
            System.out.println("1. Browse listings");
            System.out.println("2. Bid on listing with ID");
            System.out.println("3. Search for an item");
            System.out.println("4. See double auctions");
            System.out.println("0. Log Out");

            System.out.print("Enter your choice: ");
            switch_choice = scanner.nextInt();

            switch (switch_choice) {
                case 1:
                    listListings(auctionServer);
                    break;
                case 2:
                    bidOnListing(auctionServer);
                    break;
                case 3:
                    searchListings(auctionServer);
                    break;
                case 4:
                    listDoubleAuctions(auctionServer);
                    break;
                case 0:
                    System.out.println("Logging Out of Seller Client...");
                    login_status = false;
                    loginOrRegister(auctionServer);
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");

            }
        } while (login_status);

    }

    private static void searchListings(Interface auctionServer) throws RemoteException {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter the search term: ");
        String searchTerm = scanner.nextLine().toLowerCase();

        System.out.println("Search Results:");
        Map<Integer, ClientInt> clients = auctionServer.getClients();

        for (Map.Entry<Integer, AuctionItem> entry : auctionServer.getAuctionItems().entrySet()) {
            AuctionItem item = entry.getValue();

            // Convert item details to lowercase for case-insensitive search
            String itemName = item.getItemName().toLowerCase();
            String itemDesc = item.getItemDesc().toLowerCase();

            String name;
            if (Objects.isNull(item.getCurrentBidder())) {
                name = "No Bidders.";
            } else {
                name = item.getCurrentBidder().getClientName();
            }

            // Check if the search term is present in item name or description
            if (itemName.contains(searchTerm) || itemDesc.contains(searchTerm)) {
                System.out.println("ItemID: " + item.getItemId() +
                        "\nItem Name: " + item.getItemName() +
                        "\nCurrent Bid: " + item.getCurrentBid() +
                        "\nCurrent Bidder: " + name +
                        "\nItem Description: " + item.getItemDesc() +
                        "\nItem Seller: " + clients.get(item.getOwnerID()).getClientName() +
                        "\n------------------------------");
            }
        }
    }

    private static void bidOnListing(Interface auctionServer) throws RemoteException {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Which item would you like to bid on:");
        int itemID = scanner.nextInt();
        AuctionItem item = auctionServer.getAuctionItems().get(itemID);
        if(item.getCurrentBidder() != null && item.getCurrentBidder().getClientId() == client.getClientId()){
            System.out.println("You are already the highest bidder in this auction.");
            return;
        }
        if(item.getOwnerID() == client.getClientId()){
            System.out.println("This is your own auction.");
            return;
        }
        System.out.println(item.getItemId() + ":" + item.getItemName() + " \nCurrent Bid: " +
                item.getCurrentBid() + "\nItem Description: " + item.getItemDesc());
        System.out.print("Your bid: ");
        float newBid = scanner.nextFloat();
        while(newBid <= item.getCurrentBid()){
            System.out.print("Your bid is too low. Please enter a higher bid:");
            newBid = scanner.nextFloat();
        }
        item.setCurrentBid(newBid);
        item.setCurrentBidder(client);
        auctionServer.updateAuctionItem(item.getItemId(), item);
        if(item.getAuctionType() == 2){
            auctionServer.putDoubleAuctions(item.getItemName(), item);
        }
        System.out.println("Your bid is currently the highest bid!");
    }
}
