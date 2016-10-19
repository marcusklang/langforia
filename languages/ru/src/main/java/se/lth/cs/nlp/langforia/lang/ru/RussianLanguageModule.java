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
package se.lth.cs.nlp.langforia.lang.ru;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.ru.RussianAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;
import se.lth.cs.nlp.langforia.common.*;
import se.lth.cs.nlp.langforia.ext.lucene.LuceneNormalizer;
import se.lth.cs.nlp.langforia.ext.lucene.LuceneStemmer;
import se.lth.cs.nlp.langforia.ext.lucene.NoStopWordFiltering;
import se.lth.cs.nlp.langforia.kernel.LanguageModule;
import se.lth.cs.nlp.langforia.kernel.Models;
import se.lth.cs.nlp.langforia.kernel.resources.JarResource;
import se.lth.cs.nlp.langforia.kernel.structure.*;

public class RussianLanguageModule extends LanguageModule {
    public RussianLanguageModule() {
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
        bind(CharArraySet.class).annotatedWith(Models.named(LuceneNormalizer.LUCENE_STOP_WORDS_MODEL)).toInstance(RussianAnalyzer.getDefaultStopSet());
    }

    @Provides
    @Singleton
    @NoStopWordFiltering
    public Analyzer getRussianWithStopWordsAnalyzer() {
        return new RussianAnalyzer(CharArraySet.EMPTY_SET);
    }

    @Singleton
    @Provides
    public Analyzer getRussianAnalyzer() {
        return new RussianAnalyzer();
    }

    protected void configureFullpipeline() {
        PipelinesConfiguration pipelineconfig = new PipelinesConfiguration();

        bind(FullPipeline.class).to(FullPipelineImpl.class).in(Singleton.class);
        pipelineconfig.addPipeline("default", "Regex Segmenter", RegexSentenceSplitter.class, RegexTokenizer.class);

        bindPipeline(pipelineconfig);
    }

    @Provides
    @Singleton
    public PartOfSpeechMapper getPosMapper() {
        PartOfSpeechMapperHashMapBacked mapper = new PartOfSpeechMapperHashMapBacked();
        return mapper;
    }
}
