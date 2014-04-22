package org.apache.camel.rxtx;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;

public class RxTxComponentTest extends CamelTestSupport {

    @Test
    public void testRxTx() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMinimumMessageCount(1);       
        
        assertMockEndpointsSatisfied();
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() {
            	String url = "rxtx://ttyO1?port=/dev/ttyO1&dataBits=8&stopBits=1&parity=0"; 
            	
            	// Send data to serial port
            	from("direct:to_serial").to("url");
            	
            	// From serial port, just print
                from(url)
                  .process(new Processor() {
					
					@Override
					public void process(Exchange exchange) throws Exception {
						System.out.println("Processando....");
						Object body = exchange.getIn().getBody();
						
						System.out.println(body);
						
						if (body instanceof byte[]) {
							byte[] bytes = (byte[]) body;
							
							String s = new String(bytes);
							
							System.out.println("String: "+s);
						}
						
					}
				})
                  .to("mock:result");
            }
        };
    }
}
