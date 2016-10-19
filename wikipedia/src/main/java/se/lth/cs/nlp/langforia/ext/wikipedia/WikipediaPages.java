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

import se.lth.cs.docforia.Document;
import se.lth.cs.docforia.DynamicNode;
import se.lth.cs.docforia.Node;
import se.lth.cs.docforia.graph.hypertext.Anchor;
import se.lth.cs.docforia.query.NodeTVar;
import se.lth.cs.docforia.query.NodeVar;

import java.io.IOError;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Wikipedia page name utilities
 */
public class WikipediaPages {
    private static final Pattern matchUriUtf8 = Pattern.compile("((%[0-9A-Fa-f]{2})+)", Pattern.CASE_INSENSITIVE);

    /**
     * Convert a wikipedia page title to an normalized Uri
     * @param lang  the language of the page
     * @param title the page title
     * @return normalized uri
     */
    public static String toUri(String lang, String title) {
        String internalResult = formatUriInternal(lang, title, false);
        if(internalResult.codePointCount(0,internalResult.length()) < internalResult.length()) {
            //System.out.println("Found something odd = " + internalResult);
            try {
                return formatUriInternal(lang, URLEncoder.encode(title, "UTF-8"), true);
            } catch (UnsupportedEncodingException e) {
                throw new IOError(e);
            }
        }
        return internalResult;
    }

    private static String formatUriInternal(String lang, String title, boolean odd)
    {
        int section;
        if((section = title.lastIndexOf('#')) != -1) {
            title = title.substring(0, section);
        }

        if(title.startsWith(":")) {
            String formatted = formatLabel(title, odd);
            if(formatted.length() == 0)
                return "urn:wikipedia:" + lang + ":special:__EMPTY__";
            else
                return "urn:wikipedia:" + lang + ":interwiki:" + formatted;
        } else {
            String formatted = formatLabel(title, odd);
            if(formatted.length() == 0)
                return "urn:wikipedia:" + lang + ":__EMPTY__";
            else
                return "urn:wikipedia:" + lang + ":" + formatted;
        }
    }

    private static String decodeParts(String parts) {
        StringBuilder sb = new StringBuilder();
        Matcher matcher = matchUriUtf8.matcher(parts);
        int last = 0;
        while(matcher.find()) {
            if(last != matcher.start()) {
                sb.append(parts, last, matcher.start());
            }

            String decoded;

            try {
                decoded = URLDecoder.decode(matcher.group(1), "UTF-8"); }
            catch(IllegalArgumentException | UnsupportedEncodingException ex) {
                decoded = matcher.group(1);
            }

            sb.append(decoded);
            last = matcher.end();
        }

        if(last != parts.length()) {
            sb.append(parts, last, parts.length());
        }

        return sb.toString();
    }

    public static String formatLabel(String title, boolean odd) {
        String trimmed = title.trim();

        if(matchUriUtf8.matcher(trimmed).find() && !odd) {
            trimmed = decodeParts(trimmed);
        }

        if(trimmed.length() == 0)
            return "";

        String replaced = trimmed.replace(' ', '_');
        String beginning = replaced.substring(0,1);

        //Hack! There is a situation where MediaWiki treats the capitalization
        // differently: ß -> SS in java, however not in MediaWiki. So an ugly
        // check is to verify that the capitilization goes both ways, if it
        // does it should work.
        String upper;
        if((upper = beginning.toUpperCase()).toLowerCase().equals(beginning)) {
            beginning = upper;
        }

        return beginning + replaced.substring(1);
    }
}
