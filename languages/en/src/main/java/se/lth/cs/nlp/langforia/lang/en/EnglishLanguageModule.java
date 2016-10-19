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
package se.lth.cs.nlp.langforia.lang.en;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;
import se.lth.cs.nlp.langforia.common.*;
import se.lth.cs.nlp.langforia.ext.clearnlp.ClearNLP;
import se.lth.cs.nlp.langforia.ext.lucene.LuceneNormalizer;
import se.lth.cs.nlp.langforia.ext.lucene.LuceneStemmer;
import se.lth.cs.nlp.langforia.ext.lucene.NoStopWordFiltering;
import se.lth.cs.nlp.langforia.ext.maltparser.MaltParser;
import se.lth.cs.nlp.langforia.ext.stanford.corenlp.StanfordCoreNlpSegmenter;
import se.lth.cs.nlp.langforia.ext.stanford.corenlp.StanfordMultilingualCoreNlpPipeline;
import se.lth.cs.nlp.langforia.ext.stanford.corenlp.StanfordPTBTokenizer;
import se.lth.cs.nlp.langforia.kernel.LanguageModule;
import se.lth.cs.nlp.langforia.kernel.Models;
import se.lth.cs.nlp.langforia.kernel.resources.JarResource;
import se.lth.cs.nlp.langforia.kernel.structure.*;

/**
 * Created by marcusk on 2016-10-07.
 */
public class EnglishLanguageModule extends LanguageModule {
    public EnglishLanguageModule() {
        super();
    }

    @Override
    protected void configure() {
        super.configure();
        configureLucene();
        configureSegmentation();
        configureFullpipeline();
    }

    protected void configureSegmentation() {
        bind(WordSet.class).to(SimpleWordSet.class).in(Singleton.class);
        bindModel(SimpleWordSet.MODEL_ID, new JarResource("wordlist.txt", "models/en/wordlist.txt"));

        bind(Tokenizer.class).to(StanfordPTBTokenizer.class).in(Singleton.class);
        bind(SentenceSplitter.class).to(StanfordCoreNlpSegmenter.class).in(Singleton.class);
        bind(CoarsePosTagConverter.class).in(Singleton.class);
    }

    protected void configureLucene() {
        bind(Stemmer.class).to(LuceneStemmer.class);
        bind(TextNormalizer.class).to(LuceneNormalizer.class);
        bind(CharArraySet.class).annotatedWith(Models.named(LuceneNormalizer.LUCENE_STOP_WORDS_MODEL)).toInstance(EnglishAnalyzer.getDefaultStopSet());
    }

    @Provides
    @Singleton
    @NoStopWordFiltering
    public Analyzer getEnglishWithStopWordsAnalyzer() {
        return new EnglishAnalyzer(CharArraySet.EMPTY_SET);
    }

    @Singleton
    @Provides
    public Analyzer getEnglishAnalyzer() {
        return new EnglishAnalyzer();
    }

    protected void configureFullpipeline() {
        bind(FullPipeline.class).to(FullPipelineImpl.class).in(Singleton.class);
        bindProperty(StanfordMultilingualCoreNlpPipeline.PROPERTY_ENABLE_NER, Boolean.TRUE);
        bindProperty(StanfordMultilingualCoreNlpPipeline.PROPERTY_INCLUDE_NER_DATE, Boolean.TRUE);
        bindProperty(StanfordMultilingualCoreNlpPipeline.PROPERTY_ENABLE_DEPENDENCY, Boolean.FALSE);

        bindModel(MaltParser.MODEL_ID, new JarResource("engmalt.linear-1.7.mco", "models/en/maltparser/engmalt.linear-1.7.mco"));

        bind(MaltParser.class).in(Singleton.class);

        PipelinesConfiguration pipelines = new PipelinesConfiguration();

        pipelines.addPipeline("default", "Stanford CoreNLP (tok, ssplit, ner)",
                StanfordMultilingualCoreNlpPipeline.class,
                CoarsePosTagConverter.class);

        pipelines.addPipeline("default_extra", "Stanford CoreNLP (tok, ssplit, ner) + Maltparser + Lucene Terms/Stems",
                StanfordMultilingualCoreNlpPipeline.class,
                MaltParser.class,
                Stemmer.class,
                CoarsePosTagConverter.class);

        pipelines.addPipeline("clearnlp", "Stanford CoreNLP (tok, ssplit) + ClearNLP (pos, dep, srl)",
                StanfordCoreNlpSegmenter.class,
                ClearNLP.class,
                CoarsePosTagConverter.class);

        bindPipeline(pipelines);
    }

