package reso.examples.selectiverepeat;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Queue;
import java.util.Random;

import reso.common.AbstractTimer;
import reso.ip.*;
import reso.scheduler.AbstractScheduler;

public class SelectiveRepeatProtocol implements IPInterfaceListener {
    private ArrayList queue;
    private int exp_seq_num,next_seq_num, sendBase ;
    private ArrayList<SelectiveRepeatPacket> window;
    private ArrayList<SelectiveRepeatMessage> cacheSender;
    private ArrayList<SelectiveRepeatMessage> cacheReceiver;
    private Iterator<SelectiveRepeatPacket> it;
    private int wantedNum;
    private boolean isInit = false;
    private boolean initMessageSend = false;
    public final static String INIT_REQUEST = "init request",INIT_REPONSE="init_response";
    public static final int IP_PROTO_SELECTIVEREPEAT = Datagram.allocateProtocolNumber("SELECTIVE_REPEAT");
    private int numSeq;
    private CongestionControl controller;
    private int N;
    private float lostAck, lostPacket;
    private final IPHost host;
    private MyTimer timer;
    private double alpha, beta, rttEchantillon, rttEstime, rto,rtt;
    private AbstractScheduler scheduler;
    private Random random;
    private double init_time;

    public SelectiveRepeatProtocol(IPHost host, float lostPacket, float lostAck) {
        controller = new CongestionControl();
        N = controller.initSize;
        this.host = host;
        this.lostAck = lostAck;
        this.lostPacket = lostPacket;
        scheduler=host.getNetwork().getScheduler();
        alpha = 0.125;
        beta = 0.25;
        rttEstime = 1; // srtt
        rttEchantillon = rttEstime / 2;
        rto = 3;
        it=window.iterator();
        exp_seq_num=-1;
        numSeq=0;
        next_seq_num=0;
        sendBase=0;
        random=new Random();
    }

    @Override
    public void receive(IPInterfaceAdapter src, Datagram datagram) throws Exception {
        SelectiveRepeatMessage selectiveRepeatMessage = (SelectiveRepeatMessage) datagram.getPayload();
        double receptionTime = host.getNetwork().getScheduler().getCurrentTime();
        if(selectiveRepeatMessage.payload.equals(INIT_REQUEST) && selectiveRepeatMessage.num == Integer.MIN_VALUE){
            host.getIPLayer().send(IPAddress.ANY,datagram.src,IP_PROTO_SELECTIVEREPEAT, new SelectiveRepeatMessage(Integer.MIN_VALUE,INIT_REPONSE));
        }

        else if(selectiveRepeatMessage.payload.equals(INIT_REPONSE) && selectiveRepeatMessage.num == Integer.MIN_VALUE){
            isInit = true;
            sendNext();
            rttEstime = receptionTime - init_time;
        }
        else if(selectiveRepeatMessage.payload.equals("ACK")){

            rtt = receptionTime - window.get(sendBase).time;
            try{
                N = controller.receiveAck(selectiveRepeatMessage.num);
            }catch (Exception e){
                System.exit(-1);
            }
            if(controller.isThreeAck()){
                next_seq_num = sendBase;
                it = window.iterator();
                sendNext();
            }
            if(selectiveRepeatMessage.num >= sendBase && selectiveRepeatMessage.num <= sendBase){
                cacheSender.add(selectiveRepeatMessage);
                for(int i = 0; i < cacheSender.size(); i++) { // boucle car premier elem incremente
                    if (cacheSender.contains(window.get(sendBase).message)) {
                        sendBase += 1;
                    }
                }

            }
            double newSrtt = (1 - alpha)* rttEstime + alpha * rtt;
            double newRttVar = (1 - beta) * rttEchantillon + beta*Math.abs(newSrtt-rtt);
            rttEstime = newSrtt;
            rttEchantillon = newRttVar;
            rto = rttEstime + 4 * rttEchantillon;
            // TODO envoi suivant avec timer new rto

        }
        else{

            if(selectiveRepeatMessage.num >= sendBase && selectiveRepeatMessage.num <= sendBase) {
                cacheReceiver.add(selectiveRepeatMessage);
                for(int i = 0; i < cacheReceiver.size(); i++) {
                    if (cacheReceiver.contains(window.get(sendBase).message)) {
                        sendBase += 1;
                    }
                }
            }
            if (selectiveRepeatMessage.num == exp_seq_num +1){
                exp_seq_num++;
            }
            SelectiveRepeatMessage ack = new SelectiveRepeatMessage(exp_seq_num);
            float r = random.nextFloat();

            if (r > lostAck){
                host.getIPLayer().send(IPAddress.ANY, datagram.src, IP_PROTO_SELECTIVEREPEAT, ack);
            }

        }


    }



    private void sendNext() throws Exception{
        if(it.hasNext() && next_seq_num < sendBase + N){
            SelectiveRepeatPacket packet = it.next();
            float r = random.nextFloat();
            if (r > lostPacket){
                packet.time = scheduler.getCurrentTime();
                host.getIPLayer().send(IPAddress.ANY, packet.address,IP_PROTO_SELECTIVEREPEAT,packet.message);
                MyTimer timer = new MyTimer(scheduler, rto, packet.message.num);
            }
            else{
                // TODO paquet perdu (print)
            }
            next_seq_num ++;
            sendNext();
        }
    }



    public void send(IPAddress src, String data) throws Exception {


        if(!isInit && !initMessageSend){
            SelectiveRepeatMessage message = new SelectiveRepeatMessage( Integer.MIN_VALUE,INIT_REQUEST);
            host.getIPLayer().send(IPAddress.ANY, src, IP_PROTO_SELECTIVEREPEAT, message);
            init_time = scheduler.getCurrentTime();
        }
        SelectiveRepeatMessage message = new SelectiveRepeatMessage( numSeq,data);
        SelectiveRepeatPacket packet = new SelectiveRepeatPacket(message, src, -1);
        window.add(packet);
        it = window.iterator();
        numSeq++;
        if (isInit)
            sendNext();
    }



    private class MyTimer extends AbstractTimer {
        private final int numTimer;

        public MyTimer(AbstractScheduler scheduler, double interval, int numTimer) {
            super(scheduler, interval, false);
            this.numTimer = numTimer;
        }

        protected void run() throws Exception {
            System.out.println(scheduler.getCurrentTime()+": ---- TIMEOUT");
            N=controller.isALoss(true);// change the size of the window because there is a timeout
            //NChanged(true);
            next_seq_num = sendBase;
            it=window.iterator();
            host.getIPLayer().send(IPAddress.ANY, window.get(numTimer).address,IP_PROTO_SELECTIVEREPEAT,window.get(numTimer).message);
            MyTimer timer = new MyTimer(scheduler, rto, numTimer);
        }
    }
}
