package reso.examples.selectiverepeat;

import reso.common.AbstractApplication;
import reso.ip.IPAddress;
import reso.ip.IPHost;
import reso.ip.IPLayer;

public class ApplicationSender extends AbstractApplication {


    private final IPAddress ipDestinary;
    private final IPLayer ip; //source ip
    private int num;
    private float loss; //nbre de paquet qu on peut perdre

    /**
     * Creates a new application.
     *
     * @param host is the host where the application is running.
     */
    public ApplicationSender(IPHost host, IPAddress dst, int num, float loss) {
        super(host, "selectiverepeatSender");
        this.ipDestinary = dst;
        this.ip = host.getIPLayer();
        this.num= num;
        this.loss = loss; //number of packets we're going to lose

    }

    @Override
    public void start() throws Exception {
        //ip.addListener(SelectiveRepeatProtocol.IP_PROTO_SELECTIVEREPEAT, new SelectiveRepeatProtocol((IPHost) host,loss,0));
        //ip.send(IPAddress.ANY, ipDestinary, SelectiveRepeatProtocol.IP_PROTO_SELECTIVEREPEAT, new SelectiveRepeatMessage(num));
        SelectiveRepeatProtocol protocol = new SelectiveRepeatProtocol((IPHost)host,loss,0);
        ip.addListener(SelectiveRepeatProtocol.IP_PROTO_SELECTIVEREPEAT,protocol);
        for(int i = 0; i<= num; i++){
            protocol.send(ipDestinary,"Je suis le paquet" + i);
        }
    }

    @Override
    public void stop() {

    }
}
