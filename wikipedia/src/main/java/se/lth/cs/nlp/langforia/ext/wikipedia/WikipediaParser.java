/**
 *  This file is part of Langforia.
 *
 *  Langforia is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Langforia is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Langforia.  If not, see <http://www.gnu.org/licenses/>.
 */
package se.lth.cs.nlp.langforia.ext.wikipedia;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import se.lth.cs.docforia.DocumentFactory;
import se.lth.cs.docforia.graph.hypertext.Anchor;
import se.lth.cs.docforia.graph.hypertext.ListItem;
import se.lth.cs.docforia.graph.hypertext.ListSection;
import se.lth.cs.docforia.graph.outline.Heading;
import se.lth.cs.docforia.graph.outline.Section;
import se.lth.cs.docforia.graph.text.Paragraph;
import se.lth.cs.docforia.memstore.MemoryDocumentFactory;
import se.lth.cs.docforia.query.*;
import se.lth.cs.docforia.DynamicNode;

import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Wikipedia HTML parser
 *
 * <p><b>Remarks:</b> Parse method is threadsafe.</p>
 */
public class WikipediaParser {
    private final DocumentFactory factory;
    private final String lang;
    private final Mode mode;

    public enum Mode {
        /* XOWA converted Wikipedia */
        XOWA,
        /** HTML dumped Wikipedia */
        KIWIX,
        /** The real version */
        WIKIPEDIA
    }

    public WikipediaParser(String lang, DocumentFactory factory, Mode mode) {
        this.lang = lang;
        this.factory = factory;
        this.mode = mode;
    }

    public se.lth.cs.docforia.Document parse(String title, String html) {
        return parse(title, Jsoup.parse(html));
    }

    private static class TextBuilder {
        public final int offsetStart;
        public final StringBuilder sb;

        public TextBuilder(int offsetStart) {
            this.offsetStart = offsetStart;
            this.sb = new StringBuilder();
        }

        public int start() {
            return offsetStart;
        }

        public int end() {
            return offsetStart + sb.length();
        }
    }

    private static class InfoboxItem {
        private Document key;
        private Document value;
    }

    private static class GlobalContext {
        public final ArrayList<se.lth.cs.docforia.Document> infoboxEntries = new ArrayList<>();
    }

    private class ExtractionContext {
        public final GlobalContext parent;
        private boolean headerActive = false;
        private String headerTitle = "__ABSTRACT__";

        public void setHeader(String header) {
            headerActive = true;
            headerTitle = header;
        }

        public void flushHeader() {
            if(headerActive) {
                mark();
                append(headerTitle);
                heading();
                headerActive = false;
            }
        }

        private final ArrayDeque<Boolean> noparsStack = new ArrayDeque<>();
        private final ArrayDeque<Boolean> ignoreInfoboxStack = new ArrayDeque<>();
        private final ArrayDeque<Integer> markers = new ArrayDeque<>();
        private final ArrayDeque<TextBuilder> sbs = new ArrayDeque<>();
        private final se.lth.cs.docforia.Document doc;
        private int offset = 0;

        public boolean ignoreInfobox = false;
        public boolean nopars = false;
        public boolean intable = false;

        public ExtractionContext(GlobalContext parent, se.lth.cs.docforia.Document doc) {
            this.parent = parent;
            this.doc = doc;
            this.noparsStack.push(false);
            this.ignoreInfoboxStack.push(false);
        }

        public void push() {
            if(sbs.size() == 0)
                offset = 0;
            else
                offset = sbs.getFirst().offsetStart + sbs.getFirst().sb.length();

            sbs.push(new TextBuilder(offset));
            noparsStack.push(nopars);
            ignoreInfoboxStack.push(ignoreInfobox);
        }

        public void append(String text) {
            sbs.getFirst().sb.append(text);
        }

        public void mark() {
            if(sbs.size() == 0)
                markers.push(0);
            else
                markers.push(sbs.getFirst().end());
        }

        public int unmark() {
            return markers.pop();
        }

        public int markpeek() {
            return markers.getFirst();
        }

        public int offset() {
            return sbs.getFirst().end();
        }

        public void pop() {
            TextBuilder builder = sbs.pop();
            sbs.getFirst().sb.append(builder.sb);
            ignoreInfobox = ignoreInfoboxStack.pop();
            nopars = noparsStack.pop();
        }

