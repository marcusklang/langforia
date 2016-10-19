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
package se.lth.cs.nlp.langforia.lang.de;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.de.GermanAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;
import se.lth.cs.nlp.langforia.common.*;
import se.lth.cs.nlp.langforia.ext.lucene.LuceneNormalizer;
import se.lth.cs.nlp.langforia.ext.lucene.LuceneStemmer;
import se.lth.cs.nlp.langforia.ext.lucene.NoStopWordFiltering;
import se.lth.cs.nlp.langforia.ext.stanford.corenlp.StanfordMultilingualCoreNlpPipeline;
import se.lth.cs.nlp.langforia.kernel.LanguageModule;
import se.lth.cs.nlp.langforia.kernel.Models;
import se.lth.cs.nlp.langforia.kernel.resources.JarResource;
import se.lth.cs.nlp.langforia.kernel.structure.*;

/**
 * Created by marcusk on 2016-10-07.
 */
public class GermanLanguageModule extends LanguageModule {
    public GermanLanguageModule() {
        super();
    }

    @Override
    protected void configure() {
        super.configure();

        configureLucene();
        configureFullpipeline();
    }

    protected void configureLucene() {
        bind(Stemmer.class).to(LuceneStemmer.class);
        bind(TextNormalizer.class).to(LuceneNormalizer.class);
        bind(CharArraySet.class).annotatedWith(Models.named(LuceneNormalizer.LUCENE_STOP_WORDS_MODEL)).toInstance(GermanAnalyzer.getDefaultStopSet());
    }

    @Provides
    @Singleton
    @NoStopWordFiltering
    public Analyzer getGermanWithStopWordsAnalyzer() {
        return new GermanAnalyzer(CharArraySet.EMPTY_SET);
    }

    @Singleton
    @Provides
    public Analyzer getGermanAnalyzer() {
        return new GermanAnalyzer();
    }

    protected void configureFullpipeline() {
        bindProperty(StanfordMultilingualCoreNlpPipeline.PROPERTY_ENABLE_NER, Boolean.TRUE);
        bindProperty(StanfordMultilingualCoreNlpPipeline.PROPERTY_INCLUDE_NER_DATE, Boolean.TRUE);
        bindProperty(StanfordMultilingualCoreNlpPipeline.PROPERTY_ENABLE_DEPENDENCY, Boolean.FALSE);
        bind(FullPipeline.class).to(FullPipelineImpl.class).in(Singleton.class);

        PipelinesConfiguration pipelines = new PipelinesConfiguration();
        pipelines.addPipeline("default", "Stanford CoreNLP (tok, ssplit, pos, ner)",
                StanfordMultilingualCoreNlpPipeline.class,
                CoarsePosTagConverter.class
        );

        bindPipeline(pipelines);
    }

    @Provides
    @Singleton
    public PartOfSpeechMapper getPosMapper() {
        PartOfSpeechMapperHashMapBacked mapper = new PartOfSpeechMapperHashMapBacked();
        return mapper;
    }
}
