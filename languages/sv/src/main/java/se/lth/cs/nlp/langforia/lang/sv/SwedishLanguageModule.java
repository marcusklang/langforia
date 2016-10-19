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
package se.lth.cs.nlp.langforia.lang.sv;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.sv.SwedishAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;
import se.lth.cs.nlp.langforia.common.*;
import se.lth.cs.nlp.langforia.ext.lucene.LuceneNormalizer;
import se.lth.cs.nlp.langforia.ext.lucene.LuceneStemmer;
import se.lth.cs.nlp.langforia.ext.lucene.NoStopWordFiltering;
import se.lth.cs.nlp.langforia.ext.maltparser.MaltParser;
import se.lth.cs.nlp.langforia.ext.stagger.Stagger;
import se.lth.cs.nlp.langforia.kernel.LanguageModule;
import se.lth.cs.nlp.langforia.kernel.Models;
import se.lth.cs.nlp.langforia.kernel.resources.JarResource;
import se.lth.cs.nlp.langforia.kernel.structure.*;

public class SwedishLanguageModule extends LanguageModule {
    public SwedishLanguageModule() {
        super();
    }

    @Override
    protected void configure() {
        super.configure();
        configureComponents();
    }

    protected void configureComponents() {
        bind(CoarsePosTagConverter.class).in(Singleton.class);

        configureLucene();
        configureMaltparser();
        configureStagger();
        configureFullpipeline();
    }

    protected void configureFullpipeline() {
        PipelinesConfiguration pipelineconfig = new PipelinesConfiguration();

        pipelineconfig.addPipeline("default", "Stagger (tok, ssplit, pos, ner) + Maltparser",
                TextSegmenter.class,
                Stagger.Full.class,
                DependencyGrammarParser.class,
                CoarsePosTagConverter.class);

        pipelineconfig.addPipeline("herd", "Stagger (tok, ssplit, pos, ner) + Maltparser + HERD",
                TextSegmenter.class,
                Stagger.Full.class,
                DependencyGrammarParser.class,
                CoarsePosTagConverter.class);

        bindPipeline(pipelineconfig);
    }

    protected void configureLucene() {
        bind(Stemmer.class).to(LuceneStemmer.class);
        bind(TextNormalizer.class).to(LuceneNormalizer.class);
        bind(CharArraySet.class).annotatedWith(Models.named(LuceneNormalizer.LUCENE_STOP_WORDS_MODEL)).toInstance(SwedishAnalyzer.getDefaultStopSet());
    }

    protected void configureMaltparser() {
        bind(MaltParser.class).in(Singleton.class);
        bind(DependencyGrammarParser.class).to(MaltParser.class);
        bindModel(MaltParser.MODEL_ID, new JarResource("swemalt-1.7.2.mco", "models/sv/maltparser/swemalt-1.7.2.mco"));
    }

    protected void configureStagger() {
        bind(Stagger.class).in(Singleton.class);
        bind(Tokenizer.class).to(Stagger.Tokenizer.class);
        bind(Lemmatizer.class).to(Stagger.Lemmatizer.class);
        bind(SentenceSplitter.class).to(Stagger.SentenceSplitter.class);
        bind(TextSegmenter.class).to(Stagger.Segmenter.class);
        bind(NamedEntityRecognizer.class).to(Stagger.NamedEntityRecognizer.class);
        bind(PartOfSpeechTagger.class).to(Stagger.PartOfSpeechTagger.class);

        bindModel(Stagger.MODEL_ID, new JarResource("swedish.bin.bz2", "models/sv/stagger/swedish.bin.bz2"));
    }

    @Provides
    @NoStopWordFiltering
    public Analyzer getSwedishWithStopWordsAnalyzer() {
        return new SwedishAnalyzer(CharArraySet.EMPTY_SET);
    }

    @Provides
    public Analyzer getSwedishAnalyzer() {
        return new SwedishAnalyzer();
    }

    @Provides @Singleton
    public PartOfSpeechMapper getPosMapper() {
        PartOfSpeechMapperHashMapBacked mapper = new PartOfSpeechMapperHashMapBacked();

        mapper.putFromLanguage("AB", "ADV");
        mapper.putFromLanguage("DT", "DET");
        mapper.putFromLanguage("HA", "ADV");
        mapper.putFromLanguage("HD", "DET");
        mapper.putFromLanguage("HP", "PRON");
        mapper.putFromLanguage("HS", "DET");
        mapper.putFromLanguage("IE", "PRT");
        mapper.putFromLanguage("IN", "X");
        mapper.putFromLanguage("JJ", "ADJ");
        mapper.putFromLanguage("KN", "CONJ");
        mapper.putFromLanguage("NN", "NOUN");
        mapper.putFromLanguage("PC", "ADJ");
        mapper.putFromLanguage("PL", "PRT");
        mapper.putFromLanguage("PM", "PROPN");
        mapper.putFromLanguage("PN", "PRON");
        mapper.putFromLanguage("PP", "ADP");
        mapper.putFromLanguage("PS", "DET");
        mapper.putFromLanguage("RG", "NUM");
        mapper.putFromLanguage("RO", "ADJ");
        mapper.putFromLanguage("SN", "CONJ");
        mapper.putFromLanguage("UO", "X");
        mapper.putFromLanguage("VB", "VERB");
        mapper.putFromLanguage("MAD", ".");
        mapper.putFromLanguage("MID", ".");
        mapper.putFromLanguage("PAD", ".");

        mapper.putToLanguage(".", new String[] {"MAD", "MID", "PAD"});
        mapper.putToLanguage("ADJ", new String[] {"JJ", "PC", "RO"});
        mapper.putToLanguage("ADP", new String[] {"PP"});
        mapper.putToLanguage("ADV", new String[] {"AB", "HA"});
        mapper.putToLanguage("CONJ", new String[] {"KN", "SN"});
        mapper.putToLanguage("DET", new String[] {"DT", "HD", "HS", "PS"});
        mapper.putToLanguage("NOUN", new String[] {"NN"});
        mapper.putToLanguage("PROPN", new String[] {"PM"});
        mapper.putToLanguage("NUM", new String[] {"RG"});
        mapper.putToLanguage("PRON", new String[] {"HP", "PN"});
        mapper.putToLanguage("PRT", new String[] {"IE", "PL"});
        mapper.putToLanguage("VERB", new String[] {"VB"});
        mapper.putToLanguage("X", new String[] {"IN", "UO"});

        return mapper;
    }
}
