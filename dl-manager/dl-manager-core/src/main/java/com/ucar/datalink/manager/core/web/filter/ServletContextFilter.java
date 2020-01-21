package com.ucar.datalink.manager.core.web.filter;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;




/**
 * 过滤用户请求
 * @author 
 *
 */
public class ServletContextFilter implements Filter {
	private static Logger log = LoggerFactory.getLogger(ServletContextFilter.class);
	
	public void destroy() {
		
		
	}

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
        
		HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

		String spath = req.getServletPath();
		String[] urls = {"/login","/json",".js",".css",".ico",".jpg",".png"};
		boolean flag = true;
		for (String str : urls) {
			if (spath.indexOf(str) != -1) {
				flag =false;
				break;
			}
		}
		if (flag) {
			if(!"/communication/putExceptionAndCache".equals(req.getRequestURI())) {
				log.info("请求的地址 [ key = "+req.getRequestURI()+" ] ");
			}
			Map paramsMap = req.getParameterMap();
			for(Object object:paramsMap.keySet()){
				String key = (String) object;
				String value = req.getParameter(key);
				if(key.indexOf("password") > 0 || ( !StringUtils.isEmpty(value) && (value.indexOf("password") > 0) )
						|| ( !StringUtils.isEmpty(value) && (value.indexOf("pwd") > 0) )) {
					continue;
				}
				log.info("请求的参数 [ key = "+key+" ] , [ value = "+value+" ]");
			}
		}

		chain.doFilter(request, response);
	}

	public void init(FilterConfig arg0) throws ServletException {
		
	}

}
