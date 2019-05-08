package reso.examples.selectiverepeat;

public class CongestionControl {

    final static int initSize = 1;
    private double size;
    private int ackCounter;
    private int lastExpectedReceived;
    private int sstresh;
    private boolean threeAckFlag;

    public CongestionControl(){
        ackCounter = 0;
        lastExpectedReceived = -1;
        this.size = initSize;
        this.sstresh = 10;
        threeAckFlag = false;
    }

    public boolean isThreeAck(){
        if(ackCounter == 3){
            System.out.println(": trois ack identique");
            ackCounter = 0;
            threeAckFlag = true;
            return true;
        }
        threeAckFlag = false;
        return false;
    }

    public int isALoss(boolean timeout){
        size = size / 2;
        this.sstresh = (int)size;
        if(sstresh < 1){
            sstresh = 1;
        }
        if(timeout){
            size =1;
        }
        int toReturn = (int)size;
        if (size < 0 || toReturn < 1){
            size = 1;
        }
        return (int)size;

    }

    public int receiveAck(int seqNumber) throws Exception{
        if(seqNumber == lastExpectedReceived){
            ackCounter ++;
            if (isThreeAck()){
                return isALoss(false);
            }
            int toReturn = (int) size;
            if(size < 1 || toReturn < 1){
                size = 1;
            }
            return (int)size;
        }
        else if(seqNumber > lastExpectedReceived){
            int difference = seqNumber - lastExpectedReceived;
            if(size < sstresh){
                size += difference;
            }
            else{
                size = size + (1/size);
            }
            lastExpectedReceived =seqNumber;
            ackCounter = 0;
            int toReturn = (int) size;
            if(size < 1 || toReturn <1){
                size = 1;
            }
            return (int) size;
        }
        else{
            throw new Exception("mauvaise sequence de nombre");
        }
    }
}
