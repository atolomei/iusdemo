
package io.demo.web.reports;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.wicketstuff.annotation.mount.MountPath;

import io.demo.Logger;
import io.demo.model.Role;
import io.demo.model.Stat;
import io.demo.model.User;
import io.demo.model.db.service.StatDBService;
import io.demo.service.DateTimeService;
import io.demo.service.ServiceLocator;
import io.demo.web.page.DemoBasePage;
import io.demo.web.panel.SimpleHeaderPanel;
import io.wktui.error.ErrorPanel;
import io.wktui.nav.breadcrumb.BreadCrumb;
import io.wktui.struct.list.ListPanel;

/**
 * Report of the recent user sign-ins, with a date range selector and a list of
 * the visits within the selected range.
 */
@MountPath("/reports/visits")
public class UsersVisitsPage extends DemoBasePage {

	private static final long serialVersionUID = 1L;

	static private Logger logger = Logger.getLogger(UsersVisitsPage.class.getName());

	static final DateTimeFormatter df = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

	private DateRange selectedRange = DateRange.LAST_7_DAYS;

	private WebMarkupContainer signinContainer;
	private ListPanel<VisitRow> signinListPanel;
	private List<VisitRow> rows;

	public UsersVisitsPage(PageParameters parameters) {
		super(parameters);
	}

	public DateRange getSelectedRange() {
		return selectedRange;
	}

	
	@Override
	public boolean canAccess(Optional<User> ouser) {
		
		if (ouser.isEmpty())
			return false;
		
		
		
		Role role = ouser.get().getRole();
		
		if (role==null)
			return false;
		
	
		return role== Role.SYSADMIN || role== Role.ADMIN;
		
		
	} 
	
	public void setSelectedRange(DateRange v) {
		this.selectedRange = v;
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		DropDownChoice<DateRange> rangeSelector = new DropDownChoice<DateRange>("signinRangeSelector", new PropertyModel<DateRange>(this, "selectedRange"), Arrays.asList(DateRange.values()), new IChoiceRenderer<DateRange>() {
			private static final long serialVersionUID = 1L;

			@Override
			public Object getDisplayValue(DateRange object) {
				return getString(object.getKey(), null, object.getKey());
			}

			@Override
			public String getIdValue(DateRange object, int index) {
				return object.name();
			}

			@Override
			public DateRange getObject(String id, IModel<? extends List<? extends DateRange>> choices) {
				return DateRange.valueOf(id);
			}
		});

		signinContainer = new WebMarkupContainer("signinContainer");
		signinContainer.setOutputMarkupId(true);
		add(signinContainer);

		rangeSelector.add(new AjaxFormComponentUpdatingBehavior("change") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				buildSigninList();
				target.add(signinContainer);
			}
		});
		add(rangeSelector);

		buildSigninList();
	}

	
	protected Panel createHeaderPanel() {
		try {

			BreadCrumb<Void> bc = createBreadCrumb();
			bc.addElement(new io.wktui.nav.breadcrumb.BCElement( Model.of("visitas")));
					

			SimpleHeaderPanel ph = new SimpleHeaderPanel("page-header", getOptionalSessionUserModel());
			ph.setBreadCrumb(bc);

			// bc.addElement(new BCElement(new
			// Model<String>(getModel().getObject().getDisplayname())));
			// JumboPageHeaderPanel<Candidate> ph = new
			// JumboPageHeaderPanel<Candidate>("page-header", getModel(), new
			// Model<String>(getModel().getObject().getDisplayname()));
			// ph.setHeaderCss("mb-0 pb-2 border-none");
			// ph.setIcon(Candidate.getIcon());
			// ph.setBreadCrumb(bc);
			// ph.setContext(getLabel("candidate"));
			// return (ph);

			return ph;

		} catch (Exception e) {
			logger.error(e);
			return new ErrorPanel("page-header", e);
		}
	}

	
	
	
	private void buildSigninList() {

		ZoneId zoneId = getZoneId();
		OffsetDateTime from = selectedRange.getFrom(zoneId);
		OffsetDateTime to = selectedRange.getTo(zoneId);

		List<Stat> signins = getStatDBService().getRecentSignins(from, to);

		
		// Keep only the most recent sign-in per user (query is already desc-ordered)
		Map<Long, Stat> latestByUser = new LinkedHashMap<>();
		for (Stat a : signins) {
			if (a.getUser() != null)
				latestByUser.putIfAbsent(a.getUser().getId(), a);
		}
				
		
		DateTimeService dts = getDateTimeService();
		rows = new ArrayList<>();

		
		for (Stat a : latestByUser.values()) {
			String name = a.getUser().getDisplayname();
			String ts   = dts.format(a.getTimestamp(), getSessionUser().get().getZoneId(), getSessionUser().get().getLocale());
			rows.add(new VisitRow(name, ts));
		}
		
		
			
		/**
		rows = new ArrayList<>();
		for (Stat v : signins) {
			String name = displayName(v.getUser());
			String ts = v.getTimestamp() != null ? df.format(v.getTimestamp().atZoneSameInstant(zoneId)) : "";
			rows.add(new VisitRow(name, ts));
		}
**/
		
		signinListPanel = new ListPanel<VisitRow>("signinList") {
			private static final long serialVersionUID = 1L;

			@Override
			public IModel<String> getItemLabel(IModel<VisitRow> model) {
				VisitRow row = model.getObject();
				return Model.of("<span class=\"float-start\">" + row.getName() + "</span><span class=\"float-end\">" + row.getTimestamp() + "</span>");
			}

			@Override
			public List<IModel<VisitRow>> getItems() {
				List<IModel<VisitRow>> models = new ArrayList<>();
				for (VisitRow row : rows)
					models.add(Model.of(row));
				return models;
			}
		};
		signinListPanel.setHasExpander(false);
		signinListPanel.setLiveSearch(false);
		signinListPanel.setSettings(false);
		signinContainer.addOrReplace(signinListPanel);
	}

	private String displayName(User user) {
		if (user == null)
			return "-";
		StringBuilder sb = new StringBuilder();
		if (user.getFirstName() != null)
			sb.append(user.getFirstName());
		if (user.getLasttName() != null) {
			if (sb.length() > 0)
				sb.append(" ");
			sb.append(user.getLasttName());
		}
		if (sb.length() == 0 && user.getName() != null)
			sb.append(user.getName());
		return sb.toString();
	}

	private ZoneId getZoneId() {
		try {
			if (getSessionUser().isPresent() && getSessionUser().get().getZoneId() != null)
				return ZoneId.of(getSessionUser().get().getZoneId());
		} catch (Exception e) {
			logger.error(e);
		}
		return ZoneId.systemDefault();
	}

	public StatDBService getStatDBService() {
		return (StatDBService) ServiceLocator.getInstance().getBean(StatDBService.class);
	}

	@Override
	protected String getToolbarTitle() {
		return "Buscador Juridico";
	}

	private static class VisitRow implements java.io.Serializable {
		private static final long serialVersionUID = 1L;
		private final String name;
		private final String timestamp;

		VisitRow(String name, String timestamp) {
			this.name = name;
			this.timestamp = timestamp;
		}

		public String getName() {
			return name;
		}

		public String getTimestamp() {
			return timestamp;
		}
	}
}
