package io.demo;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import java.io.File;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.demo.model.User;
import io.demo.model.db.service.UserDBService;
import io.demo.service.LegalSearchService;
import io.demo.service.QueryLogService;
import io.demo.service.ServiceLocator;
import io.demo.service.Settings;
import io.demo.service.rag.KbeeRAGClient;
import io.demo.service.rag.RagResponse;
import io.demo.util.Constant;


/**
 * 
 * 
 * 
 * 
 * 
 * Una alternativa todavía más jurídica/institucional:
 * 
 * 
 * Ordenar por:
 * 
- Más recientes
- Recomendación   
- Coincidencia técnica 

 * 
 * Yo usaría:

- Fecha
- Más relevantes
- Mayor coincidencia con la consulta

Pero hay una distinción importante entre las dos últimas:

- Relevancia → sería el orden determinado por el LLM a partir de los resultados seleccionados.
- Coincidencia → sería el score técnico del Reranker, es decir, qué tan estrechamente se corresponde el contenido del fallo con la consulta.
 * 
 * 
 * 1. Fuero / materia
Por ejemplo:


Ordenar por:  [Más recientes | Más relevantes  (orden determinado por el LLM)| Mayor coincidencia con la consulta (reranker)] 
Cantidad:     [hasta 5 | hasta 10 | hasta 20]
Período:      [Todos | Últimos 2 años | Últimos 5 años | Últimos 10 años | Últimos 20 años ]
Materia:      [Todos | Civil | Penal | Laboral | Comercial | Administrativo | Constitucional | Tributario | Ambiental | Familia | Otros]



Esto puede ser muy potente si la base contiene jurisprudencia de distintos fueros. Pero si el usuario ya está dentro de una colección específica, no lo pondría.
2. Fecha
Más que como orden, podría ser un filtro temporal:


 */

@Component
public class AppStartupApplicationRunner implements ApplicationRunner {

	static private Logger logger = Logger.getLogger(AppStartupApplicationRunner.class.getName());
	static private Logger startupLogger = Logger.getLogger("StartupLogger");

	@Autowired
	@JsonIgnore
	private final ApplicationContext appContext;

	public AppStartupApplicationRunner(ApplicationContext appContext) {
		this.appContext = appContext;
	}
 

	@Override
	public void run(ApplicationArguments args) throws Exception {

		if (startupLogger.isDebugEnabled()) {
			startupLogger.debug("Command line args:");
			args.getNonOptionArgs().forEach(item -> startupLogger.debug(item));
		}

		Locale.setDefault(Locale.forLanguageTag("es"));

		startupLogger.info(Constant.SEPARATOR);

		Settings settings = getAppContext().getBean(Settings.class);
		startupLogger.info("App name -> " + settings.getAppName());
		startupLogger.info("Port -> " + settings.getPort());

		startupLogger.info(Constant.SEPARATOR);

		setupRootUser();

		for (String s : args.getSourceArgs()) {
			logger.debug(s);
		}
	}

	/**
	 * Ensures the {@code root} user exists with a valid password.
	 * <p>
	 * The initial password can be set with the environment variable
	 * {@code DEMO_ROOT_PASSWORD} (defaults to {@code root}). Passwords are stored
	 * encoded by the application's {@link org.springframework.security.crypto.password.PasswordEncoder}
	 * (i.e. {@code {bcrypt}...}), which is what Spring Security expects at login.
	 * </p>
	 */
	private void setupRootUser() {

		UserDBService users = getAppContext().getBean(UserDBService.class);

		java.util.Optional<User> oRoot = users.findByUsername("root");

		if (oRoot.isEmpty()) {
			String rawPassword = System.getenv().getOrDefault("DEMO_ROOT_PASSWORD", "root");
			User root = users.create("root", null);
			root.setRole(io.demo.model.Role.SYSADMIN);
			users.updatePassword(root, rawPassword, root);
			startupLogger.info("root user created (change the default password!)");
			return;
		}

		// migrate legacy raw-bcrypt hashes (no {bcrypt} prefix) so the
		// delegating PasswordEncoder can validate them
		for (User u : users.findAll()) {
			String p = u.getPassword();
			if (p != null && p.startsWith("$2") && !p.startsWith("{")) {
				u.setPassword("{bcrypt}" + p);
				users.save(u);
				startupLogger.info("password hash of user '" + u.getName() + "' migrated to {bcrypt} format");
			}
		}
	}
	
 
 /**
	
	  @Bean CommandLineRunner loghistory() {
		  
	        return args -> {
	  
	        try {
	  
	         
	        	UserDBService users = ((UserDBService) ServiceLocator.getInstance().getBean(UserDBService.class));
	         
	        	LegalSearchService l = ((LegalSearchService) ServiceLocator.getInstance().getBean(LegalSearchService.class));

	        	QueryLogService ql = ((QueryLogService) ServiceLocator.getInstance().getBean(QueryLogService.class));
	        	
	        	KbeeRAGClient k = ((KbeeRAGClient) ServiceLocator.getInstance().getBean(KbeeRAGClient.class));
	    
	        	final User root = users.findByUsername("root").orElseThrow(() -> new Exception("Root user not found"));
							
				
	        	k.getResponseHistory().forEach( rg -> {
	        			// skip entries already logged (avoid duplicates on restart)
	        			if (ql.getQueryDBService().getMostRecentByText(rg.question()) != null) {
	        				logger.debug("question already logged, skipping: " + rg.question());
	        				return;
	        			}
	        			// results must be the RagResponse serialized as JSON,
	        			// so QueryLogService can deserialize it as a cache entry
	        			ql.log(rg.question(), l.toJson(rg), rg.elapsedMilliseconds(), root, "na" );
	        			logger.debug("logged question: " + rg.question());
	          	});
	        	
	          	logger.debug("done");
	  
	        } catch (Exception e) { logger.error(e); } }; 
	       
	  
	  }
	 */
	
	 
	  
	  
	
	/**
	 * 
	 * 
	 
	 
	  UPDATE users SET password = '{bcrypt}$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG' WHERE name = 'root';


That hash corresponds to the plain text root (a commonly used test hash). After running it, sign in on /signin with root / root.


Notes:

	•  The {bcrypt} prefix is required because your SecurityConfig uses the delegating password encoder.
	•  If your table/column names differ (e.g. users, username), adjust accordingly — check the @Table/@Column annotations on User.java.
	•  To generate your own hash instead:
	
	
System.out.println("{bcrypt}" + new BCryptPasswordEncoder().encode("root"));



	 * 
	 */
	  
	        	

	 
	
	 /**
		  @Bean CommandLineRunner loghistory() {
			  
		        return args -> {
		  
		        try {
		  
		         
		        	UserDBService users = ((UserDBService) ServiceLocator.getInstance().getBean(UserDBService.class));
		        	 
		        	
		        	
		        	User root = users.findByUsername("root").orElseThrow(() -> new Exception("Root user not found"));
		        	root.setPassword("root");
		  		        	
		        	users.save(root);
		        	
		        	logger.debug("done");
		  
		        } catch (Exception e) { logger.error(e); } }; 
		       
		  
		  }
	**/
	
	
	

	public ApplicationContext getAppContext() {
		return appContext;
	}
}
