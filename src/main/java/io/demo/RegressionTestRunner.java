package io.demo;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import io.demo.service.RegressionTestService;

/**
 * Runs the regression test on startup when the property
 * {@code regressiontest.enabled=true} is set (e.g.
 * {@code --regressiontest.enabled=true} on the command line), and exits the
 * JVM when the test is completed.
 */
@Component
@Order(100)
@ConditionalOnProperty(name = "regressiontest.enabled", havingValue = "true")
public class RegressionTestRunner implements ApplicationRunner {

	static private Logger logger = Logger.getLogger(RegressionTestRunner.class.getName());

	private final RegressionTestService regressionTestService;
	private final ConfigurableApplicationContext context;

	public RegressionTestRunner(RegressionTestService regressionTestService, ConfigurableApplicationContext context) {
		this.regressionTestService = regressionTestService;
		this.context = context;
	}

	@Override
	public void run(ApplicationArguments args) {

		int exitCode = 0;

		try {
			this.regressionTestService.run();
		} catch (Exception e) {
			logger.error(e);
			exitCode = 1;
		}

		final int code = exitCode;
		System.exit(SpringApplication.exit(this.context, () -> code));
	}
}

/**


--regressiontest.enabled=true
--regressiontest.efforts=medium,high


**/