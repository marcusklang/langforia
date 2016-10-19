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
package se.lth.cs.nlp.langforia.ext.stanford.corenlp;

import com.google.inject.Inject;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import se.lth.cs.docforia.Document;
import se.lth.cs.docforia.graph.text.Sentence;
import se.lth.cs.docforia.graph.text.Token;
import se.lth.cs.nlp.langforia.kernel.structure.Tokenizer;

import java.io.Reader;
import java.io.StringReader;

public class StanfordPTBTokenizer implements Tokenizer {

    private ThreadLocal<TokenizerFactory<CoreLabel>> tokenizers = new ThreadLocal<TokenizerFactory<CoreLabel>>() {
        @Override
        protected TokenizerFactory<CoreLabel> initialValue() {
            return PTBTokenizer.factory(false, true);
        }
    };

    @Inject
    public StanfordPTBTokenizer() {

    }

    @Override
    public void apply(Document doc) {
        Reader r = new StringReader(doc.text());

        for (Sentence sent : doc.nodes(Sentence.class)) {

            edu.stanford.nlp.process.Tokenizer<CoreLabel> Tokenizer = tokenizers.get().getTokenizer(new StringReader(sent.text()));

            while(Tokenizer.hasNext()) {
                CoreLabel next = Tokenizer.next();
                new Token(doc).setRange(next.beginPosition() + sent.getStart(), next.endPosition() + sent.getStart());
            }
        }
    }


}
