package com.ucar.datalink.manager.core.server;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jetty.server.AbstractNetworkConnector;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.xml.XmlConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by lubiao on 2017/1/11.
 *
 * Web container for datalink manager.
 */
public class JettyServer {
    private static final Logger logger = LoggerFactory.getLogger(JettyServer.class);
    private static final String DEFAULT_JETTY_CONFIG = "jetty.xml";
    private Server server;
    private ManagerConfig config;

    public JettyServer(ManagerConfig config) {
        this.config = config;
    }

    public void startup() throws Exception {
        Resource configXml = Resource.newSystemResource(DEFAULT_JETTY_CONFIG);
        XmlConfiguration configuration = new XmlConfiguration(configXml.getInputStream());
        server = (Server) configuration.configure();

        Integer port = config.getHttpPort();
        if (port != null && port > 0) {
            Connector[] connectors = server.getConnectors();
            for (Connector connector : connectors) {
                if (connector instanceof AbstractNetworkConnector) {
                    ((AbstractNetworkConnector) connector).setPort(port);
                }
            }
        }

        Handler handler = server.getHandler();
        if (handler != null && handler instanceof WebAppContext) {
            WebAppContext webAppContext = (WebAppContext) handler;
            String webAppPath = System.getProperty("webapp.conf");
            logger.info("Web App Path is " + webAppPath);

            if (StringUtils.isBlank(webAppPath)) {
                webAppContext.setResourceBase(JettyServer.class.getResource("/webapp").toString());
            } else {
                webAppContext.setResourceBase(webAppPath);
            }
        }
        server.start();
        if (logger.isInfoEnabled()) {
            logger.info("##Jetty Embed Server is started.");
        }
    }

    public void join() throws Exception {
        server.join();
        if (logger.isInfoEnabled()) {
            logger.info("##Jetty Embed Server joined!");
        }
    }

    public void shutdown() throws Exception {
        server.stop();
        if (logger.isInfoEnabled()) {
            logger.info("##Jetty Embed Server is shutdown.");
        }
    }
}
