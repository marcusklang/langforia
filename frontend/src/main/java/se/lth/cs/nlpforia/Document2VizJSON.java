package se.lth.cs.nlpforia;
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import se.lth.cs.docforia.*;
import se.lth.cs.docforia.data.DataRef;
import se.lth.cs.docforia.data.DocArrayRef;
import se.lth.cs.docforia.data.DocRef;
import se.lth.cs.docforia.graph.TokenProperties;
import se.lth.cs.docforia.graph.disambig.EntityDisambiguation;
import se.lth.cs.docforia.graph.disambig.NamedEntityDisambiguation;
import se.lth.cs.docforia.graph.hypertext.Anchor;
import se.lth.cs.docforia.graph.text.*;

import java.io.IOError;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Converts the document model to a simpler JSON format.
 */
public class Document2VizJSON {
    private static final ObjectMapper mapper = new ObjectMapper();

    @JsonSerialize
    private static class NodeEntry {
        private int id;
        private int start;
        private int end;
        private String comment;
        private String label;
        private HashMap<String,String> properties = new HashMap<>();

        public NodeEntry() {
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getStart() {
            return start;
        }

        public void setStart(int start) {
            this.start = start;
        }

        public int getEnd() {
            return end;
        }

        public void addProperty(String key, String value) {
            properties.put(key, value);
        }

        public HashMap<String, String> getProperties() {
            return properties;
        }

        public void setEnd(int end) {
            this.end = end;
        }

        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }
    }

    @JsonSerialize
    private static class EdgeEntry {
        private int id;
        private int head;
        private int tail;
        private String comment;
        private String label;
        private HashMap<String,String> properties = new HashMap<>();

        public EdgeEntry() {
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getHead() {
            return head;
        }

        public void setHead(int head) {
            this.head = head;
        }

        public int getTail() {
            return tail;
        }

        public void setTail(int tail) {
            this.tail = tail;
        }

        public void addProperty(String key, String value) {
            properties.put(key, value);
        }

        public HashMap<String, String> getProperties() {
            return properties;
        }

        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }
    }

    @JsonSerialize
    private static class Page {
        private String uri;
        private String docid;
        private String title;
        private String text;
        private ArrayList<Integer> sentences = new ArrayList<>();
        private ArrayList<Integer> tokens = new ArrayList<>();
        private HashMap<String,ArrayList<NodeEntry>> nodelayers = new HashMap<>();
        private HashMap<String,ArrayList<EdgeEntry>> edgelayers = new HashMap<>();
        private HashMap<String,String> properties = new HashMap<>();
        private HashMap<String,Page> docProperties = new HashMap<>();
        private ArrayList<Object[]> id2layer = new ArrayList<>();

        public Page() {
        }

        public ArrayList<Object[]> getId2layer() {
            return id2layer;
        }

        public String getDocid() {
            return docid;
        }

        public void setDocid(String docid) {
            this.docid = docid;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public ArrayList<Integer> getSentences() {
            return sentences;
        }

        public void addSentence(int start, int end) {
            sentences.add(start);
            sentences.add(end);
        }

        public ArrayList<Integer> getTokens() {
            return tokens;
        }

        public void addToken(int start, int end) {
            tokens.add(start);
            tokens.add(end);
        }

        public HashMap<String, Page> getDocProperties() {
            return docProperties;
        }

        public void setDocProperties(HashMap<String, Page> docProperties) {
            this.docProperties = docProperties;
        }

        public HashMap<String, String> getProperties() {
            return properties;
        }

        public void addProperty(String key, String value) {
            this.properties.put(key, value);
        }

        public HashMap<String, ArrayList<NodeEntry>> getNodelayers() {
            return nodelayers;
        }

        public HashMap<String, ArrayList<EdgeEntry>> getEdgelayers() {
            return edgelayers;
        }

        public void addLayer(String layer, int start) {
            id2layer.add(new Object[] {start, layer});
        }

        public void addNodeEntry(String layer, NodeEntry nodeEntry) {
            ArrayList<NodeEntry> entries = nodelayers.get(layer);
            if(entries == null) {
                entries = new ArrayList<>();
                nodelayers.put(layer, entries);
            }

            entries.add(nodeEntry);
        }

        public void addEdgeEntry(String layer, EdgeEntry nodeEntry) {
            ArrayList<EdgeEntry> entries = edgelayers.get(layer);
            if(entries == null) {
                entries = new ArrayList<>();
                edgelayers.put(layer, entries);
            }

            entries.add(nodeEntry);
        }

        public String getUri() {
            return uri;
        }

        public void setUri(String uri) {
            this.uri = uri;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }
    }

