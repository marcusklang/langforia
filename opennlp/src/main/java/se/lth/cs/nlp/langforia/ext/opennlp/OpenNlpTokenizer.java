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
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;
import se.lth.cs.docforia.Document;
import se.lth.cs.docforia.graph.text.Token;
import se.lth.cs.nlp.langforia.kernel.Model;
import se.lth.cs.nlp.langforia.kernel.resources.Resource;
import se.lth.cs.nlp.langforia.kernel.structure.Tokenizer;
import java.io.IOError;
import java.io.IOException;

public class OpenNlpTokenizer implements Tokenizer {
    private final TokenizerModel model;
    public static final String MODEL_ID = "ext.opennlp.tokenize.model";

    @Inject
    public OpenNlpTokenizer(@Model(MODEL_ID) Resource handle) {
        try {
            model = new TokenizerModel(handle.binaryRead());
        } catch (IOException e) {
            throw new IOError(e);
        }
    }

    @Override
    public void apply(Document doc) {
        TokenizerME tokenizerME = new TokenizerME(model);
        Span[] spans = tokenizerME.tokenizePos(doc.text());
        for (int i = 0; i < spans.length; i++) {
            new Token(doc).setRange(spans[i].getStart(), spans[i].getEnd());
        }
    }
}
