package Common;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ClientInt extends Remote {
    int getClientId() throws RemoteException;

    String getClientPass() throws RemoteException;

    String getClientName() throws RemoteException;

    boolean isClientSeller() throws RemoteException;

    void setClientId(int clientId) throws RemoteException;

    void setClientName(String clientName) throws RemoteException;

    void setClientPass(String clientPass) throws RemoteException;

    void setClientSeller(boolean clientSeller) throws RemoteException;
}
