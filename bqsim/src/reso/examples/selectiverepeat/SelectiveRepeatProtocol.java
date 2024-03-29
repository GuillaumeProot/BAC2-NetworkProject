package reso.examples.selectiverepeat;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Queue;
import java.util.Random;

import reso.common.AbstractTimer;
import reso.ip.*;
import reso.scheduler.AbstractScheduler;

public class SelectiveRepeatProtocol implements IPInterfaceListener {
    private int exp_seq_num,next_seq_num, sendBase, numSeq ;
    private int N;
    private int expected_num_received =0, expected_num_sended =0;
    public static final int IP_PROTO_SELECTIVEREPEAT = Datagram.allocateProtocolNumber("SELECTIVE_REPEAT");
    private float lostAck, lostPacket;
    private double alpha, beta, rttEchantillon, rttEstime, rto,rtt;
    private double init_time;

    private ArrayList<SelectiveRepeatPacket> window;
    private ArrayList<SelectiveRepeatMessage> cacheSender = new ArrayList<>();
    private ArrayList<SelectiveRepeatMessage> cacheReceiver = new ArrayList<>();
    private ArrayList<MyTimer> timer_list = new ArrayList<>();
    private Iterator<SelectiveRepeatPacket> it;

    private boolean isInit = false;
    private boolean initMessageSend = false;

    public final static String INIT_REQUEST = "init request",INIT_REPONSE="init_response";

    private CongestionControl controller;
    private final IPHost host;
    private AbstractScheduler scheduler;
    private Random random;
    private FileWriter f;

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
        window = new ArrayList<>(N);
        it=window.iterator();
        exp_seq_num=-1;
        numSeq=0;
        next_seq_num=0;
        sendBase=0;
        random=new Random();



