package se.lth.cs.nlp.langforia.common;
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

import se.lth.cs.docforia.Document;
import se.lth.cs.docforia.graph.text.Sentence;
import se.lth.cs.nlp.langforia.kernel.structure.SentenceSplitter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexSentenceSplitter implements SentenceSplitter {
    private Pattern sentenceSegments = Pattern.compile("(([\\.\\!\\?¡¿])[\\p{Z}\\n\\ ]+(?=\\s*\\p{L}|\\s*[^\\p{L}]))|([«»‹›\\‘\\’\\“\\”\\'\\'\\\"\\\"].+?[«»‹›\\‘\\’\\“\\”\\'\\'\\\"\\\"])|(\\(.+?\\))", Pattern.UNICODE_CHARACTER_CLASS);

    @Override
    public void apply(Document doc) {
        Matcher matcher = sentenceSegments.matcher(doc.text());
        int last = 0;

        while(matcher.find()) {
            if(matcher.group(3) != null)
                continue;

            if(matcher.group(4) != null)
                continue;

            int current = matcher.end(2);
            if(last != current)
            {
                new Sentence(doc).setRange(last,current);
                last = matcher.end();
            }
        }

        if(last != doc.length()) {
            new Sentence(doc).setRange(last,doc.length());
        }
    }
}