    private static class BuilderContext {
        public int idcnt = 1;
        public Reference2IntOpenHashMap<StoreRef> ref = new Reference2IntOpenHashMap<>();
    }

    public static <T extends Node> void addNodeLayer(BuilderContext ctx, String layer, String variant, Document doc, Page page, Function<NodeRef, String> labelFormatter) {
        String layerName = layer;
        if(layer.startsWith("@")) {
            layerName = layerName.substring(1);
        } else {
            layerName = layerName.substring(layerName.lastIndexOf('.')+1);
        }

        layerName = layerName + (variant != null ? "#" + variant : "");

        page.addLayer(layerName, ctx.idcnt);

        for (NodeRef node : doc.engine().nodes(layer, variant)) {
            String label = labelFormatter.apply(node);
            NodeStore store = node.get();

            ctx.ref.put(node, ctx.idcnt);

            NodeEntry nodeEntry = new NodeEntry();
            nodeEntry.setId(ctx.idcnt++);
            nodeEntry.setStart(store.getStart());
            nodeEntry.setEnd(store.getEnd());

            for(Map.Entry<String, DataRef> propEntry : store.properties()) {
                nodeEntry.addProperty(propEntry.getKey(), propEntry.getValue().stringValue());
            }

            nodeEntry.setLabel(label);
            page.addNodeEntry(layerName, nodeEntry);
        }
    }

    public static <T extends Node> void addEdgeLayer(BuilderContext ctx, String layer, String variant, Document doc, Page page, Function<EdgeRef, String> labelFormatter) {
        String layerName = layer;
        if(layer.startsWith("@")) {
            layerName = layerName.substring(1);
        } else {
            layerName = layerName.substring(layerName.lastIndexOf('.')+1);
        }

        page.addLayer(layerName, ctx.idcnt);

        for (EdgeRef edge : doc.engine().edges(layer)) {
            String label = labelFormatter.apply(edge);
            EdgeStore store = edge.get();

            ctx.ref.put(edge, ctx.idcnt);

            EdgeEntry edgeEntry = new EdgeEntry();
            edgeEntry.setId(ctx.idcnt++);
            edgeEntry.setTail(ctx.ref.get(store.getTail()));
            edgeEntry.setHead(ctx.ref.get(store.getHead()));

            for(Map.Entry<String, DataRef> propEntry : store.properties()) {
                edgeEntry.addProperty(propEntry.getKey(), propEntry.getValue().stringValue());
            }

            edgeEntry.setLabel(label);
            page.addEdgeEntry(layerName, edgeEntry);
        }
    }

    public static String formatUri(String uri) {
        if(uri.startsWith("urn:wikidata:"))
            return uri.substring(13);
        else if(uri.startsWith("urn:wikipedia:")) {
            String pageLink = "wiki:" + uri.substring(uri.indexOf(':', 15)+1);
            if(pageLink.length() > 35)
                return pageLink.substring(0,35) + "...";
            else
                return pageLink;
        }
        else if(uri.toLowerCase().startsWith("http://")) {
            if(uri.length() > 40)
                return uri.substring(0,40) + "...";
            else
                return uri;
        }
        else
            return uri;
    }

/*
    public static String toJson(Document rec) {
        return toJson(rec, "main");
    }*/

