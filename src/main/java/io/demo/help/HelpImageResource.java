package io.demo.help;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.Instant;

import org.apache.wicket.request.resource.AbstractResource;

import io.demo.Logger;
import io.demo.service.ServiceLocator;
import io.demo.service.Settings;
 
 

/**
 * Serves image files from <helpDir>/images/ on the filesystem.
 * Mounted in Wicket as /help/images/${name}.
 */
public class HelpImageResource extends AbstractResource {

	private static final long serialVersionUID = 1L;

	static private Logger logger = Logger.getLogger(HelpImageResource.class.getName());

	@Override
	protected ResourceResponse newResourceResponse(Attributes attributes) {

		String lang = attributes.getParameters().get("lang").toString("");
		String name = attributes.getParameters().get("name").toString("");

		Settings settings = (Settings) ServiceLocator.getInstance().getBean(Settings.class);
		String imagesBase = settings.getHelpDir() + File.separator + "images";
		File imageFile = (lang != null && !lang.isBlank())
			? new File(imagesBase + File.separator + lang, name)
			: new File(imagesBase, name);

		ResourceResponse response = new ResourceResponse();

		if (!imageFile.exists() || !imageFile.isFile()) {
			response.setError(404, "Help image not found: " + (lang.isBlank() ? "" : lang + "/") + name);
			return response;
		}

		String contentType = resolveContentType(name);
		response.setContentType(contentType);
		response.setLastModified(Instant.ofEpochMilli(imageFile.lastModified()));
		response.setContentLength(imageFile.length());

		response.setWriteCallback(new WriteCallback() {
			@Override
			public void writeData(Attributes attributes) throws IOException {
				try (FileInputStream fis = new FileInputStream(imageFile)) {
					attributes.getResponse().write(fis.readAllBytes());
				}
			}
		});

		return response;
	}

	private String resolveContentType(String name) {
		String lower = name.toLowerCase();
		if (lower.endsWith(".webp"))  return "image/webp";
		if (lower.endsWith(".png"))   return "image/png";
		if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
		if (lower.endsWith(".gif"))   return "image/gif";
		if (lower.endsWith(".svg"))   return "image/svg+xml";
		return "application/octet-stream";
	}
}
