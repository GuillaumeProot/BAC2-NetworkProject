package reso.examples.selectiverepeat;

import reso.ip.Datagram;
import reso.ip.IPHost;
import reso.ip.IPInterfaceAdapter;
import reso.ip.IPInterfaceListener;

public class SelectiveRepeatProtocol implements IPInterfaceListener {

    public static final int IP_PROTO_SELECTIVEREPEAT = Datagram.allocateProtocolNumber("SELECTIVE_REPEAT");
    private CongestionControl controller;
    private int N;
    private float lostAck, lostPacket;
    //private final IPHost host;

    public SelectiveRepeatProtocol(IPHost host, float lostPacket, float lostAck) {
        controller = new CongestionControl();
        N = controller.initSize;
        this.lostAck = lostAck;
        this.lostPacket = lostPacket;
    }

    @Override
    public void receive(IPInterfaceAdapter src, Datagram datagram) throws Exception {

    }
}
