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
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import se.lth.cs.docforia.Document;
import se.lth.cs.docforia.graph.TokenProperties;
import se.lth.cs.docforia.graph.text.Sentence;
import se.lth.cs.docforia.graph.text.Token;
import se.lth.cs.docforia.query.NodeTVar;
import se.lth.cs.docforia.query.Proposition;
import se.lth.cs.docforia.query.PropositionGroup;
import se.lth.cs.docforia.query.QueryCollectors;
import se.lth.cs.nlp.langforia.kernel.exceptions.LangforiaRuntimeException;
import se.lth.cs.nlp.langforia.kernel.structure.Stemmer;


import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class LuceneStemmer implements Stemmer {

	private final Analyzer analyzer;
	
	@Inject
	public LuceneStemmer(@NoStopWordFiltering Analyzer analyzer) {
		this.analyzer = analyzer;
	}
	
	@Override
	public void apply(Document doc) {
        try 
        {
			NodeTVar<Token> T = Token.var();
			NodeTVar<Sentence> S = Sentence.var();

			List<PropositionGroup> groups
					= doc.select(S, T)
					.where(T).coveredBy(S)
					.stream()
					.collect(QueryCollectors.groupBy(doc, S).orderByKey(S).orderByValue(T).collector());

			for (PropositionGroup group : groups)
			{
				List<Proposition> tokens = group.values();

				ArrayList<Integer> ranges = new ArrayList<Integer>();
				boolean first = true;

                int start = group.key(S).getStart();

				for(Proposition prop : tokens) {
					Token tok = prop.get(T);

					/*if(first)
						first = false;
					else
						sb.append(" ");*/

					ranges.add(tok.getStart()-start);
                    ranges.add(tok.getEnd()-start);
				}

				TokenStream stream = analyzer.tokenStream("contents", new StringReader(group.key(S).text()));
				OffsetAttribute offsetAttribute = stream.addAttribute(OffsetAttribute.class);
				CharTermAttribute charTermAttribute = stream.addAttribute(CharTermAttribute.class);

				stream.reset();
				int i = 0;

				while (stream.incrementToken() && i < ranges.size())
				{
					while(i < ranges.size() && offsetAttribute.startOffset() >= ranges.get(i+1)) {
						i += 2;
					}

					if(i >= ranges.size()) {
						break;
					}

					if(ranges.get(i) <= offsetAttribute.startOffset() && ranges.get(i+1) >= offsetAttribute.endOffset()) {
						String stem = tokens.get(i / 2).get(T).getPropertyOrDefault(TokenProperties.STEM, "");
						if(stem.length() > 0)
							stem += " ";

						tokens.get(i/2).get(T).putProperty(TokenProperties.STEM, stem + charTermAttribute.toString());
						//i += 2;
					}
				}
				stream.close();
			}
        }
        catch(IOException ex) {
            throw new LangforiaRuntimeException(ex);
        }
	}
}
