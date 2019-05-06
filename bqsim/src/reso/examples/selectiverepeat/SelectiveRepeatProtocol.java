package reso.examples.selectiverepeat;

import java.util.ArrayList;

import reso.ip.Datagram;
import reso.ip.IPHost;
import reso.ip.IPInterfaceAdapter;
import reso.ip.IPInterfaceListener;

public class SelectiveRepeatProtocol implements IPInterfaceListener {
    private ArrayList queue;
    private Queue<SelectiveRepeatPacket> window;
    private int wantedNum;
    private boolean isInit = false;
    private boolean initMessageSend = false;
    public final static String INIT_REQUEST = "init request";
    public static final int IP_PROTO_SELECTIVEREPEAT = Datagram.allocateProtocolNumber("SELECTIVE_REPEAT");
    private int numSeq;
    private CongestionControl controller;
    private int N;
    private float lostAck, lostPacket;
    private final IPHost host;

    public SelectiveRepeatProtocol(IPHost host, float lostPacket, float lostAck) {
        this.numSeq = 0;
        controller = new CongestionControl();
        N = controller.initSize;
        this.host = host;
        this.lostAck = lostAck;
        this.lostPacket = lostPacket;
    }

    @Override
    public void receive(IPInterfaceAdapter src, Datagram datagram) throws Exception {
        
    }

    public void send(IpAddress src, String data) throws Exeption {
        if(!isInit && !initMessageSend){
            SelectiveRepeatMessage message = new SelectiveRepeatMessage(INIT_REQUEST, Integer.MIN_VALUE);
            host.getIPLayer().send(IPAddress.ANY, src, IP_PROTO_SELECTIVEREPEAT, message);
        }
        SelectiveRepeatMessage message = new SelectiveRepeatMessage(data, numSeq);
        SelectiveRepeatPacket packet = new SelectiveRepeatPacket(message, src, -1);
        window.offer(packet);
        it = window.iterator();
        numSeq++;
        if (isInit)
            sendNext();
    }
}
