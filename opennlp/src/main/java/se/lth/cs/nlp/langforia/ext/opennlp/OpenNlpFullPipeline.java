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
package se.lth.cs.nlp.langforia.ext.opennlp;

import com.google.inject.Inject;
import se.lth.cs.docforia.Document;
import se.lth.cs.nlp.langforia.kernel.Language;
import se.lth.cs.nlp.langforia.kernel.structure.FullPipeline;

public class OpenNlpFullPipeline implements FullPipeline {
    private final Language lang;

    @Inject
    public OpenNlpFullPipeline(Language lang) {
        this.lang = lang;
    }

    @Override
    public void apply(Document doc) {
        lang.apply(doc, OpenNlpTokenizer.class);
        lang.apply(doc, OpenNlpSentenceSplitter.class);
        lang.apply(doc, OpenNlpPartOfSpeechTagger.class);
        lang.apply(doc, OpenNlpNameFinder.class);
    }
}
