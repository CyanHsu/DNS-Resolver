import java.io.IOException;
import java.net.SocketException;

public class Main {
    public static void main(String[] args) throws IOException {
        DNSServer dnsServer = new DNSServer(8053);
    }
}