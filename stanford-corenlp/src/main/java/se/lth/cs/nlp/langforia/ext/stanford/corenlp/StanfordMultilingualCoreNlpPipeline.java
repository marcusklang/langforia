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
import com.google.inject.Singleton;
import edu.stanford.nlp.hcoref.CorefCoreAnnotations;
import edu.stanford.nlp.hcoref.data.CorefChain;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.util.CoreMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import se.lth.cs.docforia.Document;
import se.lth.cs.docforia.graph.TokenProperties;
import se.lth.cs.docforia.graph.text.*;
import se.lth.cs.nlp.langforia.kernel.Property;
import se.lth.cs.nlp.langforia.kernel.exceptions.LangforiaRuntimeException;
import se.lth.cs.nlp.langforia.kernel.Language;
import se.lth.cs.nlp.langforia.kernel.LanguageCodes;
import se.lth.cs.nlp.langforia.kernel.structure.FullPipeline;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@Singleton
public class StanfordMultilingualCoreNlpPipeline implements FullPipeline {

    public static final String PROPERTY_ENABLE_NER = "lang.common.StanfordMultilingualCoreNlpPipeline.NerEnabled";
    public static final String PROPERTY_ENABLE_DEPENDENCY = "lang.common.StanfordMultilingualCoreNlpPipeline.DependencyEnabled";
    public static final String PROPERTY_INCLUDE_NER_DATE = "lang.common.StanfordMultilingualCoreNlpPipeline.IncludeNerDate";

    private final boolean includeDate;

    @Inject
    public StanfordMultilingualCoreNlpPipeline(Language lang,
                                               @Property(PROPERTY_ENABLE_NER) boolean nerEnabled,
                                               @Property(PROPERTY_ENABLE_DEPENDENCY) boolean depEnabled,
                                               @Property(PROPERTY_INCLUDE_NER_DATE) boolean includeDate)
    {
        this.includeDate = includeDate;
        switch(lang.getLanguageCode()) {
            case LanguageCodes.ENGLISH: {
                final Properties props = new Properties();
                props.setProperty("annotators", "tokenize, ssplit, pos, lemma" + (nerEnabled ? ", ner" : "") + (depEnabled ? ", parse" : ""));
                props.setProperty("tokenize.options", "invertible=true,ptb3Escaping=true");
                props.setProperty("ner.applyNumericClassifiers", "false");
                props.setProperty("ner.useSUTime", "false");
                this.pipeline = new StanfordCoreNLP(props);

                break;
            }
            case LanguageCodes.SPANISH: {
                final Properties props = new Properties();
                props.setProperty("annotators", "tokenize, ssplit, pos" + (nerEnabled ? ", ner" : "") + (depEnabled ? ", parse" : ""));
                props.setProperty("tokenize.options", "invertible=true,ptb3Escaping=true");
                props.setProperty("tokenize.language", "es");

                props.setProperty("pos.model", "edu/stanford/nlp/models/pos-tagger/spanish/spanish-distsim.tagger");
                props.setProperty("ner.model", "edu/stanford/nlp/models/ner/spanish.ancora.distsim.s512.crf.ser.gz");

                props.setProperty("parse.model", "edu/stanford/nlp/models/lexparser/spanishPCFG.ser.gz");

                props.setProperty("ner.applyNumericClassifiers", "false");
                props.setProperty("ner.useSUTime", "false");
                this.pipeline = new StanfordCoreNLP(props);

                break;
            }
            case LanguageCodes.GERMAN: {
                final Properties props = new Properties();
                props.setProperty("annotators", "tokenize, ssplit, pos" + (nerEnabled ? ", ner" : "") + (depEnabled ? ", parse" : ""));
                props.setProperty("tokenize.options", "invertible");
                props.setProperty("tokenize.language", "de");

                props.setProperty("pos.model", "edu/stanford/nlp/models/pos-tagger/german/german-hgc.tagger");
                props.setProperty("ner.model", "edu/stanford/nlp/models/ner/german.hgc_175m_600.crf.ser.gz");

                props.setProperty("parse.model", "edu/stanford/nlp/models/lexparser/germanFactored.ser.gz");

                props.setProperty("ner.applyNumericClassifiers", "false");
                props.setProperty("ner.useSUTime", "false");
                this.pipeline = new StanfordCoreNLP(props);


                break;
            }
            default:
                throw new LangforiaRuntimeException(lang.getLanguageCode(), "Unsupported language '" + lang.getLanguageCode() + "' for StanfordMultilingualCoreNlpPipelien");
        }
    }