        private void expand() {
            doc.setLength(sbs.getFirst().end());
        }

        public void section(String title) {
            int start = unmark();
            int end = offset();
            expand();
            new Section(doc).setTitle(title).setRange(start, end);
            mark();
        }

        public Heading heading() {
            int start = unmark();
            int end = offset();
            expand();
            return new Heading(doc).setHeading(headerTitle).setRange(start,end);
        }

        public DynamicNode clean() {
            int start = unmark();
            int end = offset();
            expand();
            return new DynamicNode(doc, "clean").setRange(start, end).putProperty("section", headerTitle);
        }

        public Paragraph paragraph() {
            pop();
            int start = unmark();
            int end = offset();
            expand();
            return new Paragraph(doc).setHeader(headerTitle).setRange(start,end);
        }

        public Anchor anchor(String type, String target, String title) {
            pop();
            int start = unmark();
            int end = offset();
            expand();
            if(mode == Mode.XOWA) {
                return new Anchor(doc).setTarget(type.equals("internal") ? WikipediaPages.toUri(lang, target) : target)
                                      .setType(type)
                                      .setRange(start, end)
                                      .putProperty("title", title);
            } else {
                return new Anchor(doc).setTarget(type.equals("internal") ? ("urn:wikipedia:" + lang + ":" + target) : target)
                                      .setType(type)
                                      .setRange(start, end)
                                      .putProperty("title", title);
            }
        }

        public void strong() {
            pop();
            int start = unmark();
            int end = offset();
            expand();
            new DynamicNode(doc, "strong").setRange(start,end);
        }

        public void italic() {
            pop();
            int start = unmark();
            int end = offset();
            expand();
            new DynamicNode(doc, "italic").setRange(start, end);
        }
    }

    private void paragraph(final ExtractionContext context, final Element elem) {
        if(!context.nopars) {
            context.push();
            context.mark();
        }

        context.flushHeader();

        if(!context.nopars) {
            context.mark();
        }

        iterate(context, elem);

        if(!context.nopars) {
            context.clean();
            context.paragraph();
            context.append("\n\n");
        }
    }

    private static final Pattern protocolpat = Pattern.compile("([a-zA-Z]+\\:)?\\/\\/.+");
    private static final Pattern wikipedia = Pattern.compile("(?:(\\.\\/)?(.+)#(.+))?|(\\.\\/)?(.+)");

    private void anchor(final ExtractionContext context, final Element elem) {
        context.push();
        context.mark();
        iterate(context, elem);
        if(context.sbs.getFirst().sb.toString().trim().isEmpty()) {
            context.pop();
            context.unmark(); //skip
        }
        else {
            Matcher m;
            if(elem.attr("href").startsWith("#")) {
                context.pop();
                context.unmark();
            } else if(protocolpat.matcher(elem.attr("href")).matches()) {
                context.anchor("external", elem.attr("href"), elem.attr("title"));
            }  else if(mode == Mode.XOWA && elem.attr("href").startsWith("/wiki/")) {
                context.anchor("internal", elem.attr("href").substring(6), elem.attr("title"));
            } else if(mode == Mode.KIWIX && elem.attr("href").endsWith(".html")) {
                try {
                    String target = URLDecoder.decode(elem.attr("href"), "UTF-8");
                    context.anchor("internal", target.substring(0, target.length() - 5), elem.attr("title"));
                } catch (UnsupportedEncodingException e) {
                    throw new IOError(e);
                }
            } else if(mode == Mode.WIKIPEDIA && (m = wikipedia.matcher(elem.attr("href"))).matches()) {
                String target = m.group(2) != null ? m.group(2) : m.group(5);
                String part = m.group(3);

                Anchor anchor = context.anchor("internal", target, elem.attr("title"));
                if(part != null) {
                    anchor.putProperty("section", part);
                }
            } else {
                context.pop();
                context.unmark();
            }
        }
    }

