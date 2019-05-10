package reso.examples.selectiverepeat;

import reso.common.AbstractTimer;
import reso.ip.IPAddress;

public class SelectiveRepeatPacket {
    public SelectiveRepeatMessage message;
    public IPAddress address;
    public double time;


    public SelectiveRepeatPacket(SelectiveRepeatMessage message, IPAddress address, double time){
        this.message = message;
        this.address = address;
        this.time = time;
    }

    public String toString() {
        return "packet with message "+message.toString()+" and address dest: "+address.toString();
    }


}