    public static Page convert(Document doc) {

        Page page = new Page();
        page.title = doc.hasProperty(Document.PROP_TITLE) ? doc.getTitle() : doc.uri("urn:wikidata");
        page.uri = doc.uri("urn:wikidata");
        page.text = doc.text();

        BuilderContext ctx = new BuilderContext();

        for (Map.Entry<String, DataRef> entry : doc.store().properties()) {
            if(!(entry.getValue() instanceof DocRef || entry.getValue() instanceof DocArrayRef)) {
                page.addProperty(entry.getKey(), entry.getValue().stringValue());
            } else if(entry.getValue() instanceof DocRef) {
                page.docProperties.put(entry.getKey(), convert(((DocRef) entry.getValue()).documentValue()));
            }
        }

        for (Sentence sentence : doc.nodes(Sentence.class)) {
            page.addSentence(sentence.getStart(), sentence.getEnd());
        }

        page.addLayer("Token", ctx.idcnt);

        for (Token token : doc.nodes(Token.class)) {
            String label = token.getPropertyOrDefault(TokenProperties.POS, "Token");
            ctx.ref.put(token.getRef(), ctx.idcnt);

            NodeEntry nodeEntry = new NodeEntry();
            nodeEntry.setId(ctx.idcnt++);
            nodeEntry.setStart(token.getStart());
            nodeEntry.setEnd(token.getEnd());

            for(Map.Entry<String, DataRef> propEntry : token) {
                nodeEntry.addProperty(propEntry.getKey(), propEntry.getValue().stringValue());
            }

            nodeEntry.setLabel(label);
            page.addNodeEntry("Token", nodeEntry);
            page.addToken(token.getStart(), token.getEnd());
        }

        for (LayerRef layer : doc.engine().nodeLayerRefs()) {
            String s = layer.getLayer();
            if(s.equals(Token.class.getName()))
                continue;
            else if(s.equals(EntityDisambiguation.class.getName())) {
                addNodeLayer(ctx, s, layer.getVariant(), doc, page, ref -> formatUri(ref.get().getProperty(EntityDisambiguation.IDENTIFIER_PROPERTY)));
            } else if(s.equals(Anchor.class.getName())) {
                addNodeLayer(ctx, s, layer.getVariant(), doc, page, ref -> formatUri(ref.get().hasProperty(Anchor.ENTITY_PROPERTY) ? ref.get().getProperty(Anchor.ENTITY_PROPERTY) : ref.get().getProperty(Anchor.TARGET_PROPERTY)));
            } else if(s.equals(NamedEntity.class.getName())) {
                addNodeLayer(ctx, s, layer.getVariant(), doc, page, ref -> formatUri(ref.get().hasProperty(NamedEntity.PROPERTY_LABEL) ? ref.get().getProperty(NamedEntity.PROPERTY_LABEL) : "NE"));
            } else if(s.equals(Predicate.class.getName())) {
                addNodeLayer(ctx, s, layer.getVariant(), doc, page, ref -> formatUri(ref.get().hasProperty(Predicate.PROPERTY_SENSE) ? ref.get().getProperty(Predicate.PROPERTY_SENSE) : "PREDICATE"));
            } else if(s.equals(NamedEntityDisambiguation.class.getName())) {
                addNodeLayer(ctx, s, layer.getVariant(), doc, page, ref -> formatUri(ref.get().getProperty(NamedEntityDisambiguation.IDENTIFIER_PROPERTY)));
            } else if(s.equals(EntityDisambiguation.class.getName())) {
                addNodeLayer(ctx, s, layer.getVariant(), doc, page, ref -> formatUri(ref.get().getProperty(EntityDisambiguation.IDENTIFIER_PROPERTY)));
            }
            else {
                String label;
                if(s.startsWith("@"))
                    label = s.substring(1);
                else
                    label = s.substring(s.lastIndexOf(".")+1);

                addNodeLayer(ctx, s, layer.getVariant(), doc, page, ref -> label);
            }
        }

        for (String s : doc.engine().edgeLayers()) {
            if(s.equals(DependencyRelation.class.getName())) {
                addEdgeLayer(ctx, s, null, doc, page, ref -> formatUri(ref.get().getProperty(DependencyRelation.RELATION_PROPERTY)));
            } else if(s.equals(SemanticRole.class.getName())) {
                addEdgeLayer(ctx, s, null, doc, page, ref -> formatUri(ref.get().getProperty(SemanticRole.ROLE_PROPERTY)));
            } else {
                String label;
                if(s.startsWith("@"))
                    label = s.substring(1);
                else
                    label = s.substring(s.lastIndexOf(".")+1);

                addEdgeLayer(ctx, s, null, doc, page, ref -> label);
            }
        }

        return page;
    }

    public static String toJson(Document rec) {
        try {
            return mapper.writeValueAsString(convert(rec));
        } catch (IOException e) {
            throw new IOError(e);
        }
    }
}
