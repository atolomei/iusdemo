package io.demo;

import org.apache.wicket.Page;

import org.apache.wicket.authroles.authentication.AuthenticatedWebSession;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AnnotationsRoleAuthorizationStrategy;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.springframework.stereotype.Component;
import com.giffing.wicket.spring.boot.starter.app.WicketBootSecuredWebApplication;
import com.giffing.wicket.spring.boot.starter.configuration.extensions.external.spring.security.SecureWebSession;

import io.demo.help.HelpImageResource;
import io.demo.web.home.DemoHomePage;
import io.demo.web.security.LoginPage;


 
@Component
public class WicketWebApplication extends WicketBootSecuredWebApplication {

	@Override
	protected Class<? extends WebPage> getSignInPageClass() {
		return LoginPage.class;
	}

	@Override
	public Class<? extends Page> getHomePage() {
		return DemoHomePage.class;
	}

	@Override
	protected Class<? extends AuthenticatedWebSession> getWebSessionClass() {
		return SecureWebSession.class;
	}

	@Override
	public void init() {
		super.init();

	 
		// ⭐ Serve help images from filesystem (helpDir/images/)
		mountResource("/help/images/${name}", new ResourceReference("helpImages") {
			private static final long serialVersionUID = 1L;
			@Override
			public IResource getResource() {
				return new HelpImageResource();
			}
		});

		// ⭐ Serve help images in language subdirectories (helpDir/images/es/, /en/, /pt/ ...)
		mountResource("/help/images/${lang}/${name}", new ResourceReference("helpImagesLang") {
			private static final long serialVersionUID = 1L;
			@Override
			public IResource getResource() {
				return new HelpImageResource();
			}
		});

 
		
		getComponentInstantiationListeners().add(new SpringComponentInjector(this));

		// ⭐ Enable Wicket page authorization
		getSecuritySettings().setAuthorizationStrategy(new AnnotationsRoleAuthorizationStrategy(this));
	}

}
