package io.demo.web.page;

import java.io.Serializable;

/**
 * A single entry of the side menu (mirrors the menu entries used by the
 * static site generator template page.ftlh).
 */
public class MenuEntry implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String title;
    private final String href;
    private final boolean header;
    private final boolean child;
    private final boolean current;

    private MenuEntry(String title, String href, boolean header, boolean child, boolean current) {
        this.title = title;
        this.href = href;
        this.header = header;
        this.child = child;
        this.current = current;
    }

    public static MenuEntry header(String title) {
        return new MenuEntry(title, null, true, false, false);
    }

    public static MenuEntry link(String title, String href, boolean current) {
        return new MenuEntry(title, href, false, false, current);
    }

    public static MenuEntry child(String title, String href, boolean current) {
        return new MenuEntry(title, href, false, true, current);
    }

    public String getTitle()    { return title; }
    public String getHref()     { return href; }
    public boolean isHeader()   { return header; }
    public boolean isChild()    { return child; }
    public boolean isCurrent()  { return current; }
}
