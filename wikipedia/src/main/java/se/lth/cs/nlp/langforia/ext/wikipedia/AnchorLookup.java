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
package se.lth.cs.nlp.langforia.ext.wikipedia;

import com.google.inject.Inject;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import se.lth.cs.docforia.Document;
import se.lth.cs.docforia.graph.hypertext.Anchor;
import se.lth.cs.nlp.langforia.kernel.LanguageCode;
import se.lth.cs.nlp.langforia.kernel.Model;
import se.lth.cs.nlp.langforia.kernel.resources.Resource;
import se.lth.cs.nlp.langforia.kernel.structure.LanguageTool;

import java.io.BufferedReader;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;


public class AnchorLookup implements LanguageTool {
    public static final String MODEL = "ext.opennlp.wikipedia.anchor.lookup";

    private Object2ObjectOpenHashMap<String,String> lookup = new Object2ObjectOpenHashMap<>();
    private String language;

    @Inject
    public AnchorLookup(@Model(MODEL) Resource resource, @LanguageCode String language) {
        try {
            this.language = language;
            System.out.println("Loading anchor lookup map for " + language);
            BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(resource.binaryRead())));
            String line = null;
            while((line = reader.readLine()) != null) {
                String[] split = line.split("\t", 2);
                String source = split[0];
                String target = split[1];
                lookup.put(source, target);
            }
        } catch (IOException e) {
            throw new IOError(e);
        }
    }

    @Override
    public void apply(Document doc) {
        doc.nodeStream(Anchor.class).forEach(anchor -> {
	    if(anchor.getTarget().length() >= 15+language.length()) {
                String lookupTarget = anchor.getTarget().substring(15+language.length());
                String target = lookup.get("w:" + lookupTarget);
                if(target != null) {
                     if(target.startsWith("wd:")) {
                          anchor.setEntity("urn:wikidata:" + target.substring(3));
                     }
                     else if(target.startsWith("w:")) {
                          anchor.setEntity("urn:wikipedia:" + language + ":" + target.substring(2));
                     }
                 }
            }
        });
    }
}
