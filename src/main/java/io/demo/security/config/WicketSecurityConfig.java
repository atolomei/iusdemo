package io.demo.security.config;



import com.giffing.wicket.spring.boot.context.security.AuthenticatedWebSessionConfig;
import com.giffing.wicket.spring.boot.starter.configuration.extensions.external.spring.security.SecureWebSession;

  
import org.apache.wicket.authroles.authentication.AbstractAuthenticatedWebSession;
 
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class WicketSecurityConfig {
	 @Bean
	 public AuthenticatedWebSessionConfig authenticatedWebSessionConfig() {
		 return new AuthenticatedWebSessionConfig() {
			@Override
			public Class<? extends AbstractAuthenticatedWebSession> getAuthenticatedWebSessionClass() {
				return SecureWebSession.class;
			}
	      };
	 }
	 
}