    private void list(final ExtractionContext context, final Element elem) {
        Iterator<Element> li = elem.children().iterator();

        boolean reference = elem.hasClass("references");

        context.push();
        context.mark();
        context.nopars = true;
        int majorstart = context.offset();

        context.flushHeader();

        while(li.hasNext()) {
            int start = context.offset();
            Element lit = li.next();
            iterate(context, lit);

            StringBuilder sb = trimEnd(context);
            char lastchar = sb.length() > 0 ? sb.charAt(sb.length()-1) : 0;

            if(!(lastchar == '.' || lastchar == ',' || lastchar == ';')) {
                context.append(". ");
            }
            int end = context.offset();
            context.doc.setLength(end);
            if(end > start) {
                ListItem annoli = new ListItem(context.doc).setRange(start, end);
                if(reference) {
                    annoli.putProperty("reference", true);
                }
            }
        }

        boolean nopars = context.noparsStack.pop();
        context.noparsStack.push(nopars);

        if(!nopars) {
            context.paragraph().putProperty("source", "list");
            context.append("\n\n");
        }
        else {
            context.pop();
            context.unmark();
        }

        int majorend = context.offset();

        context.doc.setLength(majorend);
        if(majorend > majorstart) {
            ListSection annols = new ListSection(context.doc).setRange(majorstart, majorend);
            if(reference) {
                annols.putProperty("reference", true);
            }
        }
    }

    private StringBuilder trimEnd(final ExtractionContext context) {
        StringBuilder sb = context.sbs.getFirst().sb;
        while(sb.length() > 0 && sb.charAt(sb.length()-1) == ' ') {
            sb.deleteCharAt(sb.length()-1);
        }
        return sb;
    }

    private StringBuilder trimCommaEnd(final ExtractionContext context) {
        StringBuilder sb = context.sbs.getFirst().sb;
        while(sb.length() > 0 && sb.charAt(sb.length()-1) == ',') {
            sb.deleteCharAt(sb.length()-1);
        }
        return sb;
    }

    private void tableSelector(final ExtractionContext context, final Element elem) {
        switch(elem.tagName()) {
            case "table":
            case "tbody":
            case "thead":
                for (Element element : elem.children()) {
                    tableSelector(context, element);
                }
                break;
            case "tr": {
                for (Element element : elem.children()) {
                    tableSelector(context, element);
                }

                trimEnd(context);
                StringBuilder sb = trimCommaEnd(context);
                char lastchar = sb.length() > 0 ? sb.charAt(sb.length() - 1) : 0;

                if (!(lastchar == '.' || lastchar == ',' || lastchar == ';' || sb.length() == 0)) {
                    context.append(". ");
                }
                break;
            }
            case "caption":
            case "td":
            case "th": {
                iterate(context, elem);

                StringBuilder sb = trimEnd(context);
                char lastchar = sb.length() > 0 ? sb.charAt(sb.length() - 1) : 0;

                if (!(lastchar == '.' || lastchar == ',' || lastchar == ';' || sb.length() == 0)) {
                    context.append(", ");
                }
                break;
            }
        }
    }

    private void table(final ExtractionContext context, final Element elem) {
        context.push();
        context.mark();
        context.nopars = true;
        context.intable = true;

        context.flushHeader();

        tableSelector(context, elem);

        boolean nopars = context.noparsStack.pop();
        context.noparsStack.push(nopars);

        context.intable = false;
        if(!nopars) {
            if(context.sbs.getFirst().sb.length() != 0) {
                context.paragraph().putProperty("source", "table");
                context.append("\n\n");
            }
            else {
                context.pop();
                context.unmark();
            }
        }
        else {
            context.pop();
            context.unmark();
        }
    }

    private static final Pattern trimInfoBoxStart = Pattern.compile("^[ ]+");
    private static final Pattern trimInfoBoxEnd = Pattern.compile("[ ]+$");

    private static final Pattern endsOnDot = Pattern.compile("\\.\\s*$");

