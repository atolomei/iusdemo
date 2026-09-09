package io.demo.web.page;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.MetaDataHeaderItem;
import org.apache.wicket.markup.head.PriorityHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import io.demo.Logger;
import io.demo.email.EmailTemplateService;
import io.demo.model.User;
import io.demo.model.db.service.UserDBService;
import io.demo.model.db.service.PersistentTokenDBService;
import io.demo.model.db.service.QueryDBService;
import io.demo.model.db.service.QueryFeedbackDBService;
import io.demo.model.db.service.StatDBService;
import io.demo.service.DateTimeService;
import io.demo.service.LegalSearchService;
import io.demo.service.QueryHistoryService;
import io.demo.service.ServiceLocator;
import io.demo.service.TesauroService;
import io.demo.service.UserSettingsService;
import io.demo.web.panel.MenuEntry;
import io.demo.web.panel.ObjectModel;
import io.wktui.event.UIEvent;
import io.wktui.nav.breadcrumb.BreadCrumb;
import io.wktui.nav.breadcrumb.HREFBCElement;
import wktui.base.UIEventListener;
import wktui.bootstrap.Bootstrap;


/**
 * Base page with the book layout: fixed dark toolbar, collapsible side menu
 * (state persisted by /js/book.js) and the main page-wrap canvas.
 *
 * Subclasses provide their content via Wicket markup inheritance
 * (&lt;wicket:child/&gt;) and may override the toolbar/menu hooks.
 */
public abstract class BasePage extends WebPage {


	private static final long serialVersionUID = 1L;

	
	static private Logger logger = Logger.getLogger(BasePage.class.getName());

	private static final ResourceReference BOOTSTRAP_CSS = Bootstrap.getCssResourceReference();
	private static final ResourceReference BOOTSTRAP_JS = Bootstrap.getJavaScriptResourceReference();

	
    
    private static final ResourceReference CSS = new CssResourceReference(BasePage.class, "./demo.css");
	

	static private String xfavicon;
	static private String xlanguage;
	static private String xrobots;
	static private String xrating;
	static private String xkeywords = "demo";

	private static final String XUA_Compatible = "IE=Edge";
	
	

	// 1 Day
	static private final int COOKIE_DURATION = 86400 * 1;
	
	
	static {
		xlanguage = "English";
		xrobots = "NOINDEX, NOFOLLOW";
		xrating = "General";
	}


	private String keywords = xkeywords;
	private String language = xlanguage;

	private String xuacompatible; // = "IE=8"; // IE=edge
	private String robots = xrobots;
	private String fonts = null;
	private String favicon = null;

	private ResourceReference rcss;
	private WebMarkupContainer wfont;
	private WebMarkupContainer fvicon;
	private WebMarkupContainer vp;
	private WebMarkupContainer desc;
	private WebMarkupContainer lang;
	private WebMarkupContainer kw;	

	
	private boolean visit_logged = false;
	
	private IModel<User> sessionUserModel;

	
	protected void addListeners() {
	}

	
public abstract boolean canAccess(Optional<User> user); 

	@Override
	public void onDetach() {
		super.onDetach();

		if (sessionUserModel != null) {
			sessionUserModel.detach();
		}
	}
	
	protected void setVisitLogged(boolean b) {
		this.visit_logged = b;
	}

	protected boolean isVisitLogged() {
		return this.visit_logged;
	}

	public boolean isLogVisit() {
		return false;
	}
	
	//public Stat getStat() {
	//	return null;
	//}
	
	
	@Override
	public void onAfterRender() {
		super.onAfterRender();
		if (isLogVisit())
			logVisit();
	}
	
