package se.lth.cs.nlp.langforia.lang.de;

import org.junit.Test;
import se.lth.cs.docforia.Document;
import se.lth.cs.docforia.graph.text.NamedEntity;
import se.lth.cs.docforia.io.text.TextDocumentWriterFactory;
import se.lth.cs.docforia.io.text.columns.*;
import se.lth.cs.docforia.memstore.MemoryDocumentFactory;
import se.lth.cs.nlp.langforia.kernel.Language;
import se.lth.cs.nlp.langforia.kernel.structure.FullPipeline;

import static se.lth.cs.docforia.graph.TokenProperties.*;

public class GermanTest {

    //Source: https://de.wikipedia.org/wiki/Deutschland
    private static String text
            = " Deutschland (Vollform: Bundesrepublik Deutschland) ist ein föderal verfasster Staat in Mitteleuropa, " +
            "der aus den 16 deutschen Ländern gebildet wird. Die Bundesrepublik ist ein freiheitlich-demokratischer " +
            "und sozialer Rechtsstaat und stellt die jüngste Ausprägung des deutschen Nationalstaates dar. " +
            "Bundeshauptstadt sowie bevölkerungsreichste deutsche Stadt ist Berlin. Weitere Metropolen sind Hamburg, " +
            "München, Köln und Frankfurt; größter Ballungsraum ist die Metropolregion Rhein-Ruhr.\n" +
            "\n" +
            "An Deutschland grenzen neun Staaten und naturräumlich im Norden die Gewässer der Nord- und Ostsee, im " +
            "Süden das Bergland der Alpen. Es liegt in der gemäßigten Klimazone. Eine Vielzahl von National- und " +
            "Naturparks sind im Land ausgewiesen. Mit rund 82,2 Millionen Einwohnern zählt es zu den dicht besiedelten " +
            "Flächenstaaten und gilt international als das Einwanderungsland mit der zweithöchsten Zahl von Migranten.\n" +
            "\n" +
            "Das seit dem 10. Jahrhundert bestehende Heilige Römische Reich bestand aus vielen Herrschaftsgebieten " +
            "und war der Ursprung des föderalen deutschen Staates. Dieser entstand erstmals mit der Gründung des " +
            "Deutschen Reichs im Jahr 1871. Nach Weimarer Republik und NS-Diktatur folgte eine Zeit der Veränderung " +
            "und Teilung des Staatsgebietes. Der Gründung der Bundesrepublik als westdeutschem Teilstaat mit " +
            "Ausrufung des Grundgesetzes am 23. Mai 1949 folgte die Gründung der sozialistischen DDR-Diktatur am 7. " +
            "Oktober 1949 als ostdeutschem Teilstaat. Seit der Wiedervereinigung beider Landesteile am 3. Oktober 1990" +
            " wird der Tag der Deutschen Einheit als nationaler Feiertag begangen.";

    public Language getLanguage() {
        return new GermanLanguage(MemoryDocumentFactory.getInstance());
    }

    @Test
    public void testDefault() {

        Language german = getLanguage();
        Document doc = german.apply(text, FullPipeline.class);

        TextDocumentWriterFactory writer = new TextDocumentWriterFactory();
        writer.addColumn(new SentenceTokenCounterWriter(0));
        writer.addColumn(new FormColumnWriter(1));
        writer.addColumn(new SequenceColumnRW(2, LEMMA, POS, CPOSTAG, FEATS, STOPWORD, NORMALIZED, STEM));
        writer.addColumn(new SpanColumnRW(NamedEntity.class, NamedEntity.PROPERTY_LABEL,-1));
        System.out.println(writer.write(doc));
    }
}
