package fr.redteam.core;
public class Target {

    private String host;
    private int port;

    public Target() {
        this.host = "";
        this.port = -1;
    }

    public Target(String host) {
        this.host = host != null ? host : "";
        this.port = -1;
    }

    public Target(String host, int port) {
        this.host = host != null ? host : "";
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host != null ? host : "";
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
