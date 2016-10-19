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

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import se.lth.cs.docforia.Document;
import se.lth.cs.docforia.graph.text.Sentence;
import se.lth.cs.docforia.graph.text.Token;
import se.lth.cs.nlp.langforia.kernel.structure.TextSegmenter;

import java.util.List;
import java.util.Properties;

public class StanfordCoreNlpSegmenter implements TextSegmenter {

    private static StanfordCoreNLP pipeline;

    private static synchronized StanfordCoreNLP getPipeline() {
        if(pipeline != null)
            return pipeline;
        else
        {
            // creates a StanfordCoreNLP object, with sentence splitting
            Properties props = new Properties();
            props.setProperty("annotators", "tokenize, ssplit");
            props.setProperty("tokenize.options", "invertible");
            return (pipeline = new StanfordCoreNLP(props));
        }
    }

    private static final ThreadLocal<StanfordCoreNLP> pool = new ThreadLocal<StanfordCoreNLP>() {
        @Override
        protected StanfordCoreNLP initialValue() {
            return getPipeline();
        }
    };

    @Override
    public void apply(Document doc) {
        StanfordCoreNLP pipeline = pool.get();

        // read some text in the text variable
        String text = doc.text();

        // create an empty Annotation just with the given text
        Annotation document = new Annotation(text);

        // run all Annotators on this text
        pipeline.annotate(document);

        // these are all the sentences in this document
        // a CoreMap is essentially a Map that uses class objects as keys and has values with custom types
        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);

        for(CoreMap sentence: sentences) {
            int sent_start = sentence.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class);
            int sent_end = sentence.get(CoreAnnotations.CharacterOffsetEndAnnotation.class);

            new Sentence(doc).setRange(sent_start, sent_end);

            for (CoreLabel token: sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                int start = token.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class);
                int end = token.get(CoreAnnotations.CharacterOffsetEndAnnotation.class);

                new Token(doc).setRange(start, end);
            }
        }
    }
}
