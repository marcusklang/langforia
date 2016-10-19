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
package se.lth.cs.nlp.langforia.ext.maltparser;

import com.google.inject.Inject;
import org.maltparser.concurrent.ConcurrentMaltParserModel;
import org.maltparser.concurrent.ConcurrentMaltParserService;
import org.maltparser.core.exception.MaltChainedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.lth.cs.docforia.Document;
import se.lth.cs.docforia.graph.text.DependencyRelation;
import se.lth.cs.docforia.graph.text.Sentence;
import se.lth.cs.docforia.graph.text.Token;
import se.lth.cs.docforia.query.NodeTVar;
import se.lth.cs.docforia.query.PropositionGroup;
import se.lth.cs.docforia.query.QueryCollectors;
import se.lth.cs.nlp.langforia.kernel.Model;
import se.lth.cs.nlp.langforia.kernel.exceptions.LangforiaRuntimeException;
import se.lth.cs.nlp.langforia.kernel.resources.Resource;
import se.lth.cs.nlp.langforia.kernel.LanguageCode;
import se.lth.cs.nlp.langforia.kernel.structure.DependencyGrammarParser;
import static se.lth.cs.docforia.graph.TokenProperties.*;

import java.io.IOError;
import java.util.List;

public class MaltParser implements DependencyGrammarParser {

	public static final String MODEL_ID = "ext.maltparser.model";
	private final ConcurrentMaltParserModel model;
	private final String langcode;
	
	@Inject
	public MaltParser(@LanguageCode String langcode, @Model(MODEL_ID) Resource modelpath) {
		Logger logger = LoggerFactory.getLogger(MaltParser.class);

		logger.info("Loading maltparser model " + modelpath.name() + " for lang " + langcode);

		try {
			model = ConcurrentMaltParserService.initializeParserModel(modelpath.file().toURI().toURL());
			this.langcode = langcode;
		} catch (Exception e) {
			throw new IOError(e);
		}

		logger.info("Completed loading maltparser model " + modelpath.name() + " for lang " + langcode + ".");
	}
	
	@Override
	public void apply(Document doc) {
		NodeTVar<Token> T = Token.var();
		NodeTVar<Sentence> S = Sentence.var();
		List<PropositionGroup> groups = doc.select(T, S)
				.where(T).coveredBy(S)
				.stream()
				.collect(QueryCollectors.groupBy(doc, S).orderByKey(S).orderByValue(T).collector());

		for (PropositionGroup proposition : groups)
		{
			String[] sentence = new String[proposition.size()];

			//1    Innebär                  	 _ 	 VB  	 VB  	 PRS|AKT                         	 9  	 AA   	 _ 	 _
			for(int i = 0; i < proposition.size(); i++) {
				Token tok = proposition.value(i, T);

				StringBuilder sb = new StringBuilder();
				sb.append(i+1).append("\t");
				sb.append(tok.text()).append("\t");
				sb.append(tok.getPropertyOrDefault(LEMMA, "_")).append("\t");
				sb.append(tok.getPropertyOrDefault(POS, "_")).append("\t");
				sb.append(tok.getPropertyOrDefault(POS, "_")).append("\t");
				sb.append(tok.getPropertyOrDefault(FEATS, "_")).append("\t");
				sentence[i] = sb.toString();
			}

			try {
				String[] output = model.parseTokens(sentence);
				for(int i = 0; i < output.length; i++) {
					Token tok = proposition.value(i).get(T);

					String[] parts = output[i].split("\t");
					int headIndex = Integer.parseInt(parts[6])-1;
					/*tok.putProperty(HEAD, parts[6]);
					tok.putProperty(DEPREL, parts[7]);*/

					if(headIndex != -1) {
						new DependencyRelation(doc).connect(tok, proposition.value(headIndex, T)).setRelation(parts[7]);
					}
				}
			} catch (MaltChainedException e) {
				throw new LangforiaRuntimeException(langcode, e);
			}
		}
	}
}
