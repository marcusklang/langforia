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
import se.lth.cs.docforia.graph.text.Token;
import se.lth.cs.nlp.langforia.kernel.structure.Tokenizer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexTokenizer  implements Tokenizer{
    private Pattern pattern = Pattern.compile("(?:([+\\-]?[\\p{N}]+(?:[\\.\\,][\\p{N}]{3})*[\\.\\,]?[\\p{N}]*))|(?:(?:[\\p{Lu}]{2,})|(?:[\\p{Lu}]\\.[\\p{Lu}])[\\p{Lu}\\.]*)|[^\\s\\p{P}]+|[\\p{P}]", Pattern.UNICODE_CHARACTER_CLASS);

    @Override
    public void apply(Document doc) {
        doc.store().nodeLayer(Document.nodeLayer(Sentence.class)).forEach(sent -> {
            Matcher matcher = pattern.matcher(doc.text(sent.getStart(),sent.getEnd()));
            while(matcher.find()) {
                new Token(doc).setRange(matcher.start()+sent.getStart(),matcher.end()+sent.getStart());
            }
        });
    }
}
