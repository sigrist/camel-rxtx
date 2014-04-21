package org.apache.camel.rxtx;

import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The RxTx producer.
 */
public class RxTxProducer extends DefaultProducer {
    private static final Logger LOG = LoggerFactory.getLogger(RxTxProducer.class);
    private RxTxEndpoint endpoint;

    public RxTxProducer(RxTxEndpoint endpoint) {
        super(endpoint);
        this.endpoint = endpoint;
    }

    public void process(Exchange exchange) throws Exception {
        System.out.println(exchange.getIn().getBody());    
    }

}
