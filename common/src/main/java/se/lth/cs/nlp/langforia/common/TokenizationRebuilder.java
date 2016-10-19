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

import se.lth.cs.docforia.Document;
import se.lth.cs.docforia.graph.text.Token;

public class TokenizationRebuilder {

    public static Token[] rebuild(Document doc, String[] tokens) {
        Token[] rebuilt = new Token[tokens.length];
        String text = doc.text();

        int pos = 0;
        for(int i = 0; i < tokens.length; i++) {
            if(i + 1 < tokens.length) {
                //Look at 2 tokens, to make sure that there is no problems with skipped tokens.
                int match1 = text.indexOf(tokens[i], pos);
                int match2 = text.indexOf(tokens[i+1], pos);

                if(match1 == -1) {
                    //no match, means skip!
                    continue;
                }
                else
                {
                    if(match2 != -1) {
                        if(match1 > match2) {
                            //skip current (false match)
                            continue;
                        }
                        else {
                            pos = match1+tokens[i].length();
                            rebuilt[i] = new Token(doc).setRange(match1, match1+tokens[i].length());
                        }
                    }
                }
            }
            else {
                int match1 = text.indexOf(tokens[i], pos);
                if(match1 != -1) {
                    rebuilt[i] = new Token(doc).setRange(match1, match1 + tokens[i].length());
                }
            }
        }

        return rebuilt;
    }

}