    private void infobox(final ExtractionContext context, final Element root) {
        //Variants:
        //table (tbody) tr -> th, td
        //table (tbody) tr -> td, td

        for (Element element : root.select("tr")) {
            Elements tds = element.select("th, td");
            if(tds.size() == 2) {
                if(tds.get(0).tagName().equals("th") && tds.get(1).tagName().equals("td")) {
                    //Extract key
                    ExtractionContext keyextract = new ExtractionContext(context.parent, context.doc.newInstance("key", ""));
                    keyextract.push();
                    keyextract.ignoreInfobox = true;
                    keyextract.nopars = true;
                    text(keyextract, tds.get(0));
                    keyextract.doc.setText(keyextract.sbs.getFirst().sb.toString());

                    //Mark key
                    DynamicNode key = new DynamicNode(keyextract.doc, "key").setRange(keyextract.doc);
                    keyextract.doc.replace(trimInfoBoxStart, "", false);
                    keyextract.doc.replace(trimInfoBoxEnd, "", false);
                    if(endsOnDot.matcher(keyextract.doc.getText()).find()) {
                        keyextract.doc.setText(keyextract.doc.getText() + "\n");
                    } else {
                        keyextract.doc.setText(keyextract.doc.getText() + ".\n");
                    }

                    //Extract value
                    ExtractionContext valueextract = new ExtractionContext(context.parent, context.doc.newInstance("value", ""));
                    valueextract.push();
                    valueextract.ignoreInfobox = true;
                    valueextract.nopars = true;
                    text(valueextract, tds.get(1));
                    valueextract.doc.setText(valueextract.sbs.getFirst().sb.toString());

                    //Mark value
                    DynamicNode value = new DynamicNode(valueextract.doc, "value").setRange(valueextract.doc);
                    valueextract.doc.replace(trimInfoBoxStart, "", false);
                    valueextract.doc.replace(trimInfoBoxEnd, "", false);

                    if(endsOnDot.matcher(valueextract.doc.getText()).find()) {
                        valueextract.doc.setText(valueextract.doc.getText() + "\n\n");
                    } else {
                        valueextract.doc.setText(valueextract.doc.getText() + ".\n\n");
                    }

                    //Concat value to key and form an entry.
                    keyextract.doc.append(valueextract.doc);
                    DynamicNode entryNode = new DynamicNode(keyextract.doc, "entry").setRange(keyextract.doc);
                    new Paragraph(keyextract.doc).setRange(keyextract.doc);
                    if(element.attr("title").length() > 0) {
                        entryNode.putProperty("title", element.attr("title"));
                    }
                    entryNode.putProperty("type", "1");

                    if(key.length() != 0 && value.length() != 0) {
                        context.parent.infoboxEntries.add(keyextract.doc);
                    }
                }
                else if(tds.get(0).tagName().equals("td") && tds.get(1).tagName().equals("td")) {
                    //Extract key
                    ExtractionContext keyextract = new ExtractionContext(context.parent, context.doc.newInstance("key", ""));
                    keyextract.push();
                    keyextract.ignoreInfobox = true;
                    keyextract.nopars = true;
                    text(keyextract, tds.get(0));
                    keyextract.doc.setText(keyextract.sbs.getFirst().sb.toString());

                    //Mark key
                    DynamicNode key = new DynamicNode(keyextract.doc, "key").setRange(keyextract.doc);
                    keyextract.doc.replace(trimInfoBoxStart, "", false);
                    keyextract.doc.replace(trimInfoBoxEnd, "", false);
                    if(endsOnDot.matcher(keyextract.doc.getText()).find()) {
                        keyextract.doc.setText(keyextract.doc.getText() + "\n");
                    } else {
                        keyextract.doc.setText(keyextract.doc.getText() + ".\n");
                    }

                    //Extract value
                    ExtractionContext valueextract = new ExtractionContext(context.parent, context.doc.newInstance("value", ""));
                    valueextract.push();
                    valueextract.ignoreInfobox = true;
                    valueextract.nopars = true;
                    text(valueextract, tds.get(1));
                    valueextract.doc.setText(valueextract.sbs.getFirst().sb.toString());

                    //Mark value
                    DynamicNode value = new DynamicNode(valueextract.doc, "value").setRange(valueextract.doc);
                    valueextract.doc.replace(trimInfoBoxStart, "", false);
                    valueextract.doc.replace(trimInfoBoxEnd, "", false);
                    if(endsOnDot.matcher(valueextract.doc.getText()).find()) {
                        valueextract.doc.setText(valueextract.doc.getText() + "\n\n");
                    } else {
                        valueextract.doc.setText(valueextract.doc.getText() + ".\n\n");
                    }

                    //Concat value to key and form an entry.
                    keyextract.doc.append(valueextract.doc);
                    DynamicNode entryNode = new DynamicNode(keyextract.doc, "entry").setRange(keyextract.doc);
                    new Paragraph(keyextract.doc).setRange(keyextract.doc);
                    if(element.attr("title").length() > 0) {
                        entryNode.putProperty("title", element.attr("title"));
                    }

                    entryNode.putProperty("type", "2");

                    if(key.length() != 0 && value.length() != 0) {
                        context.parent.infoboxEntries.add(keyextract.doc);
                    }
                }
            }
        }
    }

