package Buyer;

import Common.AuctionItem;
import Common.Client;
import Common.Interface;
import Seller.SellerClient;

import java.rmi.Naming;
import java.rmi.RemoteException;
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
        for(int i = 0; i < auctionServer.getAuctionItems().size(); i++){
//            AuctionItem item = auctionServer.getAuctionItems().get(i);
            System.out.println("ItemID: " + i + ":" + auctionServer.getAuctionItems().get(i).getItemName() + " \nCurrent Bid: " +
                    auctionServer.getAuctionItems().get(i).getCurrentBid() + "\nItem Description: " + auctionServer.getAuctionItems().get(i).getItemDesc());
        }
    }

    public static void buyerMenu(Interface auctionServer) throws RemoteException {

        Scanner scanner = new Scanner(System.in);
        int switch_choice;

        do {

            System.out.println("Buyer Menu:");
            System.out.println("1. Browse listings");
            System.out.println("2. Bid on listing with ID");
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

    private static void bidOnListing(Interface auctionServer) throws RemoteException {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Which item would you like to bid on:");
        int itemID = scanner.nextInt();
        AuctionItem item = auctionServer.getAuctionItems().get(itemID);
//        System.out.println(client.getClientId());
        if(item.getCurrentBidder() == client){
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
        System.out.println(newBid);
        while(newBid <= item.getCurrentBid()){
            System.out.print("Your bid is too low. Please enter a higher bid:");
            newBid = scanner.nextFloat();
        }
        item.setCurrentBid(newBid);
        item.setCurrentBidder(client);
        auctionServer.updateAuctionItem(item.getItemId(), item);
        System.out.println("Your bid is currently the highest bid!");
    }
}
