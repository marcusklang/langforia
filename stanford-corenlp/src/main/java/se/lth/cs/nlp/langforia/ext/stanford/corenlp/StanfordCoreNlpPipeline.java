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
import se.lth.cs.docforia.graph.TokenProperties;
import se.lth.cs.docforia.graph.text.NamedEntity;
import se.lth.cs.docforia.graph.text.Sentence;
import se.lth.cs.docforia.graph.text.Token;
import se.lth.cs.nlp.langforia.kernel.structure.FullPipeline;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class StanfordCoreNlpPipeline implements FullPipeline {

    private static final ThreadLocal<StanfordCoreNLP> pool = new ThreadLocal<StanfordCoreNLP>() {
        @Override
        protected StanfordCoreNLP initialValue() {
            // creates a StanfordCoreNLP object, with POS tagging, lemmatization, NER, parsing, and coreference resolution
            Properties props = new Properties();
            props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner");
            props.setProperty("tokenize.options", "invertible");
            props.setProperty("ner.applyNumericClassifiers", "false");
            props.setProperty("ner.useSUTime", "false");

            return new StanfordCoreNLP(props);
        }
    };

    @Override
    public void apply(Document doc) {
        StanfordCoreNLP pipeline = pool.get();

        // read some text in the text variable
        String text = doc.text(); // Add your text here!

        // create an empty Annotation just with the given text
        Annotation document = new Annotation(text);

        // run all Annotators on this text
        pipeline.annotate(document);

        // these are all the sentences in this document
        // a CoreMap is essentially a Map that uses class objects as keys and has values with custom types
        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);

        ArrayList<Token> netoken = new ArrayList<>();
        int index = 0;

        for(CoreMap sentence: sentences) {
            int sent_start = sentence.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class);
            int sent_end = sentence.get(CoreAnnotations.CharacterOffsetEndAnnotation.class);

            new Sentence(doc).setRange(sent_start, sent_end);

            // traversing the words in the current sentence
            // a CoreLabel is a CoreMap with additional token-specific methods
            for (CoreLabel token: sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                int start = token.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class);
                int end = token.get(CoreAnnotations.CharacterOffsetEndAnnotation.class);

                Token tok = new Token(doc).setRange(start,end);

                // this is the text of the token
                if(token.has(CoreAnnotations.LemmaAnnotation.class)) {
                    String lemma = token.get(CoreAnnotations.LemmaAnnotation.class);
                    tok.putProperty(TokenProperties.LEMMA, lemma);
                }

                // this is the POS tag of the token
                if(token.has(CoreAnnotations.PartOfSpeechAnnotation.class)) {
                    String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                    tok.putProperty(TokenProperties.POS, pos);
                }

                // this is the NER label of the token
                if(token.has(CoreAnnotations.NamedEntityTagAnnotation.class)) {
                    String ne = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);
                    if(!ne.equals("O")) {
                        tok.putProperty(TokenProperties.NE, ne);
                        tok.putTag("index", index);
                        netoken.add(tok);
                    }
                }

                index++;
            }

            index++;
        }

        for (int i = 0; i < netoken.size(); i++) {
            Token start = netoken.get(i);
            String ne = start.getProperty(TokenProperties.NE);
            if(ne.equalsIgnoreCase("DATE"))
                continue;

            int lastIndex = start.getTag("index");

            Token end = start;

            int k = i+1;
            for(; k < netoken.size(); k++) {
                Token current = netoken.get(k);
                int currentIndex = current.getTag("index");
                if(current.getProperty(TokenProperties.NE).equals(ne) && (currentIndex == lastIndex + 1)) {
                    end = current;
                    lastIndex = currentIndex;
                }
                else
                    break;
            }

            i = k-1;

            new NamedEntity(doc).setLabel(ne).setRange(start.getStart(), end.getEnd());
        }
    }
}
