package reso.examples.selectiverepeat;

import reso.common.AbstractApplication;
import reso.ip.IPAddress;
import reso.ip.IPHost;
import reso.ip.IPLayer;

public class ApplicationSender extends AbstractApplication {


    private final IPAddress ipDestinary;
    private final IPLayer ip;
    private int packetQuantity;
    private float loss;
    /**
     * Creates a new application.
     *
     * @param host is the host where the application is running.
     */
    public ApplicationSender(IPHost host, IPAddress dst, int packetQuantity, float loss) {
        super(host, "selectiverepeatSender");
        this.ipDestinary = dst;
        this.ip = host.getIPLayer();
        this.packetQuantity = packetQuantity;
        this.loss = loss;

    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void stop() {

    }
}
