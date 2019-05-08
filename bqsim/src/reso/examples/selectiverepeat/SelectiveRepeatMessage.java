package reso.examples.selectiverepeat;

import reso.common.Message;

public class SelectiveRepeatMessage implements Message {

    public final int num;
    public final String payload;
    public final int expected;

    public SelectiveRepeatMessage(int num, String payload, int expected){
        this.payload = payload;
        this.num = num;
        this.expected = expected;
    }

    public SelectiveRepeatMessage(int num, int expected){
        this.payload = "ACK";
        this.num = num;
        this.expected = expected;
    }

    public String toString() {
        return "SelectiveRepeat [num=" + num +" with data: "+ payload+ "]";
    }

    public int getByteLength() {
        return payload.length()*2 + Integer.SIZE / 8;
    }
}
