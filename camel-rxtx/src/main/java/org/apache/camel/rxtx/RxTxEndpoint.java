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

	private SerialPort serialPort;

	public RxTxEndpoint() {
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

	protected SerialPort getPort(String port, int timeout) throws Exception {
		// Trying to open the port
		log.debug("[getPort] Getting port '" + port + "' with timeout '"
				+ timeout + "'");

		if (this.serialPort == null) {

			// Get the list of ports
			Enumeration<?> portList = CommPortIdentifier.getPortIdentifiers();

			while (portList.hasMoreElements()) {
				CommPortIdentifier portId = (CommPortIdentifier) portList
						.nextElement();
				// Check if the port is a serial port
				if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
					if (portId.getName().equals(port)) {
						try {
							serialPort = (SerialPort) portId.open(
									"TESTE_SERIAL", timeout);
						} catch (Exception e) {
							e.printStackTrace();
							// TODO Erro ao abrir a porta serial.
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