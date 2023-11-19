package Common;

import java.io.Serializable;

public class Client implements ClientInt, Serializable {
    private int ClientId;
    private String ClientName;
    private String ClientPass;
    private boolean ClientSeller;

    public Client(int ClientId, String ClientPass, String ClientName, boolean ClientSeller){
        this.ClientId = ClientId;
        this.ClientPass = ClientPass;
        this.ClientName = ClientName;
        this.ClientSeller = ClientSeller;
    }

    public int getClientId() {
        return ClientId;
    }

    public String getClientPass() {
        return ClientPass;
    }

    public String getClientName() {
        return ClientName;
    }

    public boolean isClientSeller() {
        return ClientSeller;
    }

    public void setClientId(int clientId) {
        ClientId = clientId;
    }

    public void setClientName(String clientName) {
        ClientName = clientName;
    }

    public void setClientPass(String clientPass) {
        ClientPass = clientPass;
    }

    public void setClientSeller(boolean clientSeller) {
        ClientSeller = clientSeller;
    }
}
