package se.lth.cs.nlp.langforia.lang.ru;

import org.junit.Test;
import se.lth.cs.docforia.Document;
import se.lth.cs.docforia.graph.text.NamedEntity;
import se.lth.cs.docforia.io.text.TextDocumentWriterFactory;
import se.lth.cs.docforia.io.text.columns.*;
import se.lth.cs.docforia.memstore.MemoryDocumentFactory;
import se.lth.cs.nlp.langforia.kernel.Language;
import se.lth.cs.nlp.langforia.kernel.structure.FullPipeline;

import static se.lth.cs.docforia.graph.TokenProperties.*;

public class RussianTest {
    //Source: https://ru.wikipedia.org/wiki/%D0%A0%D0%BE%D1%81%D1%81%D0%B8%D1%8F
    private static String text = "Росси́я (от греч. Ρωσία — Русь), официально также Росси́йская Федера́ция (на " +
            "практике используется и аббревиатура РФ) — государство в Восточной Европе и Северной Азии. Население " +
            "— 146 544 710 чел. (2016). Территория России, определяемая её Конституцией, составляет 17 125 191 км²." +
            " Занимает первое место в мире по территории, шестое — по объёму ВВП по ППС и девятое — по численности" +
            " населения.\n" +
            "\n" +
            "Столица — Москва. Государственный язык — русский.\n" +
            "\n" +
            "Смешанная республика федеративного устройства. В мае 2012 года пост президента занял Владимир " +
            "Владимирович Путин, председателя правительства — Дмитрий Анатольевич Медведев.\n" +
            "\n" +
            "В состав Российской Федерации входят 85 субъектов, 46 из которых именуются областями, 22 — республиками" +
            ", 9 — краями, 3 — городами федерального значения, 4 — автономными округами и 1 — автономной областью.\n" +
            "\n" +
            "Россия граничит с восемнадцатью странами (самый большой показатель в мире), включая две частично " +
            "признанные и две непризнанные";


    public Language getLanguage() {
        return new RussianLanguage(MemoryDocumentFactory.getInstance());
    }

    @Test
    public void testAnnotation() {

        Language russian = getLanguage();
        Document doc = russian.apply(text, FullPipeline.class);

        TextDocumentWriterFactory writer = new TextDocumentWriterFactory();
        writer.addColumn(new SentenceTokenCounterWriter(0));
        writer.addColumn(new FormColumnWriter(1));
        writer.addColumn(new SequenceColumnRW(2, LEMMA, POS, CPOSTAG, FEAT, STOPWORD, NORMALIZED, STEM));
        writer.addColumn(new SpanColumnRW(NamedEntity.class, NamedEntity.PROPERTY_LABEL,-1));
        writer.addColumn(new DependencyRelationWriter(false));
        System.out.println(writer.write(doc));
    }
}