	protected void logVisit() {

		if (isVisitLogged())
			return;

		try {

			// Stat stat = getStat();

			String userAgent = ((WebRequest) getRequest()).getHeader("User-Agent");

			if (userAgent != null) {
				logger.debug("user agent: " + userAgent);
				// stat.setUserAgent(userAgent);
			}

			//if (stat == null) {
			//	logger.error("getStat() returned null for page -> " + getClass().getSimpleName());
			//	return;
			//}

			// getLogVisitService().logVisit(stat);

			
			// Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML,
			// like Gecko) Chrome/146.0.0.0 Safari/537.36
			/**
			 * SiteStatInEvent stat = new SiteStatInEvent();
			 * 
			 * stat.domain_id = getDomain()!=null?
			 * Long.valueOf(getDomain().getId().toString()) : null; stat.sessionId =
			 * WebSession.get().getId();
			 * 
			 * stat.page_type = getPageType();
			 * 
			 * stat.site_id = Long.valueOf(getApplicationMenuSection().getId()); // Section
			 * (security, tasks) stat.site_title = getApplicationMenuSection().getKey(); //
			 * Section
			 * 
			 * // for console page, it is the name of the console // stat.page_id =
			 * getStatsPageId(); stat.page_title = getStatsPageTitle();
			 * 
			 * stat.user_id = getSessionUser()!=null ?
			 * Long.valueOf(getSessionUser().getId().toString()) : null; stat.user_name =
			 * getSessionUser()!=null ? getSessionUser().getFirstLastName() : null;
			 * stat.timestamp = OffsetDateTime.now();
			 * 
			 * stat.user_agent = ((WebRequest) getRequest()).getHeader("User-Agent");
			 * stat.sessionId = WebSession.get().getId();
			 * 
			 * stat.render_milisecs = Long.valueOf(end - start);
			 * 
			 * stat.content_title = getContentTitle(); // content title or
			 * user/domain/dataset title stat.contentId = getContentId(); // for Content
			 * 
			 * stat.content_long_id = getCId()!=null ? (Long) getCId() : Long.valueOf(0);
			 * stat.OId = getContentOId()!=null ? getContentOId().toString() : null;
			 * stat.objectId = getObjectId(); // for User, Domain, DataSet, etc.
			 * 
			 * stat.content_version = getContentVersion() !=null ? getContentVersion() :
			 * Integer.valueOf(0);
			 * 
			 * // Para que se logue en la Base de Datos // El logger de la Clase debe grabar
			 * en // el Appender "SiteStats" siteStatslogger.info(stat);
			 */

		} catch (Exception e) {
			logger.error(e);
		} finally {
			this.setVisitLogged(true);

		}
	}
	
	
	
	@Override
	public void onInitialize() {
		super.onInitialize();

		addListeners();

		this.wfont = new WebMarkupContainer("google-font");
		this.wfont.add(new AttributeModifier("rel", "stylesheet"));
		this.wfont.add(new AttributeModifier("href", getPageFonts()));
		this.wfont.setVisible(getPageFonts() != null);
		add(this.wfont);

		this.fvicon = new WebMarkupContainer("favicon");
		this.fvicon.add(new AttributeModifier("rel", "icon"));
		this.fvicon.add(new AttributeModifier("type", "image/x-icon"));
		this.fvicon.add(new AttributeModifier("href", getFavicon()));
		add(this.fvicon);

		this.lang = new WebMarkupContainer("language");
		this.lang.add(new AttributeModifier("name", "language"));
		this.lang.add(new AttributeModifier("language", new Model<String>() {
			private static final long serialVersionUID = 1L;

			@Override
			public String getObject() {
				return xlanguage;
			}
		}));

		add(this.lang);

		this.kw = new WebMarkupContainer("keywords");
		this.kw.add(new AttributeModifier("name", "keywords"));
		this.kw.add(new AttributeModifier("content", new Model<String>() {
			private static final long serialVersionUID = 1L;

			@Override
			public String getObject() {
				return getPageKeywords();
			}
		}));
		add(this.kw);

		this.desc = new WebMarkupContainer("header-description");
		this.desc.add(new AttributeModifier("name", "description"));
		// this.desc.add(new AttributeModifier("content", getPageDescription()));
		this.desc.add(new AttributeModifier("name", "viewport"));
		this.desc.add(new AttributeModifier("content", new Model<String>() {
			private static final long serialVersionUID = 1L;

			@Override
			public String getObject() {
				return "width=device-width, initial-scale=1, shrink-to-fit=no";

			}
		}));
		add(this.desc);

		WebMarkupContainer rating = new WebMarkupContainer("rating");
		rating.add(new AttributeModifier("name", "rating"));
		rating.add(new AttributeModifier("content", xrating));
		add(rating);

		WebMarkupContainer wrobots = new WebMarkupContainer("robots");
		wrobots.add(new AttributeModifier("name", "robots"));
		wrobots.add(new AttributeModifier("content", new Model<String>() {
			private static final long serialVersionUID = 1L;

			@Override
			public String getObject() {
				return getPageRobots();
			}
		}));

		add(wrobots);

		setPageKeywords(keywords);

		//this.wcss = new WebMarkupContainer("css");
		//this.wcss.setVisible(false);
		//add(this.wcss);

		this.vp = new WebMarkupContainer("viewport");
		add(vp);

		if (XUA_Compatible != null)
			setPageXUACompatible(XUA_Compatible);
 	}
	
