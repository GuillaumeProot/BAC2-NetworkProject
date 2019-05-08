package reso.examples.selectiverepeat;

import reso.common.Message;

public class SelectiveRepeatMessage implements Message {

    public final int num;
    public final String payload;

    public SelectiveRepeatMessage(int num, String payload){
        this.payload = payload;
        this.num = num;
    }

    public SelectiveRepeatMessage(int num){
        this.payload = "ACK";
        this.num = num;
    }

    public String toString() {
        return "SelectiveRepeat [num=" + num +" with data: "+ payload+ "]";
    }

    public int getByteLength() {
        return payload.length()*2 + Integer.SIZE / 8;
    }
}
