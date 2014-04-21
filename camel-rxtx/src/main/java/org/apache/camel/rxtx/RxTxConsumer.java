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

/**
 * The RxTx consumer.
 */
public class RxTxConsumer extends DefaultConsumer implements
		SerialPortEventListener {
	private final RxTxEndpoint endpoint;

	private InputStream inputStream;

	public RxTxConsumer(RxTxEndpoint endpoint, Processor processor) {
		super(endpoint, processor);
		this.endpoint = endpoint;
	}

	@Override
	protected void doStart() throws Exception {
		SerialPort serialPort = endpoint.getPort(endpoint.getPort(),
				endpoint.getReceiveTimeout());

		serialPort.addEventListener(this);

		this.inputStream = serialPort.getInputStream();
	}

	@Override
	protected void doStop() throws Exception {

		if (this.inputStream != null) {
			inputStream.close();
		}
	}

	@Override
	public void serialEvent(SerialPortEvent event) {
		log.trace("[serialEvent] Data received on serial port");

		int eType = event.getEventType();

		switch (eType) {
		case SerialPortEvent.DATA_AVAILABLE:
			try {
				ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
				IOUtils.copy(inputStream, outputStream);

				byte[] bytes = outputStream.toByteArray();
				outputStream.close();

				sendDataExchange(bytes);

			} catch (Throwable t) {
				getExceptionHandler().handleException("Error reading data", t);
			}

		default:
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
