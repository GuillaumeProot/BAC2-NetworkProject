package reso.examples.selectiverepeat;

import reso.common.Message;

public class SelectiveRepeatMessage implements Message {

    public final int num;

    public SelectiveRepeatMessage(int num){
        this.num = num;
    }

    public String toString() {
        return "SelectiveRepeat [num=" + num + "]";
    }

    public int getByteLength() {
        // The ping-pong message carries a single 'int'
        return Integer.SIZE / 8;
    }
}
