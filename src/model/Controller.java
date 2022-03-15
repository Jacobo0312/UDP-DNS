package model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;

public class Controller {

    private HashMap<String, String> domains;
    private ArrayList<Node> nodes;
    private DatagramSocket socket;

    public Controller() {
        this.domains = new HashMap<>();
        this.nodes = new ArrayList<>();

        try {
            this.socket = new DatagramSocket(6001);
        } catch (SocketException e) {
            e.printStackTrace();
        }

    }

    public void init() {

        try {
            importDomains();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            importNodes();
        } catch (IOException e) {
            e.printStackTrace();
        }

        receptor();
    }

    private void importDomains() throws IOException {

        FileReader file = new FileReader("src/data/domains.csv");

        BufferedReader br = new BufferedReader(file);

        String line = "";

        while ((line = br.readLine()) != null) {
            domains.put(line.split(";")[0], line.split(";")[1]);
        }

        System.out.println(domains.toString());

        br.close();

    }

    private void importNodes() throws IOException {

        FileReader file = new FileReader("src/data/nodes.csv");

        BufferedReader br = new BufferedReader(file);

        String line = "";

        while ((line = br.readLine()) != null) {
            nodes.add(new Node(line.split(";")[0], Integer.parseInt(line.split(";")[1])));

        }

        System.out.println(nodes.toString());
        br.close();

    }

    private void receptor() {
        // Lectura
        new Thread(() -> {
            while (true) {
                DatagramPacket packet = new DatagramPacket(new byte[1024], 1024);
                try {
                    socket.receive(packet);
                    String msg = new String(packet.getData(), "utf-8").trim();
                    System.out.println(msg);

                    String ip = packet.getAddress() + "";
                    ip = ip.substring(1);
                    int port = packet.getPort();

                    // Node
                    if (msg.contains(":")) {
                        nodeRequest(ip, port, msg);

                        // Client
                    } else {
                        ;
                        request(ip, port, msg.split(":")[0]);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void nodeRequest(String ip, int port, String msg) throws IOException {

        boolean find = false;

        msg = msg.substring(1);

        for (String domain : domains.keySet()) {
            if (msg.equals(domain)) {
                find = true;
                System.out.println(ip);
                System.out.println(port);
                System.out.println(domains.get(domain));
                send(ip, port, ":" + domains.get(domain));
            }
        }

        if (!find) {
            send(ip, port, ":NULL");
        }
    }

    private void request(String ip, int port, String msg) throws IOException {

        boolean find = false;


        //Searching in my domains
        for (String domain : domains.keySet()) {
            if (msg.equals(domain)) {
                find = true;
                System.out.println(ip);
                System.out.println(port);
                send(ip, port, domains.get(domain));
            }
        }

        if (!find) {

            boolean send = false;

            for (Node node : nodes) {
                send(node.getIp(), node.getPort(), ":" + msg);

                DatagramPacket packet = new DatagramPacket(new byte[1024], 1024);
    
                socket.receive(packet);
                String x = new String(packet.getData(), "utf-8").trim();
                if (!x.split(":")[1].equals("NULL")) {
                    send(ip, port, x.substring(1));
                    send = true;
                    break;
                }

                
            }

            
            if (!send) {
                send(ip, port, "NUll");
            }


            /*
            Boolean send = false;
            send(nodes.get(0).getIp(), nodes.get(0).getPort(), ":" + msg);

            DatagramPacket packet = new DatagramPacket(new byte[1024], 1024);

            socket.receive(packet);
            String x = new String(packet.getData(), "utf-8").trim();
            if (!x.split(":")[1].equals("NULL")) {
                send(ip, port, x.substring(1));
                send = true;
            }
            send(nodes.get(1).getIp(), nodes.get(0).getPort(), ":" + msg);
            packet = new DatagramPacket(new byte[1024], 1024);

            socket.receive(packet);
            x = new String(packet.getData(), "utf-8").trim();
            if (!x.split(":")[1].equals("NULL")) {
                send(ip, port, x.substring(1));
                send = true;
            }

            if (!send) {
                send(ip, port, "NUll");
            }
            */

        }
    }

    private void send(String ip, int port, String msg) throws IOException {
        System.out.println("SEND: " + msg);
        // Envio
        DatagramPacket packet = new DatagramPacket(
                msg.getBytes(),
                msg.getBytes().length,
                InetAddress.getByName(ip),
                port);

        socket.send(packet);
    }

}