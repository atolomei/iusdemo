package io.demo.security.config;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationEventPublisher;
import org.springframework.security.authentication.DefaultAuthenticationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
 
 
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.DelegatingAuthenticationEntryPoint;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

import io.demo.Logger;
 

@Configuration
@EnableWebSecurity(debug = true)
public class SecurityConfig {

	static private Logger logger = Logger.getLogger(SecurityConfig.class.getName()); 

	@Bean
	public AuthenticationEventPublisher authenticationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
		return new DefaultAuthenticationEventPublisher(applicationEventPublisher);
	}

	/**
	 * Required by SecureWebSession (wicket-spring-boot starter), which injects a
	 * bean named "authenticationManager". The demo does not require login, so a
	 * simple in-memory user is enough.
	 */
	@Bean
	public AuthenticationManager authenticationManager(UserDetailsService userDetailsService,
			PasswordEncoder passwordEncoder) {
		DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
		provider.setPasswordEncoder(passwordEncoder);
		return new ProviderManager(provider);
	}

	@Bean
	public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
		UserDetails user = User.builder()
				.username("demo")
				.password(passwordEncoder.encode("demo"))
				.roles("USER")
				.build();
		return new InMemoryUserDetailsManager(user);
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return PasswordEncoderFactories.createDelegatingPasswordEncoder();
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

	    http
	        // ⭐ Request rules
	        .authorizeHttpRequests(auth -> auth
	            .requestMatchers(
	                "/signin",
	                "/signin/**",
	                "/oauth2/**",
	                "/wicket/**",
	                "/wicket/resource/**",
	                "/css/**",
	                "/js/**",
	                "/images/**"
	            ).permitAll()

	            // ⭐ Let Wicket handle authorization
	            .anyRequest().permitAll()
	        )

	        // ⭐ Redirect to login when authentication is required
	        .exceptionHandling(ex -> ex
	            .authenticationEntryPoint(customEntryPoint())
	        )

	        // ⭐ Session handling
	        .sessionManagement(session -> session
	            .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
	            .sessionFixation(fix -> fix.migrateSession())
	        )

	        // ⭐ Persist SecurityContext in HTTP session
	        .securityContext(context -> context
	            .securityContextRepository(new HttpSessionSecurityContextRepository())
	            .requireExplicitSave(false)
	        )

	        // ⭐ Disable Spring login page (Wicket handles UI)
	        .formLogin(form -> form.disable())

	        .csrf(csrf -> csrf.disable())

	        // ⭐ Logout
	        .logout(logout -> logout
	            .logoutUrl("/logout")
	            .logoutSuccessUrl("/signin?logout")
	            .invalidateHttpSession(true)
	            .deleteCookies("JSESSIONID")
	            .permitAll()
	        );

	    return http.build();
	}

    @Bean
    public AuthenticationEntryPoint customEntryPoint() {
        LoginUrlAuthenticationEntryPoint loginEntryPoint =
                new LoginUrlAuthenticationEntryPoint("/signin");

        return (request, response, authException) -> {
            // Evitar redirigir si ya estamos en /signin
            if (PathPatternRequestMatcher.pathPattern("/signin/**").matcher(request).isMatch()) {
                // No redirige, deja pasar la request
                return;
            }
            // Para todo lo demás, redirige al login
            loginEntryPoint.commence(request, response, authException);
        };
    }

	
		 
		
		
		
		/**
		
		http
	    .authorizeHttpRequests(auth -> auth
	        //.requestMatchers("/login").permitAll()
	    		.requestMatchers("/dellemuselogin").permitAll()
	    		.anyRequest().authenticated()
	    )
	   
	    .formLogin(login -> login.disable()) 
		
	   // .formLogin(form -> form
       //         .loginPage("/login") // Custom login page URL
       //         .permitAll()
       // )
	    
	    .logout(logout -> logout
				.logoutUrl("/logout")
				.logoutSuccessUrl("/login?logout") // Custom logout success URL
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID") // Delete specific cookies
                // .addLogoutHandler(myCustomLogoutHandler) // Add custom logout handler
                .permitAll()
                );


		
		http.csrf().disable();
		
		http.csrf(csrf -> csrf.disable());
*/		
		 
		
		/**
		http
			.authorizeHttpRequests((authorize) -> authorize
				.requestMatchers("/login").permitAll() 
				.anyRequest().authenticated()
				)
			
			.httpBasic(Customizer.withDefaults())
			
			.formLogin(form -> form
	                 .loginPage("/login") // Custom login page URL
	                 .permitAll()
	         )
		
			.logout(logout -> logout
					.logoutUrl("/logout")
					.logoutSuccessUrl("/login?logout") // Custom logout success URL
	                .invalidateHttpSession(true)
	                .deleteCookies("JSESSIONID") // Delete specific cookies
	                // .addLogoutHandler(myCustomLogoutHandler) // Add custom logout handler
	                .permitAll()
	                );
		
		 http.csrf(AbstractHttpConfigurer::disable);
		*/ 
		
		
	 

	/**
	@Bean
	public AuthenticationManager authenticationManager(
			UserDetailsService userDetailsService,
			PasswordEncoder passwordEncoder) {
		DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider(userDetailsService);
		authenticationProvider.setPasswordEncoder(passwordEncoder);

		return new ProviderManager(authenticationProvider);
	}
**/
	/**
	@Bean
	public UserDetailsService userDetailsService() {
		UserDetails userDetails = User.withDefaultPasswordEncoder()
			.username("user")
			.password("11")
			.roles("USER")
			.build();

		return new InMemoryUserDetailsManager(userDetails);
	}

	
	@Bean
	public PasswordEncoder passwordEncoder() {
		return PasswordEncoderFactories.createDelegatingPasswordEncoder();
	}
**/
	
 
	
}