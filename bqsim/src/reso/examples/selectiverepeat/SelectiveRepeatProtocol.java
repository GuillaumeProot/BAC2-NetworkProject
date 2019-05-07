package reso.examples.selectiverepeat;

import java.util.ArrayList;
import java.util.Queue;

import reso.common.AbstractTimer;
import reso.ip.*;
import reso.scheduler.AbstractScheduler;

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
    private MyTimer timer;
    private double alpha, beta, rttEchantillon, rttEstime, rto,rtt;
    private AbstractScheduler scheduler;

    public SelectiveRepeatProtocol(IPHost host, float lostPacket, float lostAck) {
        this.numSeq = 0;
        controller = new CongestionControl();
        N = controller.initSize;
        this.host = host;
        this.lostAck = lostAck;
        this.lostPacket = lostPacket;
        scheduler=host.getNetwork().getScheduler();
        alpha = 0.125;
        beta = 0.25;
        rttEstime = 1;
    }

    @Override
    public void receive(IPInterfaceAdapter src, Datagram datagram) throws Exception {
        
    }

    public void send(IPAddress src, String data) throws Exception {


        if(!isInit && !initMessageSend){
            SelectiveRepeatMessage message = new SelectiveRepeatMessage( Integer.MIN_VALUE,INIT_REQUEST);
            host.getIPLayer().send(IPAddress.ANY, src, IP_PROTO_SELECTIVEREPEAT, message);
        }
        SelectiveRepeatMessage message = new SelectiveRepeatMessage( numSeq,data);
        SelectiveRepeatPacket packet = new SelectiveRepeatPacket(message, src, -1);
        window.offer(packet);
        it = window.iterator();
        numSeq++;
        if (isInit)
            sendNext();
    }

    public void checkTimer(Queue<SelectiveRepeatPacket> window, boolean stop){
        if(window.size() == 0 || stop){
            return
        }
        else{
            for(SelectiveRepeatPacket packet: window){
                if(packet.timer
            }
        }
    }

    private class MyTimer extends AbstractTimer {
        public MyTimer(AbstractScheduler scheduler, double interval) {
            super(scheduler, interval, false);
        }

        protected void run() throws Exception {
            System.out.println(scheduler.getCurrentTime()+": ---- TIMEOUT");
            N=controller.isALoss(true);// change the size of the window because there is a timeout
            NChanged(true);
            next_seq_num = sendBase;
            it=window.iterator();
            sendNext();

        }
    }
}
