package se.lth.cs.nlp.langforia.lang.en;

import org.junit.Test;
import se.lth.cs.docforia.Document;
import se.lth.cs.docforia.graph.text.NamedEntity;
import se.lth.cs.docforia.io.text.TextDocumentWriterFactory;
import se.lth.cs.docforia.io.text.columns.*;
import se.lth.cs.docforia.memstore.MemoryDocumentFactory;
import se.lth.cs.nlp.langforia.common.RegexSentenceSplitter;
import se.lth.cs.nlp.langforia.ext.lucene.LuceneTokenizer;
import se.lth.cs.nlp.langforia.kernel.Language;
import se.lth.cs.nlp.langforia.kernel.structure.FullPipeline;
import se.lth.cs.nlp.langforia.kernel.structure.Pipelines;
import se.lth.cs.nlp.langforia.kernel.structure.PipelinesConfiguration;
import se.lth.cs.nlp.langforia.kernel.structure.Tokenizer;
import se.lth.cs.nlp.langforia.lang.en.EnglishLanguage;

import java.io.UnsupportedEncodingException;

import static se.lth.cs.docforia.graph.TokenProperties.*;
import static se.lth.cs.docforia.graph.TokenProperties.STEM;

public class EnglishTest {

    //Source: http://nlp.stanford.edu/software/tokenizer.html
    private static final String text = "Another ex-Golden Stater, Paul Stankowski from Oxnard, is contending\n" +
            "for a berth on the U.S. Ryder Cup team after winning his first PGA Tour\n" +
            "event last year and staying within three strokes of the lead through\n" +
            "three rounds of last month's U.S. Open. H.J. Heinz Company said it\n" +
            "completed the sale of its Ore-Ida frozen-food business catering to the\n" +
            "service industry to McCain Foods Ltd. for about $500 million.\n" +
            "It's the first group action of its kind in Britain and one of\n" +
            "only a handful of lawsuits against tobacco companies outside the\n" +
            "U.S. A Paris lawyer last year sued France's Seita SA on behalf of\n" +
            "two cancer-stricken smokers. Japan Tobacco Inc. faces a suit from\n" +
            "five smokers who accuse the government-owned company of hooking\n" +
            "them on an addictive product.";

    public Language getLanguage() {
        return new EnglishLanguage(MemoryDocumentFactory.getInstance());
    }

    @Test
    public void testClearNLP() {
        Language english = getLanguage();
        Document doc = english.getInstance(Pipelines.class).apply("clearnlp", english.newDocument("main", text));

        TextDocumentWriterFactory writer = new TextDocumentWriterFactory();
        writer.addColumn(new SentenceTokenCounterWriter(0));
        writer.addColumn(new FormColumnWriter(1));
        writer.addColumn(new SequenceColumnRW(2, LEMMA, POS, CPOSTAG, FEATS, STOPWORD, NORMALIZED, STEM));
        writer.addColumn(new SpanColumnRW(NamedEntity.class, NamedEntity.PROPERTY_LABEL,-1));
        writer.addColumn(new DependencyRelationWriter(false));
        System.out.println(writer.write(doc));
    }

    @Test
    public void testDefault() {
        Language english = getLanguage();
        Document doc = english.getInstance(Pipelines.class).apply("default", english.newDocument("main", text));

        TextDocumentWriterFactory writer = new TextDocumentWriterFactory();
        writer.addColumn(new SentenceTokenCounterWriter(0));
        writer.addColumn(new FormColumnWriter(1));
        writer.addColumn(new SequenceColumnRW(2, LEMMA, POS, CPOSTAG, FEATS, STOPWORD, NORMALIZED, STEM));
        writer.addColumn(new SpanColumnRW(NamedEntity.class, NamedEntity.PROPERTY_LABEL,-1));
        writer.addColumn(new DependencyRelationWriter(false));
        System.out.println(writer.write(doc));
    }

    @Test
    public void testDefaultExtra() {
        Language english = getLanguage();
        Document doc = english.getInstance(Pipelines.class).apply("default_extra", english.newDocument("main", text));

        TextDocumentWriterFactory writer = new TextDocumentWriterFactory();
        writer.addColumn(new SentenceTokenCounterWriter(0));
        writer.addColumn(new FormColumnWriter(1));
        writer.addColumn(new SequenceColumnRW(2, LEMMA, POS, CPOSTAG, FEATS, STOPWORD, NORMALIZED, STEM));
        writer.addColumn(new SpanColumnRW(NamedEntity.class, NamedEntity.PROPERTY_LABEL,-1));
        writer.addColumn(new DependencyRelationWriter(false));
        System.out.println(writer.write(doc));
    }
}
