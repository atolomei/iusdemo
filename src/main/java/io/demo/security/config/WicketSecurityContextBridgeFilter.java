package io.demo.security.config;
 

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import io.demo.Logger;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

/**
 * Bridge filter: if a Spring SecurityContext was stored in the HttpSession
 * (under SPRING_SECURITY_CONTEXT_KEY), restore it to SecurityContextHolder for the request.
 */
@Component
//@Order(org.springframework.boot.autoconfigure.security.SecurityProperties.BASIC_AUTH_ORDER - 10) // adjust ordering if necessary

// BASIC_AUTH_ORDER was removed in Spring Boot 4; equivalent value was Integer.MIN_VALUE + 5 (-2147483643)
@Order(Ordered.HIGHEST_PRECEDENCE + 15)
public class WicketSecurityContextBridgeFilter implements Filter {

	static private Logger logger = Logger.getLogger(WicketSecurityContextBridgeFilter.class.getName());
	
    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        if (req instanceof HttpServletRequest request) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                Object sc = session.getAttribute(
                        HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
                if (sc instanceof SecurityContext securityContext
                        && securityContext.getAuthentication() != null) {
                	if (session != null) {
                	    Object ssc = session.getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
                	    //logger.debug("BRIDGE: sessionId= " +session.getId() + "  , springContextPresent= "+(sc != null?"yes":"no"));
                	    
                	    if (ssc instanceof SecurityContext && securityContext.getAuthentication() != null) {
                	    	//logger.debug("BRIDGE: restoring auth: " + securityContext.getAuthentication().getName());
                	        SecurityContextHolder.setContext(securityContext);
                	    }
                	}
                    SecurityContextHolder.setContext(securityContext);
                }
            }
        }

        try {
            chain.doFilter(req, res);
        } finally {
            //SecurityContextHolder.clearContext();
        }
    }
}




/**
 * //@Order(Ordered.HIGHEST_PRECEDENCE + 10) // adjust ordering if necessary

public class WicketSecurityContextBridgeFilter implements Filter {


	static private Logger logger = Logger.getLogger(WicketSecurityContextBridgeFilter.class.getName());
 
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (request instanceof HttpServletRequest) {
            HttpServletRequest httpReq = (HttpServletRequest) request;
            HttpSession session = httpReq.getSession(false);

            if (session != null) {
                Object sc = session.getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
                if (sc instanceof SecurityContext) {
                    SecurityContext securityContext = (SecurityContext) sc;
                    if (securityContext.getAuthentication() != null) {
                        // place the authentication into the SecurityContextHolder
                        SecurityContextHolder.getContext().setAuthentication(securityContext.getAuthentication());
                    }
                }
            
                HttpSession s = httpReq.getSession(false);
                logger.debug("filter session id: " + (s != null ? s.getId() : "null"));
            
            }
        }

        try {
            chain.doFilter(request, response);
        } finally {
            // IMPORTANT: clear thread-local after request to avoid leaks
            SecurityContextHolder.clearContext();
        }
    }
    

   @Bean
    public FilterRegistrationBean<WicketSecurityContextBridgeFilter> wicketBridgeRegistration(WicketSecurityContextBridgeFilter filter) {
        FilterRegistrationBean<WicketSecurityContextBridgeFilter> reg = new FilterRegistrationBean<>(filter);
        // ensure it runs earlier than Spring Security's authorization filters
        reg.setOrder(org.springframework.boot.autoconfigure.security.SecurityProperties.BASIC_AUTH_ORDER - 10);
        return reg;
    }
    

}
**/