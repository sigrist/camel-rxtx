package org.apache.camel.rxtx;

import java.io.OutputStream;

import gnu.io.SerialPort;

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
	private OutputStream outputStream;

    public RxTxProducer(RxTxEndpoint endpoint) {
        super(endpoint);
        this.endpoint = endpoint;
    }
    
    @Override
    protected void doStart() throws Exception {
    	super.doStart();
    	log.trace("[doStart] Producer doStart");
		SerialPort serialPort = endpoint.getPort(endpoint.getPort(),
				endpoint.getReceiveTimeout());
		log.trace("[doStart] Serial open opened");
		this.outputStream = serialPort.getOutputStream();
		log.trace("[doStart] Outputstream ok: "+this.outputStream);
    }
    
	@Override
	protected void doStop() throws Exception {
		super.doStop();
		if (this.outputStream != null) {
			outputStream.close();
		}
	}
    

    public void process(Exchange exchange) throws Exception {
        Object body = exchange.getIn().getBody();
        
        log.trace("[process] Sendind data: "+body);
        if (body instanceof byte[]) {
        	byte[] bytes = (byte[]) body;
        	
        	outputStream.write(bytes);
        } else {
        	throw new IllegalArgumentException("Expected body as byte[]");
        }
    }

}
