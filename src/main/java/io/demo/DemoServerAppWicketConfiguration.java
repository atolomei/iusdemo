package io.demo;

import org.apache.wicket.protocol.http.WebApplication;

import com.giffing.wicket.spring.boot.context.extensions.ApplicationInitExtension;
import com.giffing.wicket.spring.boot.context.extensions.WicketApplicationInitConfiguration;


@ApplicationInitExtension
public class DemoServerAppWicketConfiguration implements WicketApplicationInitConfiguration {

    @Override
	public void init(WebApplication webApplication) {
	   webApplication.getCspSettings().blocking().disabled();
	 //  webApplication.getApplicationSettings().setPageExpiredErrorPage(SessionExpiredPage.class);
	  // webApplication.getApplicationSettings().setInternalErrorPage(InternalErrorPage.class);
	  // webApplication.getResourceSettings().setThrowExceptionOnMissingResource(false);
	   
	}
}
