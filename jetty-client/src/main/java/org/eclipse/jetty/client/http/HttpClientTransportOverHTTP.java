//
//  ========================================================================
//  Copyright (c) 1995-2019 Mort Bay Consulting Pty. Ltd.
//  ------------------------------------------------------------------------
//  All rights reserved. This program and the accompanying materials
//  are made available under the terms of the Eclipse Public License v1.0
//  and Apache License v2.0 which accompanies this distribution.
//
//      The Eclipse Public License is available at
//      http://www.eclipse.org/legal/epl-v10.html
//
//      The Apache License v2.0 is available at
//      http://www.opensource.org/licenses/apache2.0.php
//
//  You may elect to redistribute this code under either of these licenses.
//  ========================================================================
//

package org.eclipse.jetty.client.http;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.eclipse.jetty.client.AbstractConnectorHttpClientTransport;
import org.eclipse.jetty.client.DuplexConnectionPool;
import org.eclipse.jetty.client.DuplexHttpDestination;
import org.eclipse.jetty.client.HttpDestination;
import org.eclipse.jetty.client.HttpRequest;
import org.eclipse.jetty.client.Origin;
import org.eclipse.jetty.client.api.Connection;
import org.eclipse.jetty.io.ClientConnector;
import org.eclipse.jetty.io.EndPoint;
import org.eclipse.jetty.util.ProcessorUtils;
import org.eclipse.jetty.util.Promise;
import org.eclipse.jetty.util.annotation.ManagedObject;

@ManagedObject("The HTTP/1.1 client transport")
public class HttpClientTransportOverHTTP extends AbstractConnectorHttpClientTransport
{
    public static final HttpDestination.Protocol HTTP11 = new HttpDestination.Protocol(List.of("http/1.1"), false);

    public HttpClientTransportOverHTTP()
    {
        this(Math.max(1, ProcessorUtils.availableProcessors() / 2));
    }

    public HttpClientTransportOverHTTP(int selectors)
    {
        this(new ClientConnector());
        getClientConnector().setSelectors(selectors);
    }

    public HttpClientTransportOverHTTP(ClientConnector connector)
    {
        super(connector);
        setConnectionPoolFactory(destination -> new DuplexConnectionPool(destination, getHttpClient().getMaxConnectionsPerDestination(), destination));
    }

    @Override
    public HttpDestination.Key newDestinationKey(HttpRequest request, Origin origin)
    {
        return new HttpDestination.Key(origin, HTTP11);
    }

    @Override
    public HttpDestination newHttpDestination(HttpDestination.Key key)
    {
        return new DuplexHttpDestination(getHttpClient(), key);
    }

    @Override
    public org.eclipse.jetty.io.Connection newConnection(EndPoint endPoint, Map<String, Object> context) throws IOException
    {
        HttpDestination destination = (HttpDestination)context.get(HTTP_DESTINATION_CONTEXT_KEY);
        @SuppressWarnings("unchecked")
        Promise<Connection> promise = (Promise<Connection>)context.get(HTTP_CONNECTION_PROMISE_CONTEXT_KEY);
        org.eclipse.jetty.io.Connection connection = newHttpConnection(endPoint, destination, promise);
        if (LOG.isDebugEnabled())
            LOG.debug("Created {}", connection);
        return customize(connection, context);
    }

    protected HttpConnectionOverHTTP newHttpConnection(EndPoint endPoint, HttpDestination destination, Promise<Connection> promise)
    {
        return new HttpConnectionOverHTTP(endPoint, destination, promise);
    }
}
