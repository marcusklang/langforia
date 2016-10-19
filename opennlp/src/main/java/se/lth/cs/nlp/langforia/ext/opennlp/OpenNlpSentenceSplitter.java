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
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.util.Span;
import se.lth.cs.docforia.Document;
import se.lth.cs.docforia.graph.text.Sentence;
import se.lth.cs.nlp.langforia.kernel.Model;
import se.lth.cs.nlp.langforia.kernel.resources.Resource;
import se.lth.cs.nlp.langforia.kernel.structure.SentenceSplitter;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;

public class OpenNlpSentenceSplitter implements SentenceSplitter {

    public static final String MODEL_ID = "ext.opennlp.ssplit.model";

    private final SentenceModel model;

    public OpenNlpSentenceSplitter(InputStream is) {
        try {
            model = new SentenceModel(is);
            is.close();
        }
        catch (IOException e) {
            throw new IOError(e);
        }
    }

    @Inject
    public OpenNlpSentenceSplitter(@Model(MODEL_ID) Resource model) {
        this(model.binaryRead());
    }

    @Override
    public void apply(Document doc) {
        SentenceDetectorME detector = new SentenceDetectorME(model);
        Span[] spans = detector.sentPosDetect(doc.text());

        for(int i = 0; i < spans.length; i++) {
            int start = spans[i].getStart();
            int end = spans[i].getEnd();
            new Sentence(doc).setRange(start, end);
        }
    }
}
