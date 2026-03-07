package fr.redteam.payload;

public class PayloadConfig {

    private String listenerHost;
    private int listenerPort;
    private String outputDir;
    private String apkTemplatePath;

    public PayloadConfig() {
        this.listenerHost = "0.0.0.0";
        this.listenerPort = 4444;
        this.outputDir = ".";
        this.apkTemplatePath = "";
    }

    public PayloadConfig(String listenerHost, int listenerPort) {
        this.listenerHost = listenerHost != null ? listenerHost : "0.0.0.0";
        this.listenerPort = listenerPort > 0 ? listenerPort : 4444;
        this.outputDir = ".";
        this.apkTemplatePath = "";
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

    public String getOutputDir() {
        return outputDir;
    }

    public void setOutputDir(String outputDir) {
        this.outputDir = outputDir != null ? outputDir : ".";
    }

    public String getApkTemplatePath() {
        return apkTemplatePath;
    }

    public void setApkTemplatePath(String apkTemplatePath) {
        this.apkTemplatePath = apkTemplatePath != null ? apkTemplatePath : "";
    }
}
