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
package se.lth.cs.nlp.langforia.lang.es;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.es.SpanishAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;
import se.lth.cs.nlp.langforia.common.*;
import se.lth.cs.nlp.langforia.ext.lucene.LuceneNormalizer;
import se.lth.cs.nlp.langforia.ext.lucene.LuceneStemmer;
import se.lth.cs.nlp.langforia.ext.lucene.LuceneTokenizer;
import se.lth.cs.nlp.langforia.ext.lucene.NoStopWordFiltering;
import se.lth.cs.nlp.langforia.ext.maltparser.MaltParser;
import se.lth.cs.nlp.langforia.ext.stanford.corenlp.StanfordMultilingualCoreNlpPipeline;
import se.lth.cs.nlp.langforia.kernel.LanguageModule;
import se.lth.cs.nlp.langforia.kernel.Models;
import se.lth.cs.nlp.langforia.kernel.resources.JarResource;
import se.lth.cs.nlp.langforia.kernel.structure.*;

/**
 * Created by marcusk on 2016-10-07.
 */
public class SpanishLanguageModule extends LanguageModule {
    public SpanishLanguageModule() {
        super();
    }

    protected boolean nerEnabled = true;
    protected boolean dependencyEnabled = false;

    @Override
    protected void configure() {
        super.configure();

        configureLucene();
        configureSegmentation();
        configureFullpipeline();
    }

    protected void configureSegmentation() {
        bind(WordSet.class).to(SimpleWordSet.class).in(Singleton.class);
        bindModel(SimpleWordSet.MODEL_ID, new JarResource("words.txt", "models/es/words.txt"));
        bind(MaltParser.class).in(Singleton.class);
    }

    protected void configureLucene() {
        bind(Tokenizer.class).to(LuceneTokenizer.class).in(Singleton.class);
        bind(Stemmer.class).to(LuceneStemmer.class).in(Singleton.class);
        bind(TextNormalizer.class).to(LuceneNormalizer.class).in(Singleton.class);
        bind(CoarsePosTagConverter.class).in(Singleton.class);
        bind(CharArraySet.class).annotatedWith(Models.named(LuceneNormalizer.LUCENE_STOP_WORDS_MODEL)).toInstance(SpanishAnalyzer.getDefaultStopSet());
    }

    @Singleton
    @Provides
    @NoStopWordFiltering
    public Analyzer getSpanishWithStopWordsAnalyzer() {
        return new SpanishAnalyzer(CharArraySet.EMPTY_SET);
    }

    @Singleton
    @Provides
    public Analyzer getSpanishAnalyzer() {
        return new SpanishAnalyzer();
    }

    protected void configureFullpipeline() {
        bind(FullPipeline.class).to(FullPipelineImpl.class).in(Singleton.class);
        bindModel(MaltParser.MODEL_ID, new JarResource("espmalt-1.0.mco", "models/es/maltparser/espmalt-1.0.mco"));

        bindProperty(StanfordMultilingualCoreNlpPipeline.PROPERTY_ENABLE_NER, nerEnabled);
        bindProperty(StanfordMultilingualCoreNlpPipeline.PROPERTY_INCLUDE_NER_DATE, false);
        bindProperty(StanfordMultilingualCoreNlpPipeline.PROPERTY_ENABLE_DEPENDENCY, dependencyEnabled);

        PipelinesConfiguration pipelines = new PipelinesConfiguration();
        pipelines.addPipeline("default", "Stanford CoreNLP",
                StanfordMultilingualCoreNlpPipeline.class,
                MaltParser.class,
                Stemmer.class,
                TextNormalizer.class,
                CoarsePosTagConverter.class);

        bindPipeline(pipelines);
    }

    private static final String[] PUNCT = new String[]{
            "Fa",
            "Fc",
            "Fd",
            "Fe",
            "Fg",
            "Fh",
            "Fi",
            "Fp",
            "Fs",
            "Fx",
            "Fz"
    };

    private static final String[] X = new String[]{"X", "Y"};
    private static final String[] NUM = new String[]{"Zm", "Zp", "z"};
    private static final String[] ADJ = new String[]{"ao", "aq"};
    private static final String[] CONJ = new String[]{"cc", "cs"};
    private static final String[] DET = new String[]{"da", "dd", "de", "di", "dn", "dp", "dt"};
    private static final String[] PROPN = new String[]{"np"};
    private static final String[] NOUN = new String[]{"nc", "w"};
    private static final String[] PRON = new String[]{
            "p0",
            "pd",
            "pe",
            "pi",
            "pn",
            "pp",
            "pr",
            "pt",
            "px"
    };
    private static final String[] ADV = new String[]{
            "rg",
            "rn"
    };

    private static final String[] ADP = new String[]{
            "sn",
            "sp"
    };

    private static final String[] VERB = new String[]{
            "va",
            "vm",
            "vs"
    };
    private static final String[] EMPTY = new String[]{};

    @Provides
    @Singleton
    public PartOfSpeechMapper getPosMapper() {
        return new PartOfSpeechMapper() {

            @Override
            public String[] toLanguage(String tag) {
                switch (tag) {
                    case ".":
                        return PUNCT;
                    case "X":
                        return X;
                    case "NUM":
                        return NUM;
                    case "ADJ":
                        return ADJ;
                    case "CONJ":
                        return CONJ;
                    case "DET":
                        return DET;
                    case "PROPN":
                        return PROPN;
                    case "NOUN":
                        return NOUN;
                    case "PRON":
                        return PRON;
                    case "ADV":
                        return ADV;
                    case "ADP":
                        return ADP;
                    case "VERB":
                        return VERB;
                    default:
                        return EMPTY;

                }
            }

            @Override
            public String fromLanguage(String tag) {
                /*
                        Fa	.
                        Fc	.
                        Fd	.
                        Fe	.
                        Fg	.
                        Fh	.
                        Fi	.
                        Fp	.
                        Fs	.
                        Fx	.
                        Fz	.
                 */
                if (tag.startsWith("f"))
                    return ".";

                /*
                    X	X
                    Y	X
                    i	X
                 */
                if (tag.startsWith("x") || tag.startsWith("y") || tag.startsWith("i"))
                    return "X";

                /*
                    Zm	NUM
                    Zp	NUM
                    z	NUM
                 */
                if (tag.startsWith("z"))
                    return "NUM";
                /*
                ao	ADJ
                aq	ADJ
                        */
                if (tag.startsWith("a"))
                    return "ADJ";

                /*
                cc	CONJ
                cs	CONJ
                */
                if (tag.startsWith("c"))
                    return "CONJ";

                /*
                da	DET
                dd	DET
                de	DET
                di	DET
                dn	DET
                dp	DET
                dt	DET
                */
                if (tag.startsWith("d"))
                    return "DET";

                //np	PROPN
                if (tag.startsWith("np"))
                    return "PROPN";

                /*
                nc	NOUN
                w	NOUN
                */
                if (tag.startsWith("nc") || tag.startsWith("w"))
                    return "NOUN";

                /*
                p0	PRON
                pd	PRON
                pe	PRON
                pi	PRON
                pn	PRON
                pp	PRON
                pr	PRON
                pt	PRON
                px	PRON
                */
                if (tag.startsWith("p"))
                    return "PRON";

                /*
                rg	ADV
                rn	ADV
                */
                if (tag.startsWith("r"))
                    return "ADV";

                /*
                sn	ADP
                sp	ADP
                */
                if (tag.startsWith("s"))
                    return "ADP";

                /*
                va	VERB
                vm	VERB
                vs	VERB
                */
                if (tag.startsWith("v"))
                    return "VERB";

                return null;
            }
        };
    }
}
