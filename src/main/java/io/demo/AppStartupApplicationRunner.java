package io.demo;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
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

import io.demo.service.Settings;
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

		for (String s : args.getSourceArgs()) {
			logger.debug(s);
		}
	}

	 

	public ApplicationContext getAppContext() {
		return appContext;
	}
}
