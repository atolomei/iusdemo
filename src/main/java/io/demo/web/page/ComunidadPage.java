package io.demo.web.page;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.wicketstuff.annotation.mount.MountPath;

/**
 * Comunidad page of the demo del buscador juridico kbee. No login required.
 */
@MountPath("/comunidad")
public class ComunidadPage extends BasePage {

    private static final long serialVersionUID = 1L;

    public ComunidadPage(PageParameters parameters) {
        super(parameters);
        add(new Label("pageName", Model.of(ComunidadPage.class.getSimpleName())));
    }

    @Override
    protected String getToolbarTitle() {
        return "Buscador Juridico";
    }
}
