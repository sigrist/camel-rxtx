package org.apache.camel.rxtx;

import java.util.Map;

import org.apache.camel.Endpoint;
import org.apache.camel.impl.DefaultComponent;

/**
 * Represents the component that manages {@link RxTxEndpoint}.
 */
public class RxTxComponent extends DefaultComponent {

    protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
        Endpoint endpoint = new RxTxEndpoint(uri, this);
        setProperties(endpoint, parameters);
        return endpoint;
    }
}
