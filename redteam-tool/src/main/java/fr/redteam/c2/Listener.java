package fr.redteam.c2;

import fr.redteam.core.Module;
import fr.redteam.core.Report;
import fr.redteam.core.Target;

import java.util.ArrayList;
import java.util.List;

public class Listener implements Module {

    private final List<Beacon> beacons = new ArrayList<>();

    @Override
    public String getName() {
        return "Listener";
    }

    @Override
    public String getDescription() {
        return "Listener pour beacons C2 (écoute les connexions agents)";
    }

    @Override
    public void run(Target target, Report report) {
        String host = target.getHost();
        int port = target.getPort();
        if (host == null || host.isEmpty()) {
            host = "0.0.0.0";
        }
        if (port <= 0) {
            port = 4444;
        }
        report.addFinding("Listener", "Listener configuré sur " + host + ":" + port);
    }


    public void registerBeacon(Beacon beacon) {
        if (beacon != null) {
            beacons.add(beacon);
        }
    }

    public List<Beacon> getBeacons() {
        return new ArrayList<>(beacons);
    }
}
