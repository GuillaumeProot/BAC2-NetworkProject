package reso.examples.selectiverepeat;

import reso.ip.Datagram;
import reso.ip.IPHost;

public class SelectiveRepeatProtocol {

    public static final int IP_PROTO_SELECTIVEREPEAT = Datagram.allocateProtocolNumber("SELECTIVE_REPEAT");

    public SelectiveRepeatProtocol(IPHost host) {
    }
}
