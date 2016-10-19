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
package se.lth.cs.nlp.langforia.lang.fr;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.fr.FrenchAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;
import se.lth.cs.nlp.langforia.common.*;
import se.lth.cs.nlp.langforia.ext.anna.AnnaPipeline;
import se.lth.cs.nlp.langforia.ext.anna.AnnaPipelineModels;
import se.lth.cs.nlp.langforia.ext.lucene.LuceneNormalizer;
import se.lth.cs.nlp.langforia.ext.lucene.LuceneStemmer;
import se.lth.cs.nlp.langforia.ext.lucene.NoStopWordFiltering;
import se.lth.cs.nlp.langforia.ext.maltparser.MaltParser;
import se.lth.cs.nlp.langforia.ext.opennlp.OpenNlpSentenceSplitter;
import se.lth.cs.nlp.langforia.ext.stanford.corenlp.StanfordCoreNlpSegmenter;
import se.lth.cs.nlp.langforia.kernel.LanguageModule;
import se.lth.cs.nlp.langforia.kernel.Models;
import se.lth.cs.nlp.langforia.kernel.resources.JarResource;
import se.lth.cs.nlp.langforia.kernel.structure.*;

import java.io.File;

/**
 * Created by marcusk on 2016-10-07.
 */
public class FrenchLanguageModule extends LanguageModule {
    public FrenchLanguageModule() {
        super();
    }

    @Override
    protected void configure() {
        super.configure();

        bind(FullPipeline.class).to(FullPipelineImpl.class).in(Singleton.class);
        bind(SentenceSplitter.class).to(StanfordCoreNlpSegmenter.class).in(Singleton.class);

        //bind(WordSet.class).to(SimpleWordSet.class).in(Singleton.class);
        //bindModel(SimpleWordSet.MODEL_ID, new JarResource("frwords.txt", "models/fr/words.txt"));
        bindModel(MaltParser.MODEL_ID, new JarResource("fremalt-1.7.moc", "models/fr/maltparser/fremalt-1.7.mco"));
        bind(MaltParser.class).in(Singleton.class);
        bind(CoarsePosTagConverter.class).in(Singleton.class);

        configureLucene();
        configureFullpipeline();
    }

    protected void configureFullpipeline() {

        PipelinesConfiguration pipelineconfig = new PipelinesConfiguration();

        pipelineconfig.addPipeline("default", "Stanford CoreNLP (tok, ssplit) + ANNA (lemma, morph, pos) + Maltparser",
                SentenceSplitter.class,
                AnnaPipeline.class,
                MaltParser.class,
                Stemmer.class,
                TextNormalizer.class ,
                CoarsePosTagConverter.class);

        bindPipeline(pipelineconfig);
    }

    @Provides
    @Singleton
    public AnnaPipeline getPipeline() {
        File lemma = new JarResource("lemma.model", "models/fr/anna/ftb6_1.conll09.crossannotated.anna-3.3-d8.jar.lemmatizer.model").file();
        File morphtagger = new JarResource("morphtagger.model", "models/fr/anna/ftb6_1.conll09.crossannotated.anna-3.3-d8.jar.morphtagger.model").file();
        File parser = new JarResource("morphtagger.model", "models/fr/anna/ftb6_1.conll09.crossannotated.anna-3.3-d8.jar.morphtagger.model").file();
        File postagger = new JarResource("postagger.model", "models/fr/anna/ftb6_1.conll09.crossannotated.anna-3.3-d8.jar.postagger.model").file();

        AnnaPipelineModels models = new AnnaPipelineModels("fre", postagger, parser, lemma, morphtagger, null);
        return new AnnaPipeline(models);
    }

    protected void configureLucene() {
        bind(Stemmer.class).to(LuceneStemmer.class).in(Singleton.class);
        bind(TextNormalizer.class).to(LuceneNormalizer.class).in(Singleton.class);
        bind(CharArraySet.class).annotatedWith(Models.named(LuceneNormalizer.LUCENE_STOP_WORDS_MODEL)).toInstance(FrenchAnalyzer.getDefaultStopSet());
    }

    @Provides @Singleton
    public PartOfSpeechMapper getPosMapper() {
        PartOfSpeechMapperHashMapBacked mapper = new PartOfSpeechMapperHashMapBacked();
        mapper.putFromLanguage("ADJ", "ADJ");
        mapper.putFromLanguage("ADJWH", "ADJ");
        mapper.putFromLanguage("ADV", "ADV");
        mapper.putFromLanguage("ADVWH", "ADV");
        mapper.putFromLanguage("CC", "CONJ");
        mapper.putFromLanguage("CS", "CONJ");
        mapper.putFromLanguage("CLO", "PRON");
        mapper.putFromLanguage("CLR", "PRON");
        mapper.putFromLanguage("CLS", "PRON");
        mapper.putFromLanguage("PRO", "PRON");
        mapper.putFromLanguage("PROREL", "PRON");
        mapper.putFromLanguage("PROWH", "PRON");
        mapper.putFromLanguage("DET", "DET");
        mapper.putFromLanguage("DETWH", "DET");
        mapper.putFromLanguage("ET", "X");
        mapper.putFromLanguage("I", "X");
        mapper.putFromLanguage("NC", "NOUN");
        mapper.putFromLanguage("NPP", "PROPN");
        mapper.putFromLanguage("P", "ADP");
        mapper.putFromLanguage("P+D", "ADP");
        mapper.putFromLanguage("P+PRON", "ADP");
        mapper.putFromLanguage("PONCT", ".");
        mapper.putFromLanguage("PREF", "PRT");
        mapper.putFromLanguage("V", "VERB");
        mapper.putFromLanguage("VIMP", "VERB");
        mapper.putFromLanguage("VINF", "VERB");
        mapper.putFromLanguage("VPP", "VERB");
        mapper.putFromLanguage("VPR", "VERB");
        mapper.putFromLanguage("VS", "VERB");

        mapper.putToLanguage("ADJ", new String[] {"ADJ", "ADJWH"});
        mapper.putToLanguage("ADV", new String[] {"ADV", "ADVWH"});
        mapper.putToLanguage("CONV", new String[] {"CC", "CS"});
        mapper.putToLanguage("PRON", new String[] {"CLO", "CLR", "CLS", "PRO", "PROREL", "PROWH"});
        mapper.putToLanguage("DET", new String[] {"DET", "DETWH"});
        mapper.putToLanguage("X", new String[] {"ET", "I"});
        mapper.putToLanguage("NOUN", new String[] {"NC"});
        mapper.putToLanguage("PROPN", new String[] {"NPP"});
        mapper.putToLanguage("ADP", new String[] {"P", "P+D", "P+PRON"});
        mapper.putToLanguage(".", new String[] {"PONCT"});
        mapper.putToLanguage("PRT", new String[] {"PREF"});
        mapper.putToLanguage("VERB", new String[] {"V", "VIMP", "VINF", "VPP", "VPR", "VS"});

        return mapper;
    }

    @Singleton
    @Provides @NoStopWordFiltering
    public Analyzer getFrenchWithStopWordsAnalyzer() {
        return new FrenchAnalyzer(CharArraySet.EMPTY_SET);
    }

    @Singleton
    @Provides
    public Analyzer getFrenchAnalyzer() {
        return new FrenchAnalyzer();
    }
}
