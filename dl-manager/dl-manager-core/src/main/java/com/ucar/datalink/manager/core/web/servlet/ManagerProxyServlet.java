package com.ucar.datalink.manager.core.web.servlet;

import com.ucar.datalink.common.zookeeper.ManagerMetaData;
import com.ucar.datalink.manager.core.server.ServerContainer;
import com.ucar.datalink.manager.core.server.ServerStatusMonitor;
import org.eclipse.jetty.proxy.ProxyServlet;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;

/**
 * Created by lubiao on 2017/4/16.
 */
public class ManagerProxyServlet extends ProxyServlet {
    private static final String _prefix = "/";

    @Override
    protected String rewriteTarget(HttpServletRequest clientRequest) {
        ServerStatusMonitor monitor = ServerContainer.getInstance().getServerStatusMonitor();
        ManagerMetaData activeManager = monitor.getActiveManagerMetaData();

        if (activeManager != null) {
            return rewriteTarget(clientRequest, "http://" + activeManager.getAddress() + ":" + activeManager.getHttpPort());
        }
        return null;
    }

    protected String rewriteTarget(HttpServletRequest request, String _proxyTo) {
        String path = request.getRequestURI();
        if (!path.startsWith(_prefix))
            return null;

        StringBuilder uri = new StringBuilder(_proxyTo);
        if (_proxyTo.endsWith("/"))
            uri.setLength(uri.length() - 1);
        String rest = path.substring(_prefix.length());
        if (!rest.isEmpty()) {
            if (!rest.startsWith("/"))
                uri.append("/");
            uri.append(rest);
        }

        String query = request.getQueryString();
        if (query != null) {
            // Is there at least one path segment ?
            String separator = "://";
            if (uri.indexOf("/", uri.indexOf(separator) + separator.length()) < 0)
                uri.append("/");
            uri.append("?").append(query);
        }
        URI rewrittenURI = URI.create(uri.toString()).normalize();

        return rewrittenURI.toString();
    }
}
