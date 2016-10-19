package se.lth.cs.nlp.langforia.lang.es;

import org.junit.Test;
import se.lth.cs.docforia.Document;
import se.lth.cs.docforia.graph.text.NamedEntity;
import se.lth.cs.docforia.io.text.TextDocumentWriterFactory;
import se.lth.cs.docforia.io.text.columns.*;
import se.lth.cs.docforia.memstore.MemoryDocument;
import se.lth.cs.docforia.memstore.MemoryDocumentFactory;
import se.lth.cs.nlp.langforia.kernel.Language;
import se.lth.cs.nlp.langforia.kernel.structure.FullPipeline;
import se.lth.cs.nlp.langforia.lang.es.SpanishLanguage;

import java.io.IOError;
import java.io.IOException;
import java.io.StringWriter;

import static se.lth.cs.docforia.graph.TokenProperties.*;

/**
 * Created by marcus on 2015-03-24.
 */
public class SpanishTest {
    public Language getLanguage() {
        return new SpanishLanguage(MemoryDocumentFactory.getInstance());
    }

    @Test
    public void testAnnotation() {

        Language spanish = getLanguage();

        //Source: https://es.wikipedia.org/wiki/Espa%C3%B1a
        String text = "España también denominado Reino de España, es un país soberano, miembro de la Unión Europea, " +
                "constituido en Estado social y democrático de derecho y cuya forma de gobierno es la monarquía " +
                "parlamentaria. Su territorio está organizado en diecisiete comunidades autónomas y dos ciudades " +
                "autónomas, además de cincuenta provincias. Su capital es la villa de Madrid.\n" +
                "\n" +
                "Es un país transcontinental que se encuentra situado tanto en Europa Occidental como en el norte de " +
                "África. En Europa ocupa la mayor parte de la península ibérica, conocida como España peninsular, y el " +
                "archipiélago de las islas Baleares (en el mar Mediterráneo occidental); en África se hallan las " +
                "ciudades de Ceuta (en la península Tingitana) y Melilla (en el cabo de Tres Forcas), las islas " +
                "Canarias (en el océano Atlántico nororiental), las islas Chafarinas (mar Mediterráneo), el peñón de " +
                "Vélez de la Gomera (mar Mediterráneo), las islas Alhucemas (golfo de las islas Alhucemas), y la isla " +
                "de Alborán (mar de Alborán). El municipio de Llivia, en los Pirineos, constituye un enclave rodeado" +
                " totalmente por territorio francés. Completa el conjunto de territorios una serie de islas e islotes" +
                " frente a las propias costas peninsulares.";

        Document doc = spanish.apply(text, FullPipeline.class);

        TextDocumentWriterFactory writer = new TextDocumentWriterFactory();
        writer.addColumn(new SentenceTokenCounterWriter(0));
        writer.addColumn(new FormColumnWriter(1));
        writer.addColumn(new SequenceColumnRW(2, LEMMA, POS, CPOSTAG, FEAT, STOPWORD, NORMALIZED, STEM));
        writer.addColumn(new SpanColumnRW(NamedEntity.class, NamedEntity.PROPERTY_LABEL,-1));
        writer.addColumn(new DependencyRelationWriter(false));
        System.out.println(writer.write(doc));
    }
}
