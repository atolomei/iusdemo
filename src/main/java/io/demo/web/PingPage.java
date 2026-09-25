package io.demo.web;

import java.sql.Connection;

import javax.sql.DataSource;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.handler.TextRequestHandler;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.wicketstuff.annotation.mount.MountPath;

import io.demo.Logger;

/**
 * Health check page mounted on "/ping".
 * <p>
 * Accessible without authentication. Checks that the database is up and
 * returns the plain text "ok", or an error message otherwise.
 * 
 * 
 * curl http://localhost:8080/ping
 * 
 * </p>
 */
@MountPath("/ping")
public class PingPage extends WebPage {

    private static final long serialVersionUID = 1L;

    static private Logger logger = Logger.getLogger(PingPage.class.getName());

    /** seconds to wait for the database connection validation */
    private static final int DB_VALIDATION_TIMEOUT_SECS = 5;

    @SpringBean
    private DataSource dataSource;

    public PingPage() {
        this(new PageParameters());
    }

    public PingPage(PageParameters parameters) {
        super(parameters);

        String response;
        try {
            checkDatabase();
            response = "ok";
        } catch (Exception e) {
            logger.error(e, "ping failed (database unavailable?)");
            response = "error: database check failed -> " + e.getClass().getSimpleName()
                    + (e.getMessage() != null ? (": " + e.getMessage()) : "");
        }

        // return plain text instead of rendering HTML markup
        getRequestCycle().scheduleRequestHandlerAfterCurrent(
                new TextRequestHandler("text/plain", "UTF-8", response));
    }

    private void checkDatabase() throws Exception {
        if (dataSource == null)
            throw new IllegalStateException("no DataSource available");

        try (Connection connection = dataSource.getConnection()) {
            if (!connection.isValid(DB_VALIDATION_TIMEOUT_SECS))
                throw new IllegalStateException("database connection is not valid");
        }
    }
}


