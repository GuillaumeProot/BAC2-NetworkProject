package reso.examples.selectiverepeat;

import reso.common.AbstractApplication;
import reso.ip.IPHost;
import reso.ip.IPLayer;

public class ApplicationReceiver extends AbstractApplication {

    private float loss;

    public ApplicationReceiver(IPHost host,float loss){
        super(host, "selectivrepeatReceiver");
        this.loss = loss;

    }

    @Override
    public void start() {
        SelectiveRepeatProtocol protocol = new SelectiveRepeatProtocol((IPHost) host);
        ((IPHost) host).getIPLayer().addListener(SelectiveRepeatProtocol.IP_SELECTIVEREPEAT_PROTO, protocol);

    }

    @Override
    public void stop() {

    }
}
