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
package se.lth.cs.nlp.langforia.ext.opennlp;

import com.google.inject.Inject;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.util.Span;
import se.lth.cs.docforia.Document;
import se.lth.cs.docforia.graph.text.NamedEntity;
import se.lth.cs.docforia.graph.text.Sentence;
import se.lth.cs.docforia.graph.text.Token;
import se.lth.cs.docforia.query.NodeTVar;
import se.lth.cs.docforia.query.PropositionGroup;
import se.lth.cs.docforia.query.QueryCollectors;
import se.lth.cs.nlp.langforia.kernel.Model;
import se.lth.cs.nlp.langforia.kernel.resources.Resource;
import se.lth.cs.nlp.langforia.kernel.structure.NamedEntityRecognizer;


import java.io.IOError;
import java.io.IOException;
import java.util.List;

public class OpenNlpNameFinder implements NamedEntityRecognizer {

    public static final String LOCATION_MODEL = "ext.opennlp.name.location.model";
    public static final String ORGANIZATION_MODEL = "ext.opennlp.name.org.model";
    public static final String PERSON_MODEL = "ext.opennlp.name.person.model";

    private TokenNameFinderModel models[] = new TokenNameFinderModel[3];

    @Inject
    public OpenNlpNameFinder(
            @Model(LOCATION_MODEL)     Resource location,
            @Model(ORGANIZATION_MODEL) Resource org,
            @Model(PERSON_MODEL)       Resource person)
    {
        try {
            models[0] = new TokenNameFinderModel(location.binaryRead());
            models[1] = new TokenNameFinderModel(org.binaryRead());
            models[2] = new TokenNameFinderModel(person.binaryRead());
        } catch (IOException e) {
            throw new IOError(e);
        }
    }

    private ThreadLocal<NameFinderME[]> finders = new ThreadLocal<NameFinderME[]>() {
        @Override
        protected NameFinderME[] initialValue() {
            return new NameFinderME[] {
                    new NameFinderME(models[0]),
                    new NameFinderME(models[1]),
                    new NameFinderME(models[2])
            };
        }
    };

    @Override
    public void apply(Document doc) {
        NameFinderME[] nameFinderMEs = finders.get();

        for(int k = 0; k < nameFinderMEs.length; k++) {
            NodeTVar<Token> T = Token.var();
            NodeTVar<Sentence> S = Sentence.var();

            List<PropositionGroup> query =
                    doc.select(S, T)
                       .where(T)
                       .coveredBy(S)
                       .stream()
                       .collect(QueryCollectors.groupBy(doc, S).orderByKey(S).orderByValue(T).collector());

            for (PropositionGroup proposition : query) {
                String[] tokens = new String[proposition.size()];

                int i = 0;
                for (Token item : proposition.nodes(T)) {
                    tokens[i++] = item.text();
                }

                Span[] spans = nameFinderMEs[k].find(tokens);
                for (Span span : spans) {
                    new NamedEntity(doc).setLabel(span.getType())
                                        .setRange(proposition.value(span.getStart(), T).getStart(), proposition.value(span.getEnd() - 1, T).getEnd());
                }
            }
        }

        for (int i = 0; i < nameFinderMEs.length; i++) {
            nameFinderMEs[i].clearAdaptiveData();
        }
    }
}
