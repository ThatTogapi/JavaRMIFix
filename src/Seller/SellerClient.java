package Seller;

import Common.AuctionItem;
import Common.Client;
import Common.ClientInt;
import Common.Interface;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;

public class SellerClient {


    public static Client client;
    public static boolean login_status = false;

    public static void main(String[] args) {
        try {
            Interface auctionServer = (Interface) Naming.lookup("rmi://localhost:1099/auction");

            Scanner scanner = new Scanner(System.in);

            Client client = loginOrRegister(auctionServer);

            if (client != null) {
                sellerMenu(auctionServer);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
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

            client = new Client(loginID, password, name);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            return null;
        }
        auctionServer.putClient(client);
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
            if(auctionServer.getAuctionItems().get(i).getAuctionType() == 4) continue;
            System.out.println("ItemID: " + i + ": " + auctionServer.getAuctionItems().get(i).getItemName() + " \nCurrent Bid: " +
                    auctionServer.getAuctionItems().get(i).getCurrentBid() +  "\nCurrent Bidder: " + name +
                    "\nItem Description: " + auctionServer.getAuctionItems().get(i).getItemDesc());
            System.out.println("-----------------------");
        }
    }

    public static void sellerMenu(Interface auctionServer) throws RemoteException {

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
                    createListing(auctionServer);
                    break;
                case 2:
                    manageListing(auctionServer);
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

    private static void manageListing(Interface auctionServer) throws RemoteException {

        Scanner scanner = new Scanner(System.in);
        int switch_choice;

        do {

            System.out.println("Seller Menu:");
            System.out.println("1. Close auction");
            System.out.println("2. List all active auctions");
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
                case 0:
                    System.out.println("Going back to the seller menu.");
                    sellerMenu(auctionServer);
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");

            }
        } while (login_status);

    }

    private static void closeListing(Interface auctionServer) throws RemoteException {

        Scanner scanner = new Scanner(System.in);
        System.out.print("Which auction would you like to close:");
        int itemID = scanner.nextInt();
        int switch_choice;

        AuctionItem item = auctionServer.getAuctionItems().get(itemID);

        if (item.getOwnerID() != client.getClientId()) {
            System.out.println("This auction wasn't started by you.");
            sellerMenu(auctionServer);
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
                if (item.getAuctionType() != 4) {
                    if (item.getCurrentBid() > item.getReservePrice() || item.getCurrentBidder() == null) {
                        System.out.println("Auction didn't beat the reserve price. Auction closed without a winner.");
                        item.setCurrentBidder(client);
                    } else {
                        System.out.println("The auction closed with a bid of " + item.getCurrentBid() + " and the winner is: " + item.getCurrentBidder().getClientName());
                    }
                    item.setAuctionType(4);
                    auctionServer.updateAuctionItem(item.getItemId(), item);
                } else {
                    System.out.println("This auction was already over.");
                }
                break;
            case 2:
                System.out.println("Going back to seller menu.");
                sellerMenu(auctionServer);
                break;
            case 0:
                System.out.println("Going back to seller menu.");
                sellerMenu(auctionServer);
                break;
            default:
                System.out.println("Invalid choice. Please try again.");
        }
    }

    private static void createListing(Interface auctionServer) throws RemoteException {

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
            System.out.println("1. Start Auction");
//            System.out.println("2. Reverse Auction");
            System.out.println("2. Start as Double Auction");
            System.out.println("0. Go Back");

            System.out.print("Enter your choice: ");
            switch_choice = scanner.nextInt();

            switch (switch_choice) {
                case 1:
                    ForwardAuction(auctionServer, item);
                    sellerMenu(auctionServer);
                    break;
//                case 2:
//                    ReverseAuction(auctionServer, item);
//                    sellerMenu(auctionServer);
//                    break;
                case 2:
                    DoubleAuction(auctionServer, item);
                    sellerMenu(auctionServer);
                    break;
                case 0:
                    System.out.println("Going back to seller menu.");
                    sellerMenu(auctionServer);
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");

            }
        } while (switch_choice != 0);
    }

    private static void DoubleAuction(Interface auctionServer, AuctionItem item) throws RemoteException {
        item.setAuctionType(2);
        auctionServer.putAuctionItems(item);
    }

//    private static void ReverseAuction(Interface auctionServer, AuctionItem item) throws RemoteException {
//        item.setAuctionType(1);
//        auctionServer.putAuctionItems(item);
//    }

    private static void ForwardAuction(Interface auctionServer, AuctionItem item) throws RemoteException {
        auctionServer.putAuctionItems(item);
    }
}