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
import gnu.trove.set.hash.THashSet;
import se.lth.cs.docforia.graph.text.Token;
import se.lth.cs.nlp.langforia.kernel.Model;
import se.lth.cs.nlp.langforia.kernel.resources.Resource;
import java.io.BufferedReader;
import java.io.IOError;
import java.io.IOException;
import java.util.Iterator;

public class SimpleWordSet implements WordSet {

    private final THashSet<String> words = new THashSet<>();

    public static final String MODEL_ID ="lang.common.SimpleWordSet.model";

    @Inject
    public SimpleWordSet(@Model(MODEL_ID) Resource resource) {
        BufferedReader reader = new BufferedReader(resource.textRead());
        String line;

        try {
            while( (line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if(trimmed.length() == 0)
                    continue;

                words.add(trimmed);
            }
        } catch (IOException e) {
            throw new IOError(e);
        }
    }

    @Override
    public boolean isWord(Token token) {
        return words.contains(token.text());
    }

    @Override
    public int count() {
        return words.size();
    }

    @Override
    public Iterator<String> iterator() {
        return words.iterator();
    }


}
