package io.demo;

import java.util.Locale;

import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.application.IComponentInstantiationListener;
import org.apache.wicket.markup.html.WebPage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.demo.util.BannerUtil;
import jakarta.annotation.PostConstruct;



/**
 * 
 * 
 * SOLR
 * /opt/homebrew/opt/solr/bin/solr start --user-managed -m 4g
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 */
@SpringBootApplication
public class App {
	
	@SuppressWarnings("unused")
	static private Logger logger = Logger.getLogger(App.class.getName());
	static private Logger std_logger = Logger.getLogger("StartupLogger");

	static public String[] cmdArgs = null;
	
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }


    @PostConstruct
	public void onInitialize() {
		std_logger.info("");

		// for (String s : DellemuseServerAppVersion.getAppCharacterName())
		// std_logger.info(s);

		for (String s : BannerUtil.generateBanner("IUS"))
			std_logger.info(s);

		std_logger.info("");
		std_logger.info("version: " + "0.1b");

		std_logger.info(ServerConstant.SEPARATOR);
		std_logger.info("This software is licensed under the Apache License, Version 2.0");
		std_logger.info("http://www.apache.org/licenses/LICENSE-2.0");

		initShutdownMessage();
	}

	public static class SessionLocale implements IComponentInstantiationListener {
		@Override
		public void onInstantiation(Component component) {
			if (component instanceof WebPage) {
				Session session = Session.get();
				if (session.getLocale() == null) {
					session.setLocale(Locale.forLanguageTag("es")); // Set Spanish locale
				}
			}
		}
	}

	private void initShutdownMessage() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				std_logger.info(ServerConstant.SEPARATOR);
				std_logger.info("");
				std_logger.info("As the roman legionaries used to say when falling in battle");
				std_logger.info("'Dulce et decorum est pro patria mori'...Shuting down... goodbye.");
				std_logger.info("");
			}
		});
	}
	

}