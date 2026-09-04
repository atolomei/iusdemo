package io.demo.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;


@Configuration
public class SecurityHandlersConfig {

    @Bean
    public SavedRequestAwareAuthenticationSuccessHandler savedRequestAwareAuthenticationSuccessHandler() {
        SavedRequestAwareAuthenticationSuccessHandler handler =
                new SavedRequestAwareAuthenticationSuccessHandler();
        // Opcional: si no hay SavedRequest, redirigir a home
        handler.setDefaultTargetUrl("/");  
        handler.setAlwaysUseDefaultTargetUrl(false);  // usa SavedRequest si existe
        return handler;
    }
	
	
}