    private void structureSelector(final ExtractionContext context, final Node elem, int depth) {
        if(elem instanceof Element) {
            Element el = (Element)elem;
            if(el.attr("class").contains("infobox") && !context.ignoreInfobox) {
                infobox(context, el);
            }
            else {
                if(el.tagName().equals("ol") || el.tagName().equals("ul")) {
                    list(context, el);
                }
                else if(el.tagName().equals("p")) {
                    paragraph(context, el);
                }
                else if(el.tagName().equals("table")) {
                    table(context, el);
                } else if(el.tagName().equals("sup")) {

                } else {
                    if(depth == 0) {
                        context.push();
                        context.mark();
                        context.nopars = true;
                    }

                    iterate(context, el);

                    if(depth == 0) {
                        context.paragraph().putProperty("source", "unknown");
                        context.append("\n\n");
                    }
                }
            }
        }
    }

    private void iterate(final ExtractionContext context, final Node elem) {
        for (Node node : elem.childNodes()) {
            text(context, node);
        }
    }

    private static final Pattern matchHeaders = Pattern.compile("h[1-5]", Pattern.CASE_INSENSITIVE);

    private void text(final ExtractionContext context, final Node elem) {
        if(elem instanceof TextNode) {
            context.append(((TextNode) elem).text().replace((char)160,(char)32));
        }
        else if(elem instanceof Element) {
            Element el = (Element)elem;
            switch(el.tagName()) {
                case "a":
                    anchor(context, el);
                    break;
                case "strong":
                case "b":
                    context.push();
                    context.mark();
                    iterate(context, elem);
                    context.strong();
                    break;
                case "em":
                case "i":
                    context.push();
                    context.mark();
                    iterate(context, elem);
                    context.italic();
                    break;
                case "span":
                    if(!el.attr("style").equals("display:none")) {
                        iterate(context, el);
                    } else {
                        context.append(" ");
                    }
                    break;
                case "div":
                    iterate(context, el);
                    break;
                case "table":
                    if(context.intable)
                        tableSelector(context, el);
                    else
                        table(context, el);
                    break;
                case "ol":
                case "ul":
                    list(context, el);
                    break;
                case "br":
                    context.append(" ");
                    break;
                case "sup":
                    //Ignore sups
                    break;
                default:
                    iterate(context, el);
                    break;
            }
        }
    }

    private static final Pattern Clean1 = Pattern.compile("(?<=\\n|^)[ \t]+");
    private static final Pattern Clean2 = Pattern.compile("(?<=\\n\\n)[\n]+");
    private static final Pattern Clean3 = Pattern.compile("[ \t]+(?=\\n)");
    private static final Pattern Clean4 = Pattern.compile("[ \t]+");

    private se.lth.cs.docforia.Document page(String title, final Element root) {
        se.lth.cs.docforia.Document rec = factory.create(WikipediaPages.toUri(lang, title)).setLanguage(lang);
        //Record rec = factory.create(WikipediaPages.toUri(lang, title), lang);
        GlobalContext globalContext = new GlobalContext();
        ExtractionContext context = new ExtractionContext(globalContext, rec);
        boolean sectionActive = true;
        String sectionTitle = "__ABSTRACT__";
        context.push();
        context.mark();

        boolean firstParagraphFound = false;

        for (Element el : root.children()) {
            if(firstParagraphFound || el.tagName().equals("p")) {
                firstParagraphFound = true;
                if(el.tagName().length() == 2 && el.tagName().startsWith("h") && Character.isDigit(el.tagName().charAt(1))) {
                    if(context.offset() - context.markpeek() > 0) {
                        context.section(sectionTitle);
                    }

                    String header = el.text().trim();
                    sectionTitle = header;

                    context.setHeader(header + ". ");
                }
                else {
                    structureSelector(context, el, 0);
                }
            }
            else if(el.attr("class").contains("infobox")) {
                infobox(context, el);
            }
        }

        if(context.offset() - context.markpeek() > 0) {
            context.section(sectionTitle);
        }

        context.doc.setText(context.sbs.peek().sb.toString());
        if(!globalContext.infoboxEntries.isEmpty()) {
            se.lth.cs.docforia.Document infoboxes = rec.factory().create();
            infoboxes.append(globalContext.infoboxEntries);
            rec.putProperty("wiki:infobox", infoboxes);

            infoboxes.replace(Clean1, "");
            infoboxes.replace(Clean2, "");
            infoboxes.replace(Clean3, "");
            infoboxes.replace(Clean4, " ");
        }

        rec.replace(Clean1, "");
        rec.replace(Clean2, "");
        rec.replace(Clean3, "");
        rec.replace(Clean4, " ");
        return rec;
    }

