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

		// date range selector
		DropDownChoice<DateRange> dates = new DropDownChoice<DateRange>("daterange",
				new PropertyModel<DateRange>(this, "dateRange"),
				Arrays.asList(DateRange.values()),
				new ChoiceRenderer<DateRange>("label"));

		dates.add(new AjaxFormComponentUpdatingBehavior("change") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				fireScanAll(new DateRangeEvent(getDateRange(), target));
			}
		});
		container.add(dates);

		// subject selector
		DropDownChoice<SubjectOption> subject = new DropDownChoice<SubjectOption>("subject",
				new PropertyModel<SubjectOption>(this, "subjectOption"),
				Arrays.asList(SubjectOption.values()),
				new ChoiceRenderer<SubjectOption>("label"));

		subject.add(new AjaxFormComponentUpdatingBehavior("change") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				fireScanAll(new SubjectOptionEvent(getSubjectOption(), target));
			}
		});
		container.add(subject);

		// max results selector
		DropDownChoice<TotalOption> max = new DropDownChoice<TotalOption>("maxresults",
				new PropertyModel<TotalOption>(this, "totalOption"),
				Arrays.asList(TotalOption.values()),
				new ChoiceRenderer<TotalOption>("label"));

		max.add(new AjaxFormComponentUpdatingBehavior("change") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				fireScanAll(new TotalOptionEvent(getTotalOption(), target));
			}
		});
		container.add(max);

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