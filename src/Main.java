import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Map.Entry;

public class Main {

    private static HashMap<String, String> domains;
    private static String[] nodes = new String[2];
    private static DatagramSocket socket;

    public static void main(String[] args) throws Exception {
        nodes[0] = "192.168.0.4";
        nodes[1] = "192.168.0.6";

        domains = new HashMap<>();
        domains.put("GOOGLE", "10.0.0.1");
        domains.put("MERCADOLIBRE", "10.0.0.2");
        domains.put("FACEBOOK", "10.0.0.3");

        socket = new DatagramSocket(6001);

        // Lectura
        new Thread(() -> {
            while (true) {
                DatagramPacket packet = new DatagramPacket(new byte[1024], 1024);
                try {
                    socket.receive(packet);
                    String msg = new String(packet.getData(), "utf-8").trim();
                    System.out.println(msg);

                    if(msg.contains(":")){
                        String ip = packet.getAddress() + "";
                        ip = ip.substring(1);
                        int port = packet.getPort();
                        questionNode(ip, port, msg);
                    }else{
                        String ip = packet.getAddress() + "";
                        ip = ip.substring(1);
                        int port = packet.getPort();
                        question(ip, port, msg.split(":")[0]);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    private static void questionNode(String ip, int port, String msg) throws IOException {

        boolean find = false;

        msg=msg.substring(1);

        for (String domain : domains.keySet()) {
            if (msg.equals(domain)) {
                find = true;
                System.out.println(ip);
                System.out.println(port);
                System.out.println(domains.get(domain));
                sendline(ip, port,":" +domains.get(domain));
            }
        }

        if (!find) {
            sendline(ip, port, ":NULL");
        }
    }

    private static void question(String ip, int port, String msg) throws IOException {

        boolean find = false;

        for (String domain : domains.keySet()) {
            if (msg.equals(domain)) {
                find = true;
                System.out.println(ip);
                System.out.println(port);
                sendline(ip, port, domains.get(domain));
            }
        }

        if (!find) {
            Boolean send=false;
            sendline(nodes[0], 6001, ":"+msg);
            DatagramPacket packet = new DatagramPacket(new byte[1024], 1024);

            socket.receive(packet);
            String x = new String(packet.getData(), "utf-8").trim();
            if (!x.split(":")[1].equals("NULL")){
                sendline(ip, port, x.substring(1));
                send=true;
            }
            sendline(nodes[1], 6001, ":"+msg);
             packet = new DatagramPacket(new byte[1024], 1024);

            socket.receive(packet);
             x = new String(packet.getData(), "utf-8").trim();
            if (!x.split(":")[1].equals("NULL")){
                sendline(ip, port, x.substring(1));
                send=true;
            }

            if (!send){
                sendline(ip, port,"NUll");
            }
            
        }
    }


    private static void sendline(String ip, int port, String domain) throws IOException {
        System.out.println("Enviaaaa: " + domain);
        // Envio
        DatagramPacket packet = new DatagramPacket(
                domain.getBytes(),
                domain.getBytes().length,
                InetAddress.getByName(ip),
                port);

       // System.out.println(ip);
        //System.out.println(port);

        socket.send(packet);
    }


    
}
