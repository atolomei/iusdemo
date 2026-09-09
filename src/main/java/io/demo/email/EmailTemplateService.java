package io.demo.email;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.annotation.JsonIgnore;

import freemarker.cache.FileTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import io.demo.Logger;
import io.demo.service.BaseService;
import io.demo.service.Settings;
import io.demo.service.SystemService;
import jakarta.annotation.PostConstruct;

/**
 * Renders email body HTML from FreeMarker templates stored on the filesystem.
 *
 * <p>Template directory layout mirrors {@code HelpService}:</p>
 * <pre>
 *   {templates.dir}/
 *     {lang}/
 *       {templateName}.html
 * </pre>
 *
 * <p>The directory root is configured via {@code templates.dir} in
 * {@code application.properties} (default: {@code templates}).</p>
 */
@Service
public class EmailTemplateService extends BaseService implements SystemService {

    static private Logger logger = Logger.getLogger(EmailTemplateService.class.getName());


    public static final String CANDIDATE_EMAIL_VALIDATION = "candidate-email-validation.html";
    public static final String PASSWORD_RESET 	= "password-reset.html";
    public static final String CANDIDATE_SUBMT_NOTIFY_ADMIN 	= "candidate-submit-notify-admin.html";
    public static final String USER_WELCOME = "user-welcome.html";
    
    
    /** One FreeMarker Configuration per language directory, keyed by lang code. */
    @JsonIgnore
    private final ConcurrentHashMap<String, Configuration> configCache = new ConcurrentHashMap<>();

    @JsonIgnore
    private final OffsetDateTime created = OffsetDateTime.now();

    public EmailTemplateService(Settings settings) {
        super(settings);
    }

    // ---------------------------------------------------------------
    // Public API
    // ---------------------------------------------------------------

    /**
     * Renders a FreeMarker template located at
     * {@code {templates.dir}/{lang}/{templateName}.html} using the given
     * data model.
     *
     * @param templateName  filename without path, e.g. {@code "candidate-email-validation.html"}
     * @param lang          language subdirectory, e.g. {@code "spa"}, {@code "en"}, {@code "pt"}
     * @param model         key/value pairs substituted into the template
     * @return              rendered HTML string, or {@code null} on error
     */
    public String render(String templateName, String lang, Map<String, String> model ) {

        String normalizedLang = normalizeForResourceBundle(lang);
        Configuration cfg = getOrCreateConfig(normalizedLang);

        if (cfg == null) {
            logger.error("Could not create FreeMarker configuration for lang -> " + normalizedLang);
            return null;
        }

        try {
            Template template = cfg.getTemplate(templateName);
            StringWriter writer = new StringWriter();
            template.process(model, writer);
            return writer.toString();

        } catch (IOException e) {
            logger.error(e, "Template not found -> " + templateName + " [" + normalizedLang + "]");
            return null;
        } catch (TemplateException e) {
            logger.error(e, "Template processing error -> " + templateName + " [" + normalizedLang + "]");
            return null;
        }
    }

    /**
     * Renders a template using the default server language defined in settings.
     *
     * @param templateName  filename, e.g. {@code "candidate-email-validation.html"}
     * @param model         key/value pairs substituted into the template
     * @return              rendered HTML string, or {@code null} on error
     */
    public String render(String templateName, Map<String, String> model) {
        return render(templateName, getSettings().getDefaultLocale().getLanguage(), model);
    }

    // ---------------------------------------------------------------
    // Lifecycle
    // ---------------------------------------------------------------

    @PostConstruct
    protected void onInit() {
        try {
            //setStatus(ServiceStatus.RUNNING);
            logger.debug("EmailTemplateService started. Templates dir -> " + getSettings().getTemplatesDir());
        } catch (Exception e) {
            //setStatus(ServiceStatus.STOPPED);
            logger.error(e, "EmailTemplateService failed to start");
        }
    }

    public OffsetDateTime getOffsetDateTimeCreated() {
        return created;
    }

    // ---------------------------------------------------------------
    // Private helpers
    // ---------------------------------------------------------------

    /**
     * Returns a cached (or newly created) FreeMarker {@link Configuration}
     * whose template loader points to {@code {templates.dir}/{lang}/}.
     */
    private Configuration getOrCreateConfig(String lang) {
        return configCache.computeIfAbsent(lang, l -> {
            String dirPath = getSettings().getTemplatesDir() + File.separator + l;
            File dir = new File(dirPath);
            if (!dir.exists() || !dir.isDirectory()) {
                logger.warn("Templates directory does not exist -> " + dir.getAbsolutePath());
                return null;
            }
            try {
                Configuration cfg = new Configuration(Configuration.VERSION_2_3_33);
                cfg.setTemplateLoader(new FileTemplateLoader(dir));
                cfg.setDefaultEncoding("UTF-8");
                cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
                cfg.setLogTemplateExceptions(false);
                cfg.setWrapUncheckedExceptions(true);
                cfg.setFallbackOnNullLoopVariable(false);
                return cfg;
            } catch (IOException e) {
                logger.error(e, "Failed to create FreeMarker configuration for dir -> " + dirPath);
                return null;
            }
        });
    }

    /** Normalises locale strings the same way {@code HelpService} does. */
    private String normalizeForResourceBundle(String str) {
        if (str == null)
            return "es";
        if (str.startsWith("es"))  return "es";
        if (str.startsWith("en"))  return "en";
        if (str.startsWith("pt")) return "pt";
        return str;
    }

	@Override
	public String toJSON() {
		// TODO Auto-generated method stub
		return null;
	}
}