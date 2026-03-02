package fr.redteam.c2;

/**
 * Modèle représentant un beacon (agent) connecté au C2.
 */
public class Beacon {

    private String id;
    private String listenerHost;
    private int listenerPort;
    private String remoteAddress;
    private String lastSeen;
    private String status;

    public Beacon() {
        this.id = "";
        this.listenerHost = "";
        this.listenerPort = -1;
        this.remoteAddress = "";
        this.lastSeen = "";
        this.status = "unknown";
    }

    public Beacon(String id, String listenerHost, int listenerPort, String remoteAddress) {
        this.id = id != null ? id : "";
        this.listenerHost = listenerHost != null ? listenerHost : "";
        this.listenerPort = listenerPort;
        this.remoteAddress = remoteAddress != null ? remoteAddress : "";
        this.lastSeen = "";
        this.status = "active";
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id != null ? id : "";
    }

    public String getListenerHost() {
        return listenerHost;
    }

    public void setListenerHost(String listenerHost) {
        this.listenerHost = listenerHost != null ? listenerHost : "";
    }

    public int getListenerPort() {
        return listenerPort;
    }

    public void setListenerPort(int listenerPort) {
        this.listenerPort = listenerPort;
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public void setRemoteAddress(String remoteAddress) {
        this.remoteAddress = remoteAddress != null ? remoteAddress : "";
    }

    public String getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(String lastSeen) {
        this.lastSeen = lastSeen != null ? lastSeen : "";
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status != null ? status : "unknown";
    }
}