	public BreadCrumb<Void> createBreadCrumb() {
		BreadCrumb<Void> bc = new BreadCrumb<>();
		bc.addElement(new HREFBCElement("/home", getLabel("home")));
		return bc;
	}
	
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);

		if (getPageXUACompatible() != null) {
			MetaDataHeaderItem headerItem = new MetaDataHeaderItem("meta");
			headerItem.addTagAttribute("http-equiv", "X-UA-Compatible");
			headerItem.addTagAttribute("content", getPageXUACompatible());

			response.render(new PriorityHeaderItem(headerItem));

			headerItem = new MetaDataHeaderItem("meta");
			headerItem.addTagAttribute("http-equiv", "Content-Security-Policy");
			headerItem.addTagAttribute("content", "img-src *");

			response.render(new PriorityHeaderItem(headerItem));
		}

		response.render(JavaScriptHeaderItem.forReference(getApplication().getJavaScriptLibrarySettings().getJQueryReference()));
		response.render(JavaScriptHeaderItem.forReference(getApplication().getJavaScriptLibrarySettings().getWicketAjaxReference()));

		response.render(CssHeaderItem.forReference(BOOTSTRAP_CSS));
		response.render(JavaScriptHeaderItem.forReference(BOOTSTRAP_JS));

		response.render(CssHeaderItem.forReference(CSS));

		if (getCssResource() != null)
			response.render(CssHeaderItem.forReference(getCssResource()));
	}
  
	
	public TesauroService getVoiceDBService() {
		return (TesauroService) ServiceLocator.getInstance().getBean(TesauroService.class);
	}
	

	public LegalSearchService getLegalSearchService() {
		return (LegalSearchService) ServiceLocator.getInstance().getBean(LegalSearchService.class);
	}

	public QueryHistoryService getQueryHistoryService() {
		return (QueryHistoryService) ServiceLocator.getInstance().getBean(QueryHistoryService.class);
	}

	public UserSettingsService getUserSettingsService() {
		return (UserSettingsService) ServiceLocator.getInstance().getBean(UserSettingsService.class);
	}
	

	public DateTimeService getDateTimeService() {
		return (DateTimeService) ServiceLocator.getInstance().getBean( DateTimeService.class);
	}
	
	
	protected EmailTemplateService getEmailTemplateService() {
		return (EmailTemplateService) ServiceLocator.getInstance().getBean(EmailTemplateService.class);
	}

	protected PersistentTokenDBService getPersistentTokenDBServiceDBService() {
		return (PersistentTokenDBService) ServiceLocator.getInstance().getBean(PersistentTokenDBService.class);
	}
	
	
	public Optional<IModel<User>> getOptionalSessionUserModel() {
		 
		
		if (this.getSessionUser().isPresent())
			return Optional.of(this.getSessionUserModel());
		 
		return Optional.empty();

	}
	
	
	public Optional<User> getSessionUser() {

		if (sessionUserModel != null)
			return Optional.of(sessionUserModel.getObject());

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || !auth.isAuthenticated()) {
			return Optional.empty();
		}

		//if (auth.getName().equals("anonymousUser"))
		//	return Optional.empty();


		UserDBService service = (UserDBService) ServiceLocator.getInstance().getBean(UserDBService.class);
		Optional<User> o_user = service.findByUsername("root");

		sessionUserModel = new ObjectModel<User>(o_user.get());

		// log the sign-in (once per session)
		logSignin(sessionUserModel.getObject());

		return Optional.of(sessionUserModel.getObject());

		
		/**
		Optional<User> o_user = service.findByUsername(auth.getName());

		if (o_user == null || o_user.isEmpty())
			return Optional.empty();

		sessionUserModel = new ObjectModel<User>(o_user.get());

		return Optional.of(sessionUserModel.getObject());
**/
		
	}
	

	public IModel<User> getSessionUserModel() {
		if (getSessionUser().isEmpty())
			return null;
		return this.sessionUserModel;
	}

	/** Session metadata key used to log the sign-in only once per session. */
	private static final org.apache.wicket.MetaDataKey<Boolean> SIGNIN_LOGGED = new org.apache.wicket.MetaDataKey<Boolean>() {
		private static final long serialVersionUID = 1L;
	};

	/**
	 * Logs the sign-in of the given user (once per session) via the
	 * {@link StatDBService}.
	 */
	protected void logSignin(User user) {
		try {
			org.apache.wicket.Session session = org.apache.wicket.Session.get();

			if (Boolean.TRUE.equals(session.getMetaData(SIGNIN_LOGGED)))
				return;

			// A temporary (unbound) session has no id yet -> bind it so getId() is not null
			if (session.isTemporary())
				session.bind();

			String userAgent = ((WebRequest) getRequest()).getHeader("User-Agent");
			getStatDBService().logSignin(user, session.getId(), userAgent);

			session.setMetaData(SIGNIN_LOGGED, Boolean.TRUE);

		} catch (Exception e) {
			logger.error(e);
		}
	}

	public StatDBService getStatDBService() {
		return (StatDBService) ServiceLocator.getInstance().getBean(StatDBService.class);
	}

		
	
	
	protected void setPageFonts(String s) {
		fonts = s;
	}

	protected String getPageFonts() {
		return fonts;
	}

	protected void setPageRobots(String s) {
		robots = s;
	}

	protected String getPageRobots() {
		return robots;
	}

	protected void setPageXUACompatible(String uax) {
		this.xuacompatible = uax;
	}

	protected String getPageXUACompatible() {
		return xuacompatible;
	}

	protected void setPageLanguage(String language) {
		this.language = language;
	}

	protected String getPageLanguage() {
		return this.language;
	}

	protected void setFavicon(String desc) {
		this.favicon = desc;
	}

	protected String getFavicon() {
		return favicon != null ? favicon : xfavicon;
	}

	protected void setPageKeywords(String desc) {
		this.keywords = desc;
	}

	protected String getPageKeywords() {
		return keywords;
	}
	
	
	protected void setCss(ResourceReference rcss) {
		this.rcss = rcss;
	}

	protected ResourceReference getCssResource() {
		return rcss;
	}
	
	
	
	protected StringResourceModel getLabel(String key) {
		return new StringResourceModel(key, this);
	}

	protected IModel<String> getLabel(String key, String... parameter) {
		StringResourceModel model = new StringResourceModel(key, this, null);
		model.setParameters((Object[]) parameter);
		return model;
	}
 
    protected BasePage(PageParameters parameters) {
        super(parameters);
    }

    /** Title shown at the left of the toolbar (e.g. the book title). */
    protected String getToolbarTitle() {
        return "Demo";
    }

    /** Subtitle shown below the toolbar title (e.g. the authors). */
    protected String getToolbarSubtitle() {
        return "";
    }

    /** Text shown at the right of the toolbar (e.g. the chapter). */
    protected String getToolbarRight() {
        return "";
    }

    /** Entries of the side menu. Subclasses may override. */
    protected List<MenuEntry> getMenuEntries() {
        List<MenuEntry> entries = new ArrayList<>();
        entries.add(MenuEntry.link("Home", "/home", true));
        entries.add(MenuEntry.link("Tesauro", "/tesauro", false));
        entries.add(MenuEntry.link("Comunidad", "/comunidad", false));
        entries.add(MenuEntry.link("Usuarios", "/users", false));
        entries.add(MenuEntry.link("Preferencias", "/usersettings", false));
        return entries;
    }
    
    
    public String getServerUrl() {
		String protocol = ((WebRequest) RequestCycle.get().getRequest()).getUrl().getProtocol();
		String host = ((WebRequest) RequestCycle.get().getRequest()).getUrl().getHost();
		Integer iport = ((WebRequest) RequestCycle.get().getRequest()).getUrl().getPort();
		String port = (iport.equals(80) || iport.equals(443) ? "" : (":" + iport.toString()));
		return protocol + "://" + host + port;
	}

	 
    
    
    @SuppressWarnings("unchecked")
	public void fireScanAll(UIEvent event) {
		for (UIEventListener<UIEvent> listener : getBehaviors(UIEventListener.class)) {
			if (listener.handle(event)) {
				listener.onEvent(event);
			}
		}
		fire(event, getPage().iterator(), false);
	}

	@SuppressWarnings("unchecked")
	public void fire(UIEvent event) {
		boolean handled = false;
		for (UIEventListener<UIEvent> listener : getPage().getBehaviors(UIEventListener.class)) {
			if (listener.handle(event)) {
				listener.onEvent(event);
				handled = true;
				break;
			}
		}
		if (!handled)
			fire(event, getPage().iterator());
	}

	public boolean fire(UIEvent event, Iterator<Component> components) {
		return fire(event, components, true);
	}

	@SuppressWarnings("unchecked")
	public boolean fire(UIEvent event, Iterator<Component> components, boolean stop_first_hit) {
		boolean handled = false;
		while (components.hasNext()) {
			Component component = components.next();
			for (UIEventListener<UIEvent> listener : component.getBehaviors(UIEventListener.class)) {
				if (listener.handle(event)) {
					listener.onEvent(event);
					if (stop_first_hit) {
						handled = true;
						break;
					}
				}
			}
			if (!handled) {
				if (component instanceof MarkupContainer) {
					handled = fire(event, ((MarkupContainer) component).iterator(), stop_first_hit);
				}
			} else {
				break;
			}
		}
		return handled;
	}
	
	
    
	public QueryDBService getQueryDBService() {
		return (QueryDBService) ServiceLocator.getInstance().getBean(QueryDBService.class);
	}
	
	public QueryFeedbackDBService getQueryFeedbackDBService() {
		return (QueryFeedbackDBService) ServiceLocator.getInstance().getBean(QueryFeedbackDBService.class);
	}


	public UserDBService getUserDBService() {
		return (UserDBService) ServiceLocator.getInstance().getBean(UserDBService.class);
	}
    
    
}
