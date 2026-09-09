package io.demo.web.panel;


import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.resource.UrlResourceReference;

import io.demo.model.Documento;
import io.wktui.event.UIEvent;
import io.wktui.panel.ListItemPanel;
import io.wktui.struct.list.ListPanel;
import io.wktui.struct.list.ListPanelMode;
import io.wktui.struct.list.ListPanelWicketEvent;
import wktui.base.InvisiblePanel;
import wktui.base.ModelPanel;

public abstract class DemoObjectListItemPanel<T extends Documento>  extends ModelPanel<T>  implements ListItemPanel<T> {

	private static final long serialVersionUID = 1L;

	private IModel<String> subtitle;
	private String icon;
	private Link<T> imageLink;
	private Image image;
	private ListPanelMode mode;
	private boolean imageVisible = true;
	private String titleIcon = null;
	private WebMarkupContainer imageContainer;
	private WebMarkupContainer titleTextContainer;
	private WebMarkupContainer textContainer;
	private WebMarkupContainer menu = null;

	
	public DemoObjectListItemPanel(String id, IModel<T> model, ListPanelMode mode) {
		super(id, model);
		
		this.mode = mode;
		this.imageVisible = (mode == ListPanelMode.TITLE_TEXT_IMAGE);
		
		
		this.subtitle = new Model<String>(model.getObject().getSubtitle());
	}
	
	@Override
	protected void addListeners() {
		super.addListeners();

		add(new io.wktui.event.WicketEventListener<ListPanelWicketEvent<T>>() {
			private static final long serialVersionUID = 1L;

			@Override
			public void onEvent(ListPanelWicketEvent<T> event) {
				if (isEqual(event.getModel().getObject(), DemoObjectListItemPanel.this.getModel().getObject())) {
					if (event.getName().equals(ListPanel.ITEM_EXPAND)) {
						DemoObjectListItemPanel.this.titleTextContainer.add(new AttributeModifier("class", getCss() + (event.isExpanded() ? " expanded " : "")));
						event.getTarget().add(DemoObjectListItemPanel.this.titleTextContainer);
					}
				}
			}

			@SuppressWarnings("unchecked")
			@Override
			public boolean handle(UIEvent event) {

				if (event instanceof ListPanelWicketEvent) {
					ListPanelWicketEvent<?> e = (ListPanelWicketEvent<?>) event;
					if (isEqual((T) e.getModel().getObject(), DemoObjectListItemPanel.this.getModel().getObject()))
						return true;
				}
				return false;
			}
		});
	}
	
	
 

	public IModel<String> getInfo() {
		return new Model<String>(getModel().getObject().toJSON());
	}
	
	public IModel<String> getObjectTitle() {
		return new Model<String>(getModel().getObject().getTitle());
	}
	
	@Override
	public boolean isEqual(T o1, T o2) {
		return o1.getId().equals(DemoObjectListItemPanel.this.getModel().getObject().getId());
	}

	public void setTutleIcon(String titleIcon) {
		this.titleIcon = titleIcon;
	}

	public String getTitleIcon() {
		return this.titleIcon;
	}

	protected String getIcon() {
		return icon;
	}

	public void setObjectSubtitle(IModel<String> subtitle) {
		this.subtitle = subtitle;
	}

	public boolean isImageVisible() {
		return imageVisible;
	}

	public void setImageVisible(boolean imageVisible) {
		this.imageVisible = imageVisible;
	}

	public boolean isMenuVisible() {
		return menu != null;
	}
	
