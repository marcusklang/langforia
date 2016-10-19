package se.lth.cs.nlp.langforia.lang.sv;

import org.junit.Test;
import se.lth.cs.docforia.Document;
import se.lth.cs.docforia.graph.text.NamedEntity;
import se.lth.cs.docforia.io.text.TextDocumentWriterFactory;
import se.lth.cs.docforia.io.text.columns.*;
import se.lth.cs.nlp.langforia.common.RegexSentenceSplitter;
import se.lth.cs.nlp.langforia.ext.lucene.LuceneTokenizer;
import se.lth.cs.nlp.langforia.kernel.structure.FullPipeline;
import se.lth.cs.nlp.langforia.kernel.structure.PipelinesConfiguration;
import se.lth.cs.nlp.langforia.kernel.structure.Tokenizer;

import java.io.UnsupportedEncodingException;

import static se.lth.cs.docforia.graph.TokenProperties.*;
import static se.lth.cs.docforia.graph.TokenProperties.STEM;

public class SwedishTest {

    //Source: https://sv.wikipedia.org/wiki/Sverige
    private static final String text = "Sverige/ˈsværjɛ/ (officiellt Konungariket Sverige (info)) är ett nordiskt land " +
            "på Skandinaviska halvön i Nordeuropa. Sverige har landgräns i väst med Norge, i nordost med Finland samt " +
            "angränsande territorialvatten till Danmark i sydväst och Finland i öst. Landet har kuster mot Bottenviken," +
            " Bottenhavet, Ålands hav, Östersjön, Öresund, Kattegatt och Skagerrak.\n" +
            "\n" +
            "Sverige har en befolkning på cirka 9,9 miljoner invånare och är med en area på 447 435 km² det geografiskt" +
            " femte största landet i Europa.\n" +
            "\n" +
            "Huvudstad är Stockholm, landets största stad med 1,4 miljoner invånare. Därutöver finns ytterligare tre " +
            "storstäder enligt Statistiska centralbyråns definition om fler än 200 000 invånare: Göteborg, Malmö och " +
            "Uppsala. Omkring 85 procent av befolkningen, en ökande andel, lever i tätorter. Sverige har en låg " +
            "befolkningstäthet om 22 invånare per km², med högre täthet i södra landshalvan.";

    @Test
    public void test() throws UnsupportedEncodingException {

        SwedishLanguage lang = new SwedishLanguage();
        Document doc = lang.apply("", FullPipeline.class);

        TextDocumentWriterFactory writer = new TextDocumentWriterFactory();
        writer.addColumn(new SentenceTokenCounterWriter(0));
        writer.addColumn(new FormColumnWriter(1));
        writer.addColumn(new SequenceColumnRW(2, LEMMA, POS, CPOSTAG, FEATS, STOPWORD, NORMALIZED, STEM));
        writer.addColumn(new SpanColumnRW(NamedEntity.class, NamedEntity.PROPERTY_LABEL,-1));
        writer.addColumn(new DependencyRelationWriter(false));
        System.out.println(writer.write(doc));

    }

    public static class SwedishUserModule extends SwedishLanguageModule {
        @Override
        protected void configureComponents() {
            bind(Tokenizer.class).to(LuceneTokenizer.class);
            configureLucene();
            configureFullpipeline();
        }

        @Override
        protected void configureFullpipeline() {
            PipelinesConfiguration pipelineconfig = new PipelinesConfiguration();

            pipelineconfig.setDefaultPipeline(
                    RegexSentenceSplitter.class,
                    Tokenizer.class);

            bindPipeline(pipelineconfig);
        }
    }

    @Test
    public void testSpecialConfig() throws UnsupportedEncodingException {

        SwedishLanguage lang = new SwedishLanguage(new SwedishUserModule());
        Document doc = lang.apply(text, FullPipeline.class);

        TextDocumentWriterFactory writer = new TextDocumentWriterFactory();
        writer.addColumn(new SentenceTokenCounterWriter(0));
        writer.addColumn(new FormColumnWriter(1));
        writer.addColumn(new SequenceColumnRW(2, LEMMA, POS, CPOSTAG, FEATS, STOPWORD, NORMALIZED, STEM));
        writer.addColumn(new SpanColumnRW(NamedEntity.class, NamedEntity.PROPERTY_LABEL,-1));
        writer.addColumn(new DependencyRelationWriter(false));
        System.out.println(writer.write(doc));
    }

}
