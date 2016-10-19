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
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTagger;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.util.InvalidFormatException;
import se.lth.cs.docforia.Document;
import se.lth.cs.docforia.graph.TokenProperties;
import se.lth.cs.docforia.graph.text.Sentence;
import se.lth.cs.docforia.graph.text.Token;
import se.lth.cs.docforia.query.NodeTVar;
import se.lth.cs.docforia.query.PropositionGroup;
import se.lth.cs.docforia.query.QueryCollectors;
import se.lth.cs.nlp.langforia.kernel.Model;
import se.lth.cs.nlp.langforia.kernel.resources.Resource;
import se.lth.cs.nlp.langforia.kernel.exceptions.LangforiaException;
import se.lth.cs.nlp.langforia.kernel.LanguageCode;
import se.lth.cs.nlp.langforia.kernel.structure.PartOfSpeechTagger;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class OpenNlpPartOfSpeechTagger implements PartOfSpeechTagger {
	private POSModel model;

	public final static String MODEL_ID = "ext.opennlp.pos.model";

	@Inject
	public OpenNlpPartOfSpeechTagger(@LanguageCode String lang, @Model(MODEL_ID) Resource vfsFile) throws LangforiaException {
		this(lang, vfsFile.binaryRead());
	}

    private ThreadLocal<POSTagger> taggers = new ThreadLocal<POSTagger>() {
        @Override
        protected POSTagger initialValue() {
            return new POSTaggerME(model);
        }
    };

    public OpenNlpPartOfSpeechTagger(String lang, InputStream modelInput) throws LangforiaException {
        try {
            model = new POSModel(modelInput);
            modelInput.close();
        } catch (InvalidFormatException e) {
            throw new LangforiaException("Failed to load OpenNLP model for language " + lang, e);
        } catch (IOException e) {
            throw new LangforiaException("Failed to load OpenNLP model for language " + lang, e);
        }
    }
	
	public void apply(Document doc) {
		NodeTVar<Token> T = Token.var();
		NodeTVar<Sentence> S = Sentence.var();

        POSTagger posTagger = taggers.get();

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

			String[] postags = posTagger.tag(tokens);
			for(i = 0; i <  proposition.size(); i++) {
				proposition.value(i, T).putProperty(TokenProperties.POS, postags[i]);
			}
		}
	}
}
