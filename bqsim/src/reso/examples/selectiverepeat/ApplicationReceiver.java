package reso.examples.selectiverepeat;

import reso.common.AbstractApplication;
import reso.common.Host;

public class ApplicationReceiver extends AbstractApplication {

    private float loss;

    public ApplicationReceiver(Host host,float loss){
        super(host, "selectivrepeatReceiver");
        this.loss = loss;

    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }
}
