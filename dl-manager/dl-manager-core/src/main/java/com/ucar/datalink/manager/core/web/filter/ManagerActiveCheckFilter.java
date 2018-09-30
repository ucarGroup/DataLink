package com.ucar.datalink.manager.core.web.filter;

import com.ucar.datalink.manager.core.server.ServerContainer;
import com.ucar.datalink.manager.core.server.ServerStatusMonitor;
import com.ucar.datalink.manager.core.web.servlet.ManagerProxyServlet;

import javax.servlet.*;
import java.io.IOException;
import java.util.Enumeration;

/**
 * Created by lubiao on 2017/4/16.
 */
public class ManagerActiveCheckFilter implements Filter {

    private FilterConfig filterConfig;
    private ManagerProxyServlet proxyServlet;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;
        this.proxyServlet = new ManagerProxyServlet();
        this.proxyServlet.init(new ServletConfig() {
            @Override
            public String getServletName() {
                return ManagerProxyServlet.class.getSimpleName();
            }

            @Override
            public ServletContext getServletContext() {
                return ManagerActiveCheckFilter.this.filterConfig.getServletContext();
            }

            @Override
            public String getInitParameter(String name) {
                return ManagerActiveCheckFilter.this.filterConfig.getInitParameter(name);
            }

            @Override
            public Enumeration<String> getInitParameterNames() {
                return ManagerActiveCheckFilter.this.filterConfig.getInitParameterNames();
            }
        });
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        ServerStatusMonitor monitor = ServerContainer.getInstance().getServerStatusMonitor();
        if (monitor.activeIsMine()) {
            filterChain.doFilter(servletRequest, servletResponse);
        } else {
            proxyServlet.service(servletRequest, servletResponse);
        }
    }

    @Override
    public void destroy() {
        this.proxyServlet.destroy();
    }
}
