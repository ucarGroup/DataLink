package com.ucar.datalink.flinker.core.admin.rest;


import com.ucar.datalink.common.zookeeper.ZkClientX;
import com.ucar.datalink.flinker.core.admin.AdminConstants;
import com.ucar.datalink.flinker.core.admin.JobRunningController;
import org.apache.commons.lang.StringUtils;
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
import java.util.Properties;

/**
 * Embedded server for the REST API that provides the control plane for Datalink workers.
 */
public class RestServer {
    private static final Logger log = LoggerFactory.getLogger(RestServer.class);

    private static final long GRACEFUL_SHUTDOWN_TIMEOUT_MS = 60 * 1000;

    /**
     * Hostname for the REST API. If this is set, it will only bind to this interface
     */
    public static final String REST_HOST_NAME_CONFIG = "rest.host.name";

    public static final int REST_PORT_DEFAULT = 8083;

    /**
     * Value to set the Access-Control-Allow-Origin header to for REST API requests." +
     "To enable cross origin access, set this to the domain of the application that should be permitted" +
     " to access the API, or '*' to allow access parseFrom any domain. The default value only allows access" +
     " parseFrom the domain of the REST API.";
     */
    public static final String ACCESS_CONTROL_ALLOW_ORIGIN_CONFIG = "access.control.allow.origin";
    protected static final String ACCESS_CONTROL_ALLOW_ORIGIN_DEFAULT = "";

    /**
     * Sets the methods supported for cross origin requests by setting the Access-Control-Allow-Methods header. "
     + "The default value of the Access-Control-Allow-Methods header allows cross origin requests for GET, POST and HEAD.";
     */
    public static final String ACCESS_CONTROL_ALLOW_METHODS_CONFIG = "access.control.allow.methods";
    protected static final String ACCESS_CONTROL_ALLOW_METHODS_DEFAULT = "";

    /**
     * If this is set, this is the hostname that will be given out to other workers to connect to.
     */
    public static final String REST_ADVERTISED_HOST_NAME_CONFIG = "rest.advertised.host.name";

    /**
     * If this is set, this is the port that will be given out to other workers to connect to.
     */
    public static final String REST_ADVERTISED_PORT_CONFIG = "rest.advertised.port";

    private Server jettyServer;

    private JobRunningController jobRunningController;

    private ZkClientX zkClient;

    /**
     * Create a REST server for this keeper using the specified configs.
     */
    public RestServer(JobRunningController jobRunningController, ZkClientX zkClient,Properties properties) {

        // To make the advertised port available immediately, we need to do some configuration here
        String hostname = System.getProperty(REST_HOST_NAME_CONFIG,"");
        String portConfig = properties.getProperty(AdminConstants.DATAX_REST_SERVER_PORT);
        Integer port;
        if(StringUtils.isNotBlank(portConfig)){
            port = Integer.parseInt(portConfig);
        }else {
            port = REST_PORT_DEFAULT;
        }

        jettyServer = new Server();
        ServerConnector connector = new ServerConnector(jettyServer);
        if (hostname != null && !hostname.isEmpty()) {
            connector.setHost(hostname);
        }
        connector.setPort(port);
        jettyServer.setConnectors(new Connector[]{connector});
        this.jobRunningController = jobRunningController;
        this.zkClient = zkClient;
        DataxResource.setJobRunningController(jobRunningController);
        DataxResource.setZkClient(zkClient);
    }

    public void start() {
            log.info("Starting REST server");
            ResourceConfig resourceConfig = new ResourceConfig();
            resourceConfig.register(DataxResource.class);

            ServletContainer servletContainer = new ServletContainer(resourceConfig);
            ServletHolder servletHolder = new ServletHolder(servletContainer);

            ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
            context.setContextPath("/");
            context.addServlet(servletHolder, "/*");

            String allowedOrigins = System.getProperty(ACCESS_CONTROL_ALLOW_ORIGIN_CONFIG);
            if (allowedOrigins != null && !allowedOrigins.trim().isEmpty()) {
                FilterHolder filterHolder = new FilterHolder(new CrossOriginFilter());
                filterHolder.setName("cross-origin");
            filterHolder.setInitParameter(CrossOriginFilter.ALLOWED_ORIGINS_PARAM, allowedOrigins);
            String allowedMethods = System.getProperty(ACCESS_CONTROL_ALLOW_METHODS_CONFIG);
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
            throw new RuntimeException("Unable to start REST server", e);
        }

        log.info("REST server listening at " + jettyServer.getURI() + ", advertising URL " + advertisedUrl());
    }

    public void stop() {
        log.info("Stopping REST server");

        try {
            jettyServer.stop();
            jettyServer.join();
        } catch (Exception e) {
            throw new RuntimeException("Unable to stop REST server", e);
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
        String advertisedHostname = System.getProperty(REST_ADVERTISED_HOST_NAME_CONFIG);
        if (advertisedHostname != null && !advertisedHostname.isEmpty())
            builder.host(advertisedHostname);
        Integer advertisedPort = Integer.getInteger(REST_ADVERTISED_PORT_CONFIG);
        if (advertisedPort != null) {
            builder.port(advertisedPort);
        }
        else {
            builder.port( REST_PORT_DEFAULT );
        }
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