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
package se.lth.cs.nlp.langforia.ext.lucene;

import com.google.inject.Inject;
import org.apache.lucene.analysis.util.CharArraySet;
import se.lth.cs.docforia.Document;
import se.lth.cs.docforia.graph.TokenProperties;
import se.lth.cs.docforia.graph.text.Token;
import se.lth.cs.docforia.query.NodeTVar;
import se.lth.cs.docforia.query.StreamUtils;
import se.lth.cs.nlp.langforia.kernel.Model;
import se.lth.cs.nlp.langforia.kernel.structure.TextNormalizer;

public class LuceneNormalizer implements TextNormalizer {

	private final CharArraySet stopWords;
	public static final String LUCENE_STOP_WORDS_MODEL = "ext.lucene.stopwords";
	
	@Inject
	public LuceneNormalizer(@Model(LUCENE_STOP_WORDS_MODEL) CharArraySet stopWords) {
		this.stopWords = stopWords;
	}

	@Override
	public void apply(Document doc) {
		NodeTVar<Token> T = Token.var();

		doc.select(T)
		   .where(T).property(TokenProperties.STEM).exists()
		   .stream().sorted(StreamUtils.orderBy(T)).map(StreamUtils.toNode(T)).forEach(tok -> {
			if(stopWords.contains(tok.getProperty(TokenProperties.STEM))) {
				tok.putProperty(TokenProperties.STOPWORD, true);
			}

			tok.putProperty(TokenProperties.NORMALIZED, tok.getPropertyOrDefault(TokenProperties.NORMALIZED, tok.getProperty(TokenProperties.STEM)).toLowerCase());
		});
	}
}
