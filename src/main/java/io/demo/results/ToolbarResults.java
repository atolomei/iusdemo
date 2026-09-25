package io.demo.results;

import java.util.Arrays;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

import io.demo.model.User;
import io.demo.web.event.DateRangeEvent;
import io.demo.web.event.OrderOptionEvent;
import io.demo.web.event.QueryAnalysisEvent;
import io.demo.web.event.SubjectOptionEvent;
import io.demo.web.event.TotalOptionEvent;
import wktui.base.ModelPanel;
import wktui.base.NumberFormatter;

/**
 * Toolbar of the results panel. Displays the selectors (order, date range,
 * subject, max results) and the total number of results.
 *
 * Each selector fires its own {@link io.wktui.event.WicketAjaxEvent} subclass
 * when the user changes the selection.
 */
public class ToolbarResults extends ModelPanel<User> {

	private static final long serialVersionUID = 1L;

	private WebMarkupContainer container;

	/** current selections (defaults can be changed via the setters) */
	private OrderOption orderOption = OrderOption.getDefault();
	private DateRange dateRange = DateRange.getDefault();
	private SubjectOption subjectOption = SubjectOption.getDefault();
	private TotalOption totalOption = TotalOption.getDefault();
	private ReasoningEffortOption reasoningEffortOption = ReasoningEffortOption.getDefault();

	/** total number of results displayed on the right of the toolbar */
	private Integer total;

	private Label totalLabel;

	public ToolbarResults(String id, IModel<User> model) {
		super(id, model);
	}

	// --- default / current values -------------------------------------

	public void setOrderOption(OrderOption option) {
		this.orderOption = option;
	}

	public OrderOption getOrderOption() {
		return this.orderOption;
	}

	public void setDateRange(DateRange option) {
		this.dateRange = option;
	}

	public DateRange getDateRange() {
		return this.dateRange;
	}

	public void setSubjectOption(SubjectOption option) {
		this.subjectOption = option;
	}

	public SubjectOption getSubjectOption() {
		return this.subjectOption;
	}

	public void setTotalOption(TotalOption option) {
		this.totalOption = option;
	}

	public TotalOption getTotalOption() {
		return this.totalOption;
	}

	public void setReasoningEffortOption(ReasoningEffortOption option) {
		this.reasoningEffortOption = option;
	}

	public ReasoningEffortOption getReasoningEffortOption() {
		return this.reasoningEffortOption;
	}

	// --- total --------------------------------------------------------

	public void setTotal(Integer total) {
		this.total = total;
	}

	public Integer getTotal() {
		return this.total;
	}

	// -------------------------------------------------------------------

	@Override
	public void onInitialize() {
		super.onInitialize();

		container = new WebMarkupContainer("container");
		container.setOutputMarkupId(true);
		add(container);

		// order selector
		DropDownChoice<OrderOption> order = new DropDownChoice<OrderOption>("order",
				new PropertyModel<OrderOption>(this, "orderOption"),
				Arrays.asList(OrderOption.values()),
				new ChoiceRenderer<OrderOption>("label"));

		order.add(new AjaxFormComponentUpdatingBehavior("change") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				fireScanAll(new OrderOptionEvent(getOrderOption(), target));
			}
		});
		container.add(order);

		// date range selector (read-only: value comes from the SearchForm)
		DropDownChoice<DateRange> dates = new DropDownChoice<DateRange>("daterange",
				new PropertyModel<DateRange>(this, "dateRange"),
				Arrays.asList(DateRange.values()),
				new ChoiceRenderer<DateRange>("label")) {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isEnabled() {
				return false;
			}
		};
		container.add(dates);

		// subject selector (read-only: value comes from the SearchForm)
		DropDownChoice<SubjectOption> subject = new DropDownChoice<SubjectOption>("subject",
				new PropertyModel<SubjectOption>(this, "subjectOption"),
				Arrays.asList(SubjectOption.values()),
				new ChoiceRenderer<SubjectOption>("label")) {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isEnabled() {
				return false;
			}
		};
		container.add(subject);

		// reasoning level selector (read-only: value comes from the SearchForm)
		DropDownChoice<ReasoningEffortOption> reasoning = new DropDownChoice<ReasoningEffortOption>("reasoninglevel",
				new PropertyModel<ReasoningEffortOption>(this, "reasoningEffortOption"),
				Arrays.asList(ReasoningEffortOption.values()),
				new ChoiceRenderer<ReasoningEffortOption>("label")) {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isEnabled() {
				return false;
			}
		};
		container.add(reasoning);

		// "Análisis" button: fires the QueryAnalysisEvent so the ResultsPanel
		// displays the general analysis of the query
		container.add(new org.apache.wicket.ajax.markup.html.AjaxLink<Void>("analysis") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				fireScanAll(new QueryAnalysisEvent(target));
			}
		});
/**
		// max results selector (read-only: value comes from the SearchForm)
		DropDownChoice<TotalOption> max = new DropDownChoice<TotalOption>("maxresults",
				new PropertyModel<TotalOption>(this, "totalOption"),
				Arrays.asList(TotalOption.values()),
				new ChoiceRenderer<TotalOption>("label")) {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isEnabled() {
				return false;
			}

			@Override
			public boolean isVisible() {
				return false;
			}

			
		};
		container.add(max);
		*
		*/
		

		// total number of results (like ListPanelToolbar)
		this.totalLabel = new Label("total", new Model<String>() {
			private static final long serialVersionUID = 1L;

			@Override
			public String getObject() {
				return (getTotal() != null) ? NumberFormatter.formatNumber(getTotal()) : "";
			}
		}) {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isVisible() {
				return getTotal() != null;
			}
		};
		container.add(this.totalLabel);
	}
}