	@Override	 
	public void onInitialize() {
		super.onInitialize();
	
		
		this.imageContainer = new WebMarkupContainer("imageContainer");
		this.imageContainer.add(new org.apache.wicket.AttributeModifier("class", getImageCss()));

		add(this.imageContainer);
		this.imageContainer.setVisible(isImageVisible());

		this.titleTextContainer = new WebMarkupContainer("titleTextContainer");
		add(this.titleTextContainer);
		this.titleTextContainer.setOutputMarkupId(true);

		this.titleTextContainer.add(new org.apache.wicket.AttributeModifier("class", getCss()));

		this.textContainer = new WebMarkupContainer("textContainer");
		this.titleTextContainer.add(this.textContainer);

		WebMarkupContainer menu = null;
		menu = getObjectMenu();
		if (menu == null)
			menu = new InvisiblePanel("menu");
		this.titleTextContainer.add(menu);

		this.imageLink = new Link<>("image-link", getModel()) {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick() {
				DemoObjectListItemPanel.this.onClick();
			}
		};

		this.imageContainer.add(imageLink);

		Link<T> titleLink = new Link<>("title-link", getModel()) {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick() {
				DemoObjectListItemPanel.this.onClick();
			}
		};

		if (getTitleLinkCss() != null)
			titleLink.add(new org.apache.wicket.AttributeModifier("class", getTitleLinkCss()));

		this.titleTextContainer.add(titleLink);

		Label title = new Label("title", getObjectTitle());
		title.setEscapeModelStrings(false);
		titleLink.add(title);

		WebMarkupContainer ti = new WebMarkupContainer("titleIcon") {
			private static final long serialVersionUID = 1L;

			public boolean isVisible() {
				return getTitleIcon() != null;
			}
		};

		ti.add(new org.apache.wicket.AttributeModifier("class", "float-none ps-3 pe-3 " + (getTitleIcon() != null ? getTitleIcon() : "")));
		this.titleTextContainer.add(ti);

		if (getObjectSubtitle() != null) {
			WebMarkupContainer subtitleContainer = new WebMarkupContainer("subtitle-container");
			titleTextContainer.addOrReplace(subtitleContainer);
			Label subtitleLabel = new Label("subtitle", getObjectSubtitle());
			
			subtitleLabel.setEscapeModelStrings(false);
			subtitleContainer.add(subtitleLabel);
		} else {
			titleTextContainer.addOrReplace(new InvisiblePanel("subtitle-container"));
		}

		Label text = new Label("text", getInfo());
		text.setEscapeModelStrings(false);
		this.textContainer.add(text);
		this.textContainer.setVisible(this.mode != ListPanelMode.TITLE);
		
		if (getIcon() != null) {
			WebMarkupContainer ic = new WebMarkupContainer("icon");
			ic.add(new org.apache.wicket.AttributeModifier("class", getIcon()));
			this.imageLink.addOrReplace(ic);

		} else {
			this.imageLink.addOrReplace(new InvisiblePanel("icon"));
		}

		if (isImageVisible()) {
			
			String imageSrc = getImageSrc();

			if (imageSrc != null) {
				Url url = Url.parse(imageSrc);
				UrlResourceReference resourceReference = new UrlResourceReference(url);
				this.image = new Image("image", resourceReference);

				this.imageLink.addOrReplace(image);

			} else {
				this.imageLink.addOrReplace(new InvisiblePanel("image"));
				this.imageLink.setVisible(false);
			}

			WebMarkupContainer noimage = new WebMarkupContainer("noimage") {
				private static final long serialVersionUID = 1L;

				public boolean isVisible() {
					return !imageLink.isVisible();
				}
			};
			this.imageContainer.addOrReplace(noimage);
		}
	
	
	
	
	
	
	
	}
	 
	


	
	public WebMarkupContainer getObjectMenu() {
		return null;
	}


	protected IModel<String> getObjectSubtitle() {
		return subtitle;
	}

	protected String getTitleLinkCss() {
		return "title-link";
	}

	public void onClick() {

	}

	public String getImageSrc() {
		return null;
	}

	protected String getImageCss() {

		StringBuilder str = new StringBuilder();
		str.append("bg-body-tertiary border rounded-0 col-xxl-2  col-xl-2  col-lg-3  col-md-4 col-sm-12 col-xs-12 text-lg-start text-md-start text-xs-center");

		if (this.isMenuVisible()) {
			str.append(" ismenu");
		}
		return str.toString();
	}

	protected String getCss() {
		StringBuilder str = new StringBuilder();
		if (isImageVisible())
			str.append("mt-2 mt-md-0 mt-xl-0 mt-lg-0 mt-xxl-0 col-xxl-10  col-xl-10  col-lg-9  col-md-8 col-sm-12 text-lg-start text-md-start text-xs-center");
		else
			str.append("col-xxl-12 col-xl-12 col-lg-12 col-md-12 col-sm-12 col-xs-12 text-lg-start text-md-start text-xs-center");
		if (this.isMenuVisible()) {
			str.append(" ismenu");
		}
		return str.toString();
	}
	
	
	
	
}