    private static class Category {
        public String title;
        public String href;
    }

    public se.lth.cs.docforia.Document parse(String title, Document doc) {
        //Simple filtering
        Element elem = doc.select(mode == Mode.WIKIPEDIA ? ".mw-body-content" : "#mw-content-text").first();
        if(elem != null) {
            ArrayList<Category> categories = new ArrayList<>();

            Elements catlinks = doc.select("#catlinks a");
            if (catlinks.size() > 0) {
                for (Element category : catlinks) {
                    Category cat = new Category();
                    cat.title = category.text();
                    cat.href = "urn:wikipedia:" + lang + ":" + WikipediaPages.formatLabel( category.attr("href").substring(6), false);
                    categories.add(cat);
                }
            }

            elem.select("#toc, .mw-cite-backlink, .hatnote, .noprint, .navbox, .mw-reflink-text, #catlinks, div.thumb, img, p:has(#coordinates), .xowa-timeline, #xowa_math_txt_0, .visualClear").remove();

            //Anchor + text collapse
            List<Node> els = elem.childNodes();
            IntList makeParagraphs = new IntArrayList();

            for(int i = 0; i < els.size(); i++) {
                if((els.get(i) instanceof TextNode  && ((TextNode) els.get(i)).text().trim().length() != 0)
                        || (els.get(i) instanceof  Element && ((Element) els.get(i)).tagName().equals("a"))) {

                    int k = i;
                    for(; k < els.size(); k++) {
                        if(els.get(k) instanceof Element) {
                            Element lel = (Element)els.get(k);
                            if(!lel.tagName().equals("a"))
                                break;
                        }
                    }

                    if(k-i > 1) {
                        makeParagraphs.add(i);
                        makeParagraphs.add(k);
                    }

                    i = k-1;
                }
            }

            if(makeParagraphs.size() != 0) {
                StringBuilder newHtml = new StringBuilder();
                newHtml.append("<div>");
                for(int i = 0, k = 0; i < els.size(); i++) {
                    if(k*2 < makeParagraphs.size() && makeParagraphs.get(k*2) == i) {
                        newHtml.append("<p>");
                        final int start = makeParagraphs.get(k*2);
                        final int end = makeParagraphs.get(k*2+1);
                        for(int h = start; h < end; h++) {
                            newHtml.append(els.get(h));
                        }
                        newHtml.append("</p>");
                        k++;
                        i = end - 1;
                    }
                    else {
                        newHtml.append(els.get(i).outerHtml());
                    }
                }
                newHtml.append("</div>");

                elem = Jsoup.parseBodyFragment(newHtml.toString()).select("div").first();
            }

            se.lth.cs.docforia.Document record = page(title, elem);

            if(elem.children().size() == 0) {
                record.putProperty("wikipedia:empty-page", "1");
            }

            if(doc.select("meta[property=mw:PageProp/disambiguation]").size() > 0) {
                record.putProperty("wiki:type", "DISAMBIGUATION");
            } else {
                record.putProperty("wiki:type", "ARTICLE");
            }

            if (categories.size() > 0) {
                StringBuilder categoryBuilder = new StringBuilder();
                se.lth.cs.docforia.Document categoryDoc = record.factory().create();
                //WikipediaRecords.createCategories(record);

                for (Category category : categories) {
                    int start = categoryBuilder.length();
                    categoryBuilder.append(category.title);
                    int end = categoryBuilder.length();
                    categoryDoc.setLength(end);
                    new Anchor(categoryDoc).setTarget(category.href).setType("internal").setRange(start,end);
                    categoryBuilder.append(".\n");
                }

                categoryDoc.setText(categoryBuilder.toString());
                record.putProperty("wiki:categories", categoryDoc);
            }

            return record;
        }
        else
            return factory.create(WikipediaPages.toUri(lang, title), lang);
    }
}
