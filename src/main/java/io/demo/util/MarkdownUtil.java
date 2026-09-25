package io.demo.util;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataSet;
import com.vladsch.flexmark.parser.ParserEmulationProfile;

public class MarkdownUtil {

	private static final MutableDataSet OPTIONS = new MutableDataSet();

    private static final Parser PARSER =
            Parser.builder(OPTIONS).build();

    private static final HtmlRenderer RENDERER =
            HtmlRenderer.builder(OPTIONS).build();

    public static String markdownToHtml(String markdown) {
        var document = PARSER.parse(markdown);
        return RENDERER.render(document);
    }
}