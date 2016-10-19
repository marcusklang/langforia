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

import se.lth.cs.docforia.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class PipelinesConfiguration {
    private HashMap<String,Class<? extends LanguageTool>[]> pipelines = new HashMap<>();
    private HashMap<String,String> friendlyName = new HashMap<>();

    public void setDefaultPipeline(Class<? extends LanguageTool>...tools) {
        this.pipelines.put("default", tools);
        this.friendlyName.put("default", "default");
    }

    public void setDefaultPipeline(String friendlyName, Class<? extends LanguageTool>...tools) {
        this.pipelines.put("default", tools);
        this.friendlyName.put("default", friendlyName);
    }

    public void addPipeline(String id, String friendlyName, Class<? extends LanguageTool>...tools) {
        this.pipelines.put(id, tools);
        this.friendlyName.put(id, friendlyName);
    }

    public Set<String> pipelines() {
        return pipelines.keySet();
    }

    public String friendlyName(String name) {
        return friendlyName.get(name);
    }

    public Class<? extends LanguageTool>[] get(String name) {
        return pipelines.get(name);
    }

    public Class<? extends LanguageTool>[] getDefault() {
        return pipelines.get("default");
    }
}
