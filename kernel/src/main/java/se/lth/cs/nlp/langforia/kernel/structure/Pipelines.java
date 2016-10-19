package se.lth.cs.nlp.langforia.kernel.structure;
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
import se.lth.cs.nlp.langforia.kernel.Language;

import java.util.HashMap;
import java.util.Set;

public class Pipelines {

    private PipelinesConfiguration config;
    private Language language;

    @Inject
    public Pipelines(Language language, PipelinesConfiguration config) {
        this.config = config;
        this.language = language;
    }

    public Set<String> pipelines() {
        return config.pipelines();
    }

    public Document apply(String pipeline, Document doc) {
        for (Class<? extends LanguageTool> tool : config.get(pipeline)) {
            doc = language.apply(doc, tool);
        }
        return doc;
    }

    public Document apply(Document doc) {
        for (Class<? extends LanguageTool> tool : config.getDefault()) {
            doc = language.apply(doc, tool);
        }
        return doc;
    }
}
