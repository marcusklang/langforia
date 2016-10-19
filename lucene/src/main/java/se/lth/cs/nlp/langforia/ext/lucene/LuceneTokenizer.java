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
import se.lth.cs.docforia.graph.text.Token;
import se.lth.cs.nlp.langforia.kernel.exceptions.LangforiaRuntimeException;
import se.lth.cs.nlp.langforia.kernel.structure.LanguageTool;
import se.lth.cs.nlp.langforia.kernel.structure.Tokenizer;

import java.io.IOException;
import java.io.StringReader;


public class LuceneTokenizer implements Tokenizer, LanguageTool {

	private final Analyzer analyzer;
	
	@Inject
	public LuceneTokenizer(@NoStopWordFiltering Analyzer analyzer) {
		this.analyzer = analyzer;
	}
	
	@Override
	public void apply(Document doc) {
        try 
        {
            TokenStream stream = analyzer.tokenStream("contents", new StringReader(doc.text()));
            OffsetAttribute offsetAttribute = stream.addAttribute(OffsetAttribute.class);
            CharTermAttribute charTermAttribute = stream.addAttribute(CharTermAttribute.class);
            
            stream.reset();
            while (stream.incrementToken())
            {
            	Token tok = new Token(doc).setRange(offsetAttribute.startOffset(), offsetAttribute.endOffset());
            	tok.putProperty(TokenProperties.STEM, charTermAttribute.toString());
            }
            stream.close();
        }
        catch(IOException ex) {
            throw new LangforiaRuntimeException(ex);
        }
	}
}
