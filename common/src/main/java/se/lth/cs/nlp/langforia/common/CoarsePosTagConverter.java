package se.lth.cs.nlp.langforia.common;
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

import com.google.inject.Inject;
import se.lth.cs.docforia.Document;
import se.lth.cs.docforia.graph.TokenProperties;
import se.lth.cs.docforia.graph.text.Token;
import se.lth.cs.nlp.langforia.kernel.structure.LanguageTool;
import se.lth.cs.nlp.langforia.kernel.structure.PartOfSpeechMapper;

public class CoarsePosTagConverter implements LanguageTool {
    private final PartOfSpeechMapper mapper;

    @Inject
    public CoarsePosTagConverter(PartOfSpeechMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void apply(Document doc) {
        for (Token token : doc.nodes(Token.class)) {
            String posTag = token.getProperty(TokenProperties.POS);
            if(posTag != null) {
                String result = mapper.fromLanguage(posTag);
                if(result != null)
                {
                    token.putProperty(TokenProperties.CPOSTAG, result);
                }
            }
        }
    }
}