    private final StanfordCoreNLP pipeline;

    @Override
    public void apply(Document doc) {
        Int2ObjectOpenHashMap<Token> tokens = new Int2ObjectOpenHashMap<>();

        // read some text in the text variable
        String text = doc.text();

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

            tokens.clear();

            // traversing the words in the current sentence
            // a CoreLabel is a CoreMap with additional token-specific methods
            for (CoreLabel token: sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                int start = token.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class);
                int end = token.get(CoreAnnotations.CharacterOffsetEndAnnotation.class);

                Token tok = new Token(doc).setRange(start, end);
                int id = token.get(CoreAnnotations.IndexAnnotation.class);
                //tok.putProperty(TokenProperties.ID, id);
                tokens.put(id, tok);

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

                if(token.has(CoreAnnotations.FeaturesAnnotation.class)) {
                    String feats = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                    tok.putProperty(TokenProperties.FEATS, feats);
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

            SemanticGraph semanticGraph = sentence.get(SemanticGraphCoreAnnotations.CollapsedDependenciesAnnotation.class);
            if(semanticGraph != null) {
                for (SemanticGraphEdge semanticGraphEdge : semanticGraph.edgeIterable()) {
                    int tail = semanticGraphEdge.getSource().get(CoreAnnotations.IndexAnnotation.class);
                    int head = semanticGraphEdge.getTarget().get(CoreAnnotations.IndexAnnotation.class);
                    String label = semanticGraphEdge.getRelation().getShortName();

                    new DependencyRelation(doc).setRelation(label).connect(tokens.get(tail), tokens.get(head));
                }
            }


            index++;
        }

        if(document.has(CorefCoreAnnotations.CorefChainAnnotation.class)) {
            Map<Integer, CorefChain> value = document.get(CorefCoreAnnotations.CorefChainAnnotation.class);
            for (Map.Entry<Integer, CorefChain> entry : value.entrySet()) {
                String id = entry.getKey().toString();
                CorefChain.CorefMention repr = entry.getValue().getRepresentativeMention();
                CoreferenceChain chain = new CoreferenceChain(doc).setId(id);

                for (CorefChain.CorefMention corefMention : entry.getValue().getMentionsInTextualOrder()) {
                    List<CoreLabel> sentTokens = sentences.get(corefMention.sentNum - 1).get(CoreAnnotations.TokensAnnotation.class);

                    int start = sentTokens.get(corefMention.startIndex - 1).get(CoreAnnotations.CharacterOffsetBeginAnnotation.class);
                    int end = sentTokens.get(corefMention.endIndex - 2).get(CoreAnnotations.CharacterOffsetEndAnnotation.class);

                    CoreferenceMention coref = new CoreferenceMention(doc).setRange(start, end);
                    coref.connect(chain, new CoreferenceChainEdge(doc));

                    coref.putProperty("mention-type", corefMention.mentionType.name());
                    coref.putProperty("animacy", corefMention.animacy.name());
                    coref.putProperty("gender", corefMention.gender.name());
                    coref.putProperty("number", corefMention.number.name());
                    if (corefMention.equals(repr)) {
                        coref.putProperty("representative", "1");
                    }
                }
            }
        }

        for (int i = 0; i < netoken.size(); i++) {
            Token start = netoken.get(i);
            String ne = start.getProperty(TokenProperties.NE);
            if(!includeDate && ne.equalsIgnoreCase("DATE"))
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
