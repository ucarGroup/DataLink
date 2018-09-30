package com.ucar.datalink.worker.core.runtime.rest;

import com.alibaba.fastjson.support.jaxrs.FastJsonProvider;
import com.ucar.datalink.common.errors.DatalinkException;
import com.ucar.datalink.worker.core.runtime.Keeper;
import com.ucar.datalink.worker.core.runtime.WorkerConfig;
import com.ucar.datalink.worker.core.runtime.rest.errors.RestExceptionMapper;
import com.ucar.datalink.worker.core.runtime.rest.resources.*;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.RequestLogHandler;
import org.eclipse.jetty.server.handler.StatisticsHandler;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.DispatcherType;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

/**
 * Embedded server for the REST API that provides the control plane for Datalink workers.
 */
public class RestServer {
    private static final Logger log = LoggerFactory.getLogger(RestServer.class);

    private static final long GRACEFUL_SHUTDOWN_TIMEOUT_MS = 60 * 1000;

    private final WorkerConfig config;

    private Server jettyServer;

    /**
     * Create a REST server for this keeper using the specified configs.
     */
    public RestServer(WorkerConfig config) {
        this.config = config;

        // To make the advertised port available immediately, we need to do some configuration here
        String hostname = config.getString(WorkerConfig.REST_HOST_NAME_CONFIG);
        Integer port = config.getInt(WorkerConfig.REST_PORT_CONFIG);

        jettyServer = new Server();
        ServerConnector connector = new ServerConnector(jettyServer);
        if (hostname != null && !hostname.isEmpty()) {
            connector.setHost(hostname);
        }
        connector.setPort(port);
        jettyServer.setConnectors(new Connector[]{connector});
    }

    public void start(Keeper keeper) {
        log.info("Starting REST server");

        ResourceConfig resourceConfig = new ResourceConfig();
        resourceConfig.register(new FastJsonProvider());
        resourceConfig.register(RootResource.class);
        resourceConfig.register(new TasksResource(keeper));
        resourceConfig.register(RestExceptionMapper.class);
        resourceConfig.register(FlushResource.class);
        resourceConfig.register(HBaseMetaResource.class);
        resourceConfig.register(WorkerResource.class);

        ServletContainer servletContainer = new ServletContainer(resourceConfig);
        ServletHolder servletHolder = new ServletHolder(servletContainer);

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        context.addServlet(servletHolder, "/*");

        String allowedOrigins = config.getString(WorkerConfig.ACCESS_CONTROL_ALLOW_ORIGIN_CONFIG);
        if (allowedOrigins != null && !allowedOrigins.trim().isEmpty()) {
            FilterHolder filterHolder = new FilterHolder(new CrossOriginFilter());
            filterHolder.setName("cross-origin");
            filterHolder.setInitParameter(CrossOriginFilter.ALLOWED_ORIGINS_PARAM, allowedOrigins);
            String allowedMethods = config.getString(WorkerConfig.ACCESS_CONTROL_ALLOW_METHODS_CONFIG);
            if (allowedMethods != null && !allowedOrigins.trim().isEmpty()) {
                filterHolder.setInitParameter(CrossOriginFilter.ALLOWED_METHODS_PARAM, allowedMethods);
            }
            context.addFilter(filterHolder, "/*", EnumSet.of(DispatcherType.REQUEST));
        }

        RequestLogHandler requestLogHandler = new RequestLogHandler();
        Slf4jRequestLog requestLog = new Slf4jRequestLog();
        requestLog.setLoggerName(RestServer.class.getCanonicalName());
        requestLog.setLogLatency(true);
        requestLogHandler.setRequestLog(requestLog);

        HandlerCollection handlers = new HandlerCollection();
        handlers.setHandlers(new Handler[]{context, new DefaultHandler(), requestLogHandler});

        /* Needed for graceful shutdown as per `setStopTimeout` documentation */
        StatisticsHandler statsHandler = new StatisticsHandler();
        statsHandler.setHandler(handlers);
        jettyServer.setHandler(statsHandler);
        jettyServer.setStopTimeout(GRACEFUL_SHUTDOWN_TIMEOUT_MS);
        jettyServer.setStopAtShutdown(true);

        try {
            jettyServer.start();
        } catch (Exception e) {
            throw new DatalinkException("Unable to start REST server", e);
        }

        log.info("REST server listening at " + jettyServer.getURI() + ", advertising URL " + advertisedUrl());
    }

    public void stop() {
        log.info("Stopping REST server");

        try {
            jettyServer.stop();
            jettyServer.join();
        } catch (Exception e) {
            throw new DatalinkException("Unable to stop REST server", e);
        } finally {
            jettyServer.destroy();
        }

        log.info("REST server stopped");
    }

    /**
     * Get the URL to advertise to other workers and clients. This uses the default connector parseFrom the embedded Jetty
     * server, unless overrides for advertised hostname and/or port are provided via configs.
     */
    public URI advertisedUrl() {
        UriBuilder builder = UriBuilder.fromUri(jettyServer.getURI());
        String advertisedHostname = config.getString(WorkerConfig.REST_ADVERTISED_HOST_NAME_CONFIG);
        if (advertisedHostname != null && !advertisedHostname.isEmpty())
            builder.host(advertisedHostname);
        Integer advertisedPort = config.getInt(WorkerConfig.REST_ADVERTISED_PORT_CONFIG);
        if (advertisedPort != null)
            builder.port(advertisedPort);
        else
            builder.port(config.getInt(WorkerConfig.REST_PORT_CONFIG));
        return builder.build();
    }

    public static class HttpResponse<T> {
        private int status;
        private Map<String, List<String>> headers;
        private T body;

        public HttpResponse(int status, Map<String, List<String>> headers, T body) {
            this.status = status;
            this.headers = headers;
            this.body = body;
        }

        public int status() {
            return status;
        }

        public Map<String, List<String>> headers() {
            return headers;
        }

        public T body() {
            return body;
        }
    }

    public static String urlJoin(String base, String path) {
        if (base.endsWith("/") && path.startsWith("/"))
            return base + path.substring(1);
        else
            return base + path;
    }

}