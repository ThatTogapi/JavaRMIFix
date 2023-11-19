package Seller;

import Common.Client;
import Common.Interface;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.Objects;
import java.util.Scanner;

public class SellerClient {
    private static Client client;
    private static boolean login_status = false;

    public static void main(String[] args) {
        try {
            Interface auctionServer = (Interface) Naming.lookup("rmi://localhost:1099/auction");

            Scanner scanner = new Scanner(System.in);

            loginOrRegister(auctionServer);

            if (client != null) {
                sellerMenu(auctionServer);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void sellerMenu(Interface auctionServer) {
        System.out.println("Seller Menu:");

    }

    private static void loginOrRegister(Interface auctionServer) throws RemoteException {

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
                    performLogin(auctionServer);
                    break;
                case 2:
                    performRegistration(auctionServer);
                    break;
                case 0:
                    System.out.println("Exiting Seller Client...");
                    System.exit(0);
                default:
                    System.out.println("Invalid choice. Please try again.");

            }
        } while (!login_status);
    }

    private static void performRegistration(Interface auctionServer) throws RemoteException {
        Scanner scanner = new Scanner(System.in);
        try {
            System.out.print("Enter the login ID you want: ");
            int loginID = scanner.nextInt();
            if (auctionServer.getClients().containsKey(loginID)) {
                System.out.println("ID already in use please choose something else");
                loginOrRegister(auctionServer);
            }
            System.out.print("Enter your password: ");
            String password = scanner.next();
            System.out.print("Enter your name: ");
            String name = scanner.next();

            client = new Client(loginID, password, name, true);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            return;
        }
        auctionServer.putClient(client);
        loginOrRegister(auctionServer);

    }

    private static void performLogin(Interface auctionServer) throws RemoteException {
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
            } else {
                System.out.println("Login ID or password incorrect try again.");
                performLogin(auctionServer);
                login_status = false;
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}