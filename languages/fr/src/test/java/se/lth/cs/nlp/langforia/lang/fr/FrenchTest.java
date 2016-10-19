package se.lth.cs.nlp.langforia.lang.fr;

import org.junit.Test;
import se.lth.cs.docforia.Document;
import se.lth.cs.docforia.graph.text.NamedEntity;
import se.lth.cs.docforia.io.text.TextDocumentWriterFactory;
import se.lth.cs.docforia.io.text.columns.*;
import se.lth.cs.docforia.memstore.MemoryDocumentFactory;
import se.lth.cs.nlp.langforia.kernel.Language;
import se.lth.cs.nlp.langforia.kernel.structure.FullPipeline;

import java.io.IOError;
import java.io.IOException;
import java.io.StringWriter;

import static se.lth.cs.docforia.graph.TokenProperties.*;

/**
 * Created by marcus on 2015-03-24.
 */
public class FrenchTest {
    public Language getLanguage() {
        return new FrenchLanguage(MemoryDocumentFactory.getInstance());
    }

    @Test
    public void testAnnotation() {

        Language french = getLanguage();

        //Source: https://fr.wikipedia.org/wiki/France
        String text = "La France, officiellement la République française, est un État transcontinental souverain, " +
                "dont le territoire métropolitain est situé en Europe occidentale, et qui comporte également des " +
                "territoires dans les océans Indien, Atlantique et Pacifique9 ainsi que sur le continent sud-américain." +
                " Issue d'une histoire politique longue et mouvementée, c'est aujourd'hui une république " +
                "constitutionnelle unitaire ayant un régime semi-présidentiel. Elle a pour capitale Paris depuis 508 " +
                "et pour langue officielle le français depuis 1539. Ses actuelles monnaies sont l'euro et le franc " +
                "Pacifique sur ses territoires de l'océan Pacifique. La devise de la République est « Liberté, " +
                "Égalité, Fraternité » et son drapeau est constitué des trois couleurs nationales (bleu, blanc, rouge)" +
                " disposées en trois bandes verticales d'égale largeur. Son hymne national est La Marseillaise, chant" +
                " patriotique hérité de la Révolution française.\n" +
                "\n" +
                "Ancien pays formé au début du Haut Moyen Âge, la France tire son nom des Francs, peuple germanique " +
                "qui a institué les premiers fondements de son État sur les bases de la Gaule romaine. C'est au fil " +
                "des siècles, par des guerres, des mariages politiques et des unions souveraines, que cet État " +
                "monarchique et catholique va peu à peu constituer autour de lui une véritable fédération de provinces," +
                " qui finira par se cristalliser en une nation unique par une politique d'uniformisation administrative " +
                "et culturelle, portée à son aboutissement par la Révolution française de 1789 et la fin du régime féodal.";

        Document doc = french.apply(text, FullPipeline.class);

        TextDocumentWriterFactory writer = new TextDocumentWriterFactory();
        writer.addColumn(new SentenceTokenCounterWriter(0));
        writer.addColumn(new FormColumnWriter(1));
        writer.addColumn(new SequenceColumnRW(2, LEMMA, POS, CPOSTAG, FEAT, STOPWORD, NORMALIZED, STEM));
        writer.addColumn(new SpanColumnRW(NamedEntity.class, NamedEntity.PROPERTY_LABEL,-1));
        writer.addColumn(new DependencyRelationWriter(false));
        System.out.println(writer.write(doc));
    }
}