    @Provides @Singleton
    public PartOfSpeechMapper getPosMapper() {
        PartOfSpeechMapperHashMapBacked mapper = new PartOfSpeechMapperHashMapBacked();

        mapper.putFromLanguage("!",".");
        mapper.putFromLanguage("#",".");
        mapper.putFromLanguage("$",".");
        mapper.putFromLanguage("''",".");
        mapper.putFromLanguage("(",".");
        mapper.putFromLanguage(")",".");
        mapper.putFromLanguage(",",".");
        mapper.putFromLanguage("-LRB-",".");
        mapper.putFromLanguage("-RRB-",".");
        mapper.putFromLanguage(".",".");
        mapper.putFromLanguage(":",".");
        mapper.putFromLanguage("?",".");
        mapper.putFromLanguage("CC","CONJ");
        mapper.putFromLanguage("CD","NUM");
        mapper.putFromLanguage("CD|RB","X");
        mapper.putFromLanguage("DT","DET");
        mapper.putFromLanguage("EX","DET");
        mapper.putFromLanguage("FW","X");
        mapper.putFromLanguage("IN","ADP");
        mapper.putFromLanguage("IN|RP","ADP");
        mapper.putFromLanguage("JJ","ADJ");
        mapper.putFromLanguage("JJR","ADJ");
        mapper.putFromLanguage("JJRJR","ADJ");
        mapper.putFromLanguage("JJS","ADJ");
        mapper.putFromLanguage("JJ|RB","ADJ");
        mapper.putFromLanguage("JJ|VBG","ADJ");
        mapper.putFromLanguage("LS","X");
        mapper.putFromLanguage("MD","VERB");
        mapper.putFromLanguage("NN","NOUN");
        mapper.putFromLanguage("NNP","PROPN");
        mapper.putFromLanguage("NNPS","PROPN");
        mapper.putFromLanguage("NNS","NOUN");
        mapper.putFromLanguage("NN|NNS","NOUN");
        mapper.putFromLanguage("NN|SYM","NOUN");
        mapper.putFromLanguage("NN|VBG","NOUN");
        mapper.putFromLanguage("NP","NOUN");
        mapper.putFromLanguage("PDT","DET");
        mapper.putFromLanguage("POS","PRT");
        mapper.putFromLanguage("PRP","PRON");
        mapper.putFromLanguage("PRP$","PRON");
        mapper.putFromLanguage("PRP|VBP","PRON");
        mapper.putFromLanguage("PRT","PRT");
        mapper.putFromLanguage("RB","ADV");
        mapper.putFromLanguage("RBR","ADV");
        mapper.putFromLanguage("RBS","ADV");
        mapper.putFromLanguage("RB|RP","ADV");
        mapper.putFromLanguage("RB|VBG","ADV");
        mapper.putFromLanguage("RN","X");
        mapper.putFromLanguage("RP","PRT");
        mapper.putFromLanguage("SYM","X");
        mapper.putFromLanguage("TO","PRT");
        mapper.putFromLanguage("UH","X");
        mapper.putFromLanguage("VB","VERB");
        mapper.putFromLanguage("VBD","VERB");
        mapper.putFromLanguage("VBD|VBN","VERB");
        mapper.putFromLanguage("VBG","VERB");
        mapper.putFromLanguage("VBG|NN","VERB");
        mapper.putFromLanguage("VBN","VERB");
        mapper.putFromLanguage("VBP","VERB");
        mapper.putFromLanguage("VBP|TO","VERB");
        mapper.putFromLanguage("VBZ","VERB");
        mapper.putFromLanguage("VP","VERB");
        mapper.putFromLanguage("WDT","DET");
        mapper.putFromLanguage("WH","X");
        mapper.putFromLanguage("WP","PRON");
        mapper.putFromLanguage("WP$","PRON");
        mapper.putFromLanguage("WRB","ADV");
        mapper.putFromLanguage("``",".");

        mapper.putToLanguage(".", new String[] {"!",
                "#",
                "$",
                "''",
                "(",
                ")",
                ",",
                "-LRB-",
                "-RRB-",
                ".",
                ":",
                "?",
                "``"});

        mapper.putToLanguage("ADJ", new String[] {"JJ",
                "JJR",
                "JJRJR",
                "JJS",
                "JJ|RB",
                "JJ|VBG"});

        mapper.putToLanguage("ADP", new String[] {"IN",
                "IN|RP"});

        mapper.putToLanguage("ADV", new String[] {"RB",
                "RBR",
                "RBS",
                "RB|RP",
                "RB|VBG",
                "WRB"});

        mapper.putToLanguage("CONJ", new String[] {"CC"});
        mapper.putToLanguage("DET", new String[] {"DT",
                "EX",
                "PDT",
                "WDT"});

        mapper.putToLanguage("NOUN", new String[] {"NN",
                "NNS",
                "NN|NNS",
                "NN|SYM",
                "NN|SYM",
                "NN|VBG",
                "NP"});

        mapper.putToLanguage("PROPN", new String[] {
                "NNP",
                "NNPS"
        });

        mapper.putToLanguage("NUM", new String[] {"CD"});
        mapper.putToLanguage("PRON", new String[] {"PRP",
                "PRP$",
                "PRP|VBP",
                "WP",
                "WP$"});

        mapper.putToLanguage("PRT", new String[] {"POS",
                "PRT",
                "RP",
                "TO"});

        mapper.putToLanguage("VERB", new String[] {"MD",
                "VB",
                "VBD",
                "VBD|VBN",
                "VBG",
                "VBG|NN",
                "VBN",
                "VBP",
                "VBP|TO",
                "VBZ",
                "VP"});

        mapper.putToLanguage("X", new String[] {"CD|RB",
                "FW",
                "LS",
                "RN",
                "SYM",
                "UH",
                "WH"});

        return mapper;
    }
}

