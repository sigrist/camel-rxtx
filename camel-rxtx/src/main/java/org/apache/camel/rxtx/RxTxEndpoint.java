package org.apache.camel.rxtx;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

import java.util.Enumeration;

import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.api.management.ManagedAttribute;
import org.apache.camel.api.management.ManagedResource;
import org.apache.camel.impl.DefaultEndpoint;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.UriParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a RxTx endpoint.
 */
@ManagedResource(description = "Managed RxTx endpoint")
@UriEndpoint(scheme = "rxtx", consumerClass = RxTxConsumer.class)
public class RxTxEndpoint extends DefaultEndpoint {

	private static Logger log = LoggerFactory.getLogger(RxTxEndpoint.class);

	@UriParam
	private String port;

	@UriParam
	private int baudRate;

	@UriParam
	private int dataBits;

	@UriParam
	private int stopBits;

	@UriParam
	private int parity;

	@UriParam
	private int receiveTimeout;

	@UriParam
	private boolean failOnError;

	private SerialPort serialPort;

	public RxTxEndpoint() {
		this.failOnError = false;
	}

	public RxTxEndpoint(String uri, RxTxComponent component) {
		super(uri, component);
	}

	public Producer createProducer() throws Exception {
		return new RxTxProducer(this);
	}

	public Consumer createConsumer(Processor processor) throws Exception {
		return new RxTxConsumer(this, processor);
	}

	public boolean isSingleton() {
		return true;
	}

	@ManagedAttribute(description = "Serial Port device")
	public void setPort(String port) {
		this.port = port;
	}

	@ManagedAttribute(description = "Serial Port device")
	public String getPort() {
		return port;
	}

	@ManagedAttribute(description = "Data bits")
	public int getDataBits() {
		return dataBits;
	}

	@ManagedAttribute(description = "Data bits")
	public void setDataBits(int dataBits) {
		this.dataBits = dataBits;
	}

	@ManagedAttribute(description = "Stop bits")
	public int getStopBits() {
		return stopBits;
	}

	@ManagedAttribute(description = "Stop bits")
	public void setStopBits(int stopBits) {
		this.stopBits = stopBits;
	}

	@ManagedAttribute(description = "Parity")
	public int getParity() {
		return parity;
	}

	@ManagedAttribute(description = "Parity")
	public void setParity(int parity) {
		this.parity = parity;
	}

	@ManagedAttribute(description = "Receive timeout")
	public int getReceiveTimeout() {
		return receiveTimeout;
	}

	@ManagedAttribute(description = "Receive timeout")
	public void setReceiveTimeout(int receiveTimeout) {
		this.receiveTimeout = receiveTimeout;
	}

	@ManagedAttribute(description = "Baud rate")
	public void setBaudRate(int baudRate) {
		this.baudRate = baudRate;
	}

	@ManagedAttribute(description = "Baud rate")
	public int getBaudRate() {
		return baudRate;
	}

	public void setFailOnError(boolean failOnError) {
		this.failOnError = failOnError;
	}

	@ManagedAttribute(description = "Fail the consumer/producer when there is an error")
	public boolean isFailOnError() {
		return failOnError;
	}

	protected SerialPort getPort(String port, int timeout) throws Exception {
		// Trying to open the port
		log.debug("[getPort] Getting port '" + port + "' with timeout '"
				+ timeout + "'");

		if (this.serialPort == null) {
			log.debug("[getPort] Current serial port is null, try to open");

			// Get the list of ports
			Enumeration<?> portList = CommPortIdentifier.getPortIdentifiers();

			log.debug("[getPort] Port List, has mode elements: "
					+ portList.hasMoreElements());

			while (portList.hasMoreElements()) {
				CommPortIdentifier portId = (CommPortIdentifier) portList
						.nextElement();
				log.trace("[getPort] Checking portId: " + portId.getName());
				// Check if the port is a serial port
				if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
					log.trace("[getPort] \tIs serial port: "
							+ portId.getPortType());
					if (portId.getName().equals(port)) {
						try {
							log.trace("[getPort] Open the port");
							serialPort = (SerialPort) portId.open(
									"TESTE_SERIAL", timeout);
							log.trace("[getPort] Port is open");
							
							// Break the while loop
							break;
						} catch (Exception e) {

							if (this.isFailOnError()) {
								log.trace("[getPort] Open the port");
								throw e;
							} else {
								log.error(
										"[getPort] Fail trying to open the serial port",
										e);
							}

						}
					}
				}
			}

			if (serialPort != null) {
				serialPort.setSerialPortParams(this.getBaudRate(),
						this.getDataBits(), this.getStopBits(),
						this.getParity());
			}
		}

		return serialPort;
	}

}
