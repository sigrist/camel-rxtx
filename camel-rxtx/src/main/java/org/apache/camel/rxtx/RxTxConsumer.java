package org.apache.camel.rxtx;

import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.impl.DefaultConsumer;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The RxTx consumer.
 */
public class RxTxConsumer extends DefaultConsumer implements
		SerialPortEventListener {
	private final RxTxEndpoint endpoint;
	private static Logger log = LoggerFactory.getLogger(RxTxConsumer.class);

	private InputStream inputStream;

	public RxTxConsumer(RxTxEndpoint endpoint, Processor processor) {
		super(endpoint, processor);
		this.endpoint = endpoint;
	}

	@Override
	protected void doStart() throws Exception {
		super.doStart();
		log.trace("[doStart] Initializing RxRxConsumer for '"+endpoint.getPort()+"'");
		SerialPort serialPort = endpoint.getPort(endpoint.getPort(),
				endpoint.getReceiveTimeout());

		log.trace("[doStart] Serial port is open, registering the event listener");
		serialPort.addEventListener(this);
		serialPort.notifyOnDataAvailable(true);
		serialPort.enableReceiveTimeout(1000);

		log.trace("[doStart] Getting the input stream");
		this.inputStream = serialPort.getInputStream();
		log.trace("[doStart] \tInput stream: "+this.inputStream);
	}

	@Override
	protected void doStop() throws Exception {
		super.doStop();
		if (this.inputStream != null) {
			inputStream.close();
		}
	}

	@Override
	public void serialEvent(SerialPortEvent event) {
		log.trace("[serialEvent] Data received on serial port");

		int eType = event.getEventType();
		log.trace("[serialEvent] \tEvent type: "+eType);

		switch (eType) {
		case SerialPortEvent.DATA_AVAILABLE:
			try {
				log.trace("[serialEvent] \tData available: "+(inputStream.available())+" bytes");
				ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
				log.trace("[serialEvent] \tCopying the stream data");
				{
					byte[] bufferLeitura = new byte[255];
					int nodeBytes = 0;
					while (inputStream.available() > 0) {
						nodeBytes = inputStream.read(bufferLeitura);
					}
					log.trace("[serialEvent] \tRead '"+nodeBytes+"' bytes");
					outputStream.write(bufferLeitura);
				}
				//IOUtils.copy(inputStream, outputStream);
				log.trace("[serialEvent] \tCopy ok");

				byte[] bytes = outputStream.toByteArray();
				outputStream.close();

				log.trace("[serialEvent] \tSend data to exchange");
				sendDataExchange(bytes);
				log.trace("[serialEvent] \tEvent finished");

			} catch (Throwable t) {
				log.trace("[serialEvent] \tError: "+t);
				getExceptionHandler().handleException("Error reading data", t);
			}

		default:
			log.trace("[serialEvent] \tDefault event");
			break;
		}

	}

	private void sendDataExchange(byte[] bytes) {
		Exchange exchange = endpoint.createExchange();

		// Set the bytes as body
		exchange.getIn().setBody(bytes);
		exchange.getIn().setHeader("port", endpoint.getPort());

		try {
			getProcessor().process(exchange);
		} catch (Exception e) {
			exchange.setException(e);
		}

		// handle any thrown exception
		if (exchange.getException() != null) {
			getExceptionHandler().handleException("Error processing exchange",
					exchange, exchange.getException());
		}

	}

}
