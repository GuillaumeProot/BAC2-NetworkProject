package reso.examples.selectiverepeat;

import reso.common.Link;
import reso.common.Network;
import reso.ethernet.EthernetAddress;
import reso.ethernet.EthernetFrame;
import reso.ethernet.EthernetInterface;
import reso.examples.pingpong.AppReceiver;
import reso.examples.pingpong.AppSender;
import reso.examples.static_routing.AppSniffer;
import reso.ip.IPAddress;
import reso.ip.IPHost;
import reso.scheduler.AbstractScheduler;
import reso.scheduler.Scheduler;
import reso.utilities.NetworkBuilder;

public class Demo {
    /* Enable or disable packet capture (can be used to observe ARP messages) */
    private static final boolean ENABLE_SNIFFER= false;


    public static void main(String [] args) {
        int packetQuantities;
        float lostPacket;
        float lostAck;
        try{
            packetQuantities=Integer.parseInt(args[0]);
            lostPacket=Float.parseFloat(args[1]);
            lostAck=Float.parseFloat(args[2]);
        }catch (Exception e){
            System.out.println("valeurs incorrectes. Les paramètres par défaut sont appliqués");
            packetQuantities = 10;
            lostPacket = 0.2f;
            lostAck = 0.0f;
        }
        AbstractScheduler scheduler= new Scheduler();
        Network network= new Network(scheduler);
        try {
            final EthernetAddress MAC_ADDR1= EthernetAddress.getByAddress(0x00, 0x26, 0xbb, 0x4e, 0xfc, 0x28);
            final EthernetAddress MAC_ADDR2= EthernetAddress.getByAddress(0x00, 0x26, 0xbb, 0x4e, 0xfc, 0x29);
            final IPAddress IP_ADDR1= IPAddress.getByAddress(192, 168, 0, 1);
            final IPAddress IP_ADDR2= IPAddress.getByAddress(192, 168, 0, 2);

            IPHost host1= NetworkBuilder.createHost(network, "H1", IP_ADDR1, MAC_ADDR1);
            host1.getIPLayer().addRoute(IP_ADDR2, "eth0");

            host1.addApplication(new ApplicationSender(host1,IP_ADDR2,packetQuantities,lostPacket));

            IPHost host2= NetworkBuilder.createHost(network,"H2", IP_ADDR2, MAC_ADDR2);
            host2.getIPLayer().addRoute(IP_ADDR1, "eth0");
            host2.addApplication(new ApplicationReceiver(host2,lostAck));

            EthernetInterface h1_eth0= (EthernetInterface) host1.getInterfaceByName("eth0");
            EthernetInterface h2_eth0= (EthernetInterface) host2.getInterfaceByName("eth0");

            // Connect both interfaces with a 5000km long link
            new Link<EthernetFrame>(h1_eth0, h2_eth0, 5000000, 100000);

            host1.start();
            host2.start();

            scheduler.run();
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace(System.err);
        }
    }

}