        try {
            f=new FileWriter("data.txt",false);
            NChanged(false);
        } catch (IOException e) {
            System.out.println(scheduler.getCurrentTime()+": ERROR: "+"chargement de fichier impossible");
        }
    }

    @Override
    public void receive(IPInterfaceAdapter src, Datagram datagram) throws Exception {
        System.out.println(scheduler.getCurrentTime() + ":reception de donnee");
        SelectiveRepeatMessage selectiveRepeatMessage = (SelectiveRepeatMessage) datagram.getPayload();
        System.out.println(scheduler.getCurrentTime()+": num de sequence: "+selectiveRepeatMessage.num+" , data: "+selectiveRepeatMessage.payload);
        double receptionTime = host.getNetwork().getScheduler().getCurrentTime();

        if(selectiveRepeatMessage.payload.equals(INIT_REQUEST) && selectiveRepeatMessage.num == Integer.MIN_VALUE){
            System.out.println(scheduler.getCurrentTime()+":    type: "+"init request");
            System.out.println(scheduler.getCurrentTime()+": --> SEND: "+"init response");
            host.getIPLayer().send(IPAddress.ANY,datagram.src,IP_PROTO_SELECTIVEREPEAT, new SelectiveRepeatMessage(Integer.MIN_VALUE,INIT_REPONSE));
        }

        else if(selectiveRepeatMessage.payload.equals(INIT_REPONSE) && selectiveRepeatMessage.num == Integer.MIN_VALUE){
            System.out.println(scheduler.getCurrentTime()+":    type: "+"init response");
            isInit = true;
            sendNext();
            rttEstime = receptionTime - init_time;
        }
        else if(selectiveRepeatMessage.payload.equals("ACK")){
            System.out.println(scheduler.getCurrentTime()+":    type: "+"ACK");
            rtt = receptionTime - window.get(sendBase).time;
            try{
                N = controller.receiveAck(selectiveRepeatMessage.num);
            }catch (Exception e){
                System.out.println(scheduler.getCurrentTime()+": ERROR: "+" ACK sequence number error");
                System.exit(-1);
            }
            if(controller.isThreeAck()){
                System.out.println("is3ack");
                next_seq_num = sendBase;
                it = window.iterator();
                sendNext();
            }
            if(selectiveRepeatMessage.num >= sendBase && selectiveRepeatMessage.num <= sendBase){

                cacheSender.add(selectiveRepeatMessage);
                for(int i = 0; i < cacheSender.size(); i++) { // boucle car premier elem incremente
                    for(SelectiveRepeatMessage message:cacheSender){
                        if(message.num == expected_num_sended){
                            expected_num_sended ++;
                            sendBase ++;
                            System.out.println("message.num: " + message.num);
                            timer_list.get(message.num).stop();
                            System.out.println("timer :"+ message.num);
                        }
                    }

                }

            }
            double newSrtt = (1 - alpha)* rttEstime + alpha * rtt;
            double newRttVar = (1 - beta) * rttEchantillon + beta*Math.abs(newSrtt-rtt);
            rttEstime = newSrtt;
            rttEchantillon = newRttVar;
            rto = rttEstime + 4 * rttEchantillon;
            sendNext();

            NChanged(true);
        }
        else{

            if(selectiveRepeatMessage.num >= sendBase && selectiveRepeatMessage.num <= sendBase) {
                System.out.println(scheduler.getCurrentTime()+":    type: "+"message");
                cacheReceiver.add(selectiveRepeatMessage);
                for(int i = 0; i < cacheReceiver.size(); i++) {
                    for (SelectiveRepeatMessage message: cacheReceiver){
                        if(message.num == expected_num_received){
                            sendBase +=1;
                            expected_num_received +=1;
                        }
                    }
                }
            }
            if (selectiveRepeatMessage.num == exp_seq_num +1){
                exp_seq_num++;
            }
            SelectiveRepeatMessage ack = new SelectiveRepeatMessage(selectiveRepeatMessage.num);
            float r = random.nextFloat();

            if (r > lostAck){
                host.getIPLayer().send(IPAddress.ANY, datagram.src, IP_PROTO_SELECTIVEREPEAT, ack);
            }
            System.out.println("--------------------------------------- \n");

        }


    }



    private void sendNext() throws Exception{
        if(it.hasNext() && next_seq_num < sendBase + N){


            SelectiveRepeatPacket packet = it.next();
            float r = random.nextFloat();
            if (r > lostPacket){
                packet.time = scheduler.getCurrentTime();
                System.out.println(scheduler.getCurrentTime() + ": --> send: message: "+packet.message.num);
                host.getIPLayer().send(IPAddress.ANY, packet.address,IP_PROTO_SELECTIVEREPEAT,packet.message);

                next_seq_num ++;

            }
            else{
                System.out.println(scheduler.getCurrentTime()+": paquet perdu");
                packet = it.next();

            }
            MyTimer timer = new MyTimer(scheduler, rto, packet.message.num);
            timer_list.add(timer);
            timer.start();
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


    private void NChanged(boolean close){

        try {
            f.write(scheduler.getCurrentTime()+" "+N+"\n");
            System.out.println(scheduler.getCurrentTime() + " : taille de la fenetre: "+ N);
            if(window.isEmpty() && close){
                f.close();
            }

        } catch (IOException e) {
            System.out.println(scheduler.getCurrentTime()+": ERROR: "+"ecriture impossible dans le fichier");
        }
    }



    private class MyTimer extends AbstractTimer {
        private final int numTimer;

        public MyTimer(AbstractScheduler scheduler, double interval, int numTimer) {
            super(scheduler, interval, false);
            this.numTimer = numTimer;
        }

        protected void run() throws Exception {
            System.out.println(scheduler.getCurrentTime()+": ---- TIMEOUT "+ numTimer );
            N=controller.isALoss(true);// change the size of the window because there is a timeout
            NChanged(true);
            host.getIPLayer().send(IPAddress.ANY, window.get(numTimer).address,IP_PROTO_SELECTIVEREPEAT,window.get(numTimer).message);
            start();

        }

        public void stop(){
            super.stop();
        }
    }
}
