package io.demo.email;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

 
 
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.demo.Logger;
import io.demo.service.BaseService;
import io.demo.service.Settings;
import io.demo.service.SystemService;
import io.demo.util.Check;


@Service
public class EmailService extends BaseService implements SystemService  {

	static private Logger logger = Logger.getLogger( EmailService.class.getName());

	
	static private Logger elogger = Logger.getLogger( "email" );
	
//	@JsonIgnore
//	@Autowired
//    private JavaMailSender javaMailSender;
	
	
	// ---------------------------------------------------------------
    // HTTP client (reused across calls)
    // ---------------------------------------------------------------

	@JsonIgnore
    private final HttpClient httpClient = HttpClient.newHttpClient();
    
    
	public EmailService(Settings settings) {
		super(settings);
	}
	
	
	public String sendText(String to, String subject, String text)
            throws IOException, InterruptedException {
	return send( getSettings().getEmailFrom(), to, subject, text, "text");
}
	
	public String sendHTML(String to, String subject, String text)
	            throws IOException, InterruptedException {
		return send( getSettings().getEmailFrom(), to, subject, text, "html");
    }
	

	 /**
     * Sends an email with an explicit {@code from} address.
     *
     * @param from    sender address
     * @param to      recipient address
     * @param subject email subject line
     * @param text    plain-text body
     * @return the Mailgun JSON response body
     * @throws IOException          if an I/O error occurs
     * @throws InterruptedException if the operation is interrupted
     */
	
    public String send(String from, String to, String subject, String text, String type)
            throws IOException, InterruptedException {

    	
    	Check.requireNonNullStringArgument(to, "to cannot be null");
    	Check.requireNonNullStringArgument(from, "to cannot be null");
    	Check.requireNonNullStringArgument(subject, "to cannot be null");
    	Check.requireNonNullStringArgument(text, "to cannot be null");

    	
        Map<String, String> params = new LinkedHashMap<>();
        params.put("from",    from);
        params.put("to",      to);
        params.put("subject", subject);
        params.put(type,     text);
        
        String formBody = buildFormBody(params);
        
        if (type.equals("text")) {
			logger.debug("Sending text email with body -> " + text);
		}

        if (getSettings().isEmailSenderEnabled()) {
        	logger.debug("Sending email to -> " + to + " with subject  -> " + subject);
		} else {
			logger.warn("Email sender is disabled. Email to " + to + " with subject '" + subject + "' will not be sent.");
			return "{\"message\": \"Email sender is disabled.\"}";
		}
        
        String basicAuth = buildBasicAuth("api", getSettings().getEmailApiKey());
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create( getSettings().getEmailBaseurl()))
                .header("Authorization", basicAuth)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(formBody))
                .build();

        
        HttpResponse<String> response =
                httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        
        elogger.info("Email sent -> to. " + to + "| subject. " + subject + " | Mailgun response: " + response.statusCode() + " - " + response.body());
        
        logger.debug("Mailgun response [" + response.statusCode() + "]: " + response.body());

        if (response.statusCode() != 200) {
            logger.error("Mailgun send failed [" + response.statusCode() + "]: " + response.body() + " | to." + to + " | subject." + subject);
            elogger.error("Mailgun send failed [" + response.statusCode() + "]: " + response.body() + " | to." + to + " | subject." + subject);
        }

        return response.body();
    }

    /**
     * Sends the default test message defined in the Mailgun sandbox quick-start.
     *
     * @return the Mailgun JSON response body
     * @throws IOException          if an I/O error occurs
     * @throws InterruptedException if the operation is interrupted
    public String sendTestMessage() throws IOException, InterruptedException {
        return send(
            "Alejandro Tolomei <atolomei@novamens.com>",
            "Hello Alejandro Tolomei",
            "Congratulations Alejandro Tolomei, you just sent an email with Mailgun!"
            + " You are truly awesome!"
        );
    }
    */
    
  
    private static String buildFormBody(Map<String, String> params) {
        return params.entrySet().stream()
                .map(e -> URLEncoder.encode(e.getKey(),   StandardCharsets.UTF_8)
                        + "="
                        + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));
    }

    private static String buildBasicAuth(String username, String password) {
        String credentials = username + ":" + password;
        return "Basic " + Base64.getEncoder()
                .encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
    }


	@Override
	public String toJSON() {
		 
		return this.getClass().getName();
	}	
 
	
	 

	
	 
	
}
