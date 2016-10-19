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
package se.lth.cs.nlpforia;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.codestory.http.WebServer;
import net.codestory.http.payload.Payload;
import net.codestory.http.templating.ModelAndView;
import org.apache.commons.cli.*;
import se.lth.cs.docforia.Document;
import se.lth.cs.docforia.graph.TokenProperties;
import se.lth.cs.docforia.graph.disambig.NamedEntityDisambiguation;
import se.lth.cs.docforia.graph.text.DependencyRelation;
import se.lth.cs.docforia.graph.text.NamedEntity;
import se.lth.cs.docforia.graph.text.Token;
import se.lth.cs.docforia.io.text.TextDocumentWriterFactory;
import se.lth.cs.docforia.io.text.columns.*;
import se.lth.cs.docforia.memstore.MemoryDocument;
import se.lth.cs.docforia.memstore.MemoryDocumentFactory;
//import se.lth.cs.nlp.langforia.common.AnchorLookup;
import se.lth.cs.nlp.langforia.ext.wikipedia.WikipediaParser;
import se.lth.cs.nlp.langforia.kernel.Language;
import se.lth.cs.nlp.langforia.kernel.structure.Pipelines;
import se.lth.cs.nlp.langforia.kernel.structure.PipelinesConfiguration;
import se.lth.cs.nlp.langforia.lang.de.GermanLanguage;
import se.lth.cs.nlp.langforia.lang.en.EnglishLanguage;
import se.lth.cs.nlp.langforia.lang.es.SpanishLanguage;
import se.lth.cs.nlp.langforia.lang.fr.FrenchLanguage;
import se.lth.cs.nlp.langforia.lang.ru.RussianLanguage;
import se.lth.cs.nlp.langforia.lang.sv.SwedishLanguage;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Main class for the webserver
 */
public class App
{
    public static Map<String,Language> loadLanguages() {
        HashMap<String,Language> languages = new HashMap<>();
        languages.put("sv", new SwedishLanguage());
        languages.put("en", new EnglishLanguage());
        languages.put("fr", new FrenchLanguage());
        languages.put("es", new SpanishLanguage());
        languages.put("de", new GermanLanguage());
        languages.put("ru", new RussianLanguage());

        return Collections.unmodifiableMap(languages);
    }

    public static void main( String[] args ) throws Exception
    {
        Options options = new Options();
        options.addOption(Option.builder("p").hasArg(true).type(Integer.class).longOpt("port").build());

        DefaultParser parser = new DefaultParser();
        ObjectMapper mapper = new ObjectMapper();

        try {
            final Map<String,Language> langs = loadLanguages();

            HashMap<String,Set<String>> languageConfigs = new HashMap<>();
            HashMap<String,String> configFriendlyNames = new HashMap<>();

            for (String lang : langs.keySet()) {
                PipelinesConfiguration instance = langs.get(lang).getInstance(PipelinesConfiguration.class);
                languageConfigs.put(lang, instance.pipelines().stream().collect(Collectors.toSet()));

                for (String s : instance.pipelines()) {
                    configFriendlyNames.put(lang + "/" + s, lang + " / " + instance.friendlyName(s));
                }
            }

            final String configs = mapper.writeValueAsString(languageConfigs);
            final String friendlyNames = mapper.writeValueAsString(configFriendlyNames);

            CommandLine cmdline = parser.parse(options, args);

            new WebServer().configure(routes -> routes
                    .get("/", (context) -> {
                        return ModelAndView.of("index", "Configs", configs, "FriendlyNames", friendlyNames);
                    })
                    .get("/languages", (context) -> {
                        return langs.keySet();
                    })
                    .get("/:lang/", (context, lang) -> {
                        if(langs.get(lang) != null) {
                            PipelinesConfiguration instance = langs.get(lang).getInstance(PipelinesConfiguration.class);
                            return instance.pipelines();
                        } else {
                            return new Payload("text/html", "404 Could not find language", 404);
                        }
                    })
                    .get("/:lang/:config/", (context, lang, config) -> {
                        if(langs.get(lang) != null) {
                            return ModelAndView.of("docs");
                        } else {
                            return new Payload("text/html", "404 Could not find language", 404);
                        }
                    })
                    .get("/api", (context) -> {
                        return ModelAndView.of("docs");
                    })
                    .post("/:lang/:config/api/json", (context, lang, config) -> {
                        Language language;
                        if((language = langs.get(lang)) != null && languageConfigs.get(lang).contains(config)) {
                            Pipelines pipelines = language.getInstance(Pipelines.class);
                            MemoryDocument doc = new MemoryDocument("dynamic", context.request().content());
                            pipelines.apply(config, doc);
                            return new Payload("application/json; charset=utf-8", doc.toJson());
                        } else {
                            return new Payload("application/json", "{\"errorCode\":404,\"message\":\"Could not find language or configuration\"}", 404);
                        }
                    })
                    .post("/:lang/:config/api/binary", (context, lang, config) -> {
                        Language language;
                        if((language = langs.get(lang)) != null && languageConfigs.get(lang).contains(config)) {
                            Pipelines pipelines = language.getInstance(Pipelines.class);
                            MemoryDocument doc = new MemoryDocument("dynamic", context.request().content());
                            pipelines.apply(config, doc);
                            return new Payload("application/x-docforia", doc.toJson());
                        } else {
                            return new Payload("text/html", "404 Could not find language or configuration", 404);
                        }
                    })
                    .post("/:lang/:config/api/tsv", (context, lang, config) -> {
                        Language language;
                        if((language = langs.get(lang)) != null && languageConfigs.get(lang).contains(config)) {
                            Pipelines pipelines = language.getInstance(Pipelines.class);
                            MemoryDocument doc = new MemoryDocument("dynamic", new String(context.request().contentAsBytes(), "utf-8"));
                            pipelines.apply(config, doc);

                            ArrayList<String> columns = new ArrayList<String>();

                            TextDocumentWriterFactory dw = new TextDocumentWriterFactory();
                            dw.addColumn(new SentenceTokenCounterWriter());
                            columns.add("id");

                            dw.addColumn(new FormColumnWriter());
                            columns.add("form");

                            TreeSet<String> tokenColumns = new TreeSet<String>();

                            //Find token properties
                            doc.nodes(Token.class).forEach(t -> t.properties().forEach(e -> {
                                if(!e.getKey().equals(TokenProperties.NE)) {
                                    tokenColumns.add(e.getKey());
                                }
                            }));

                            columns.addAll(tokenColumns);
                            dw.addColumn(new SequenceColumnRW(tokenColumns.stream().collect(Collectors.toList())));

                            if(doc.store().nodeLayer(Document.nodeLayer(NamedEntity.class)).size() > 0) {
                                dw.addColumn(new SpanColumnRW(NamedEntity.class, NamedEntity.PROPERTY_LABEL,-1));
                                columns.add("ne-class");
                            }

                            if(doc.store().nodeLayer(Document.nodeLayer(NamedEntityDisambiguation.class)).size() > 0) {
                                dw.addColumn(new SpanColumnRW(NamedEntityDisambiguation.class, NamedEntityDisambiguation.IDENTIFIER_PROPERTY,-1));

                                dw.addColumn(new SpanColumnRW(NamedEntityDisambiguation.class, NamedEntityDisambiguation.SCORE_PROPERTY,-1));
                            }

                            if(doc.store().edgeLayer(Document.edgeLayer(DependencyRelation.class)).size() > 0) {
                                dw.addColumn(new DependencyRelationWriter(false));
                                columns.add("head");
                                columns.add("deprel");
                            }

                            /*
                            dw.setHeaderText(columns.stream().collect(Collectors.joining("\t")));
                            dw.setWriteHeader(true);*/

                            return new Payload("text/plain; charset=utf-8", dw.write(doc));
                        } else {
                            return new Payload("text/plain", "404 Could not find language or configuration", 404);
                        }
                    })
                    .post("/:lang/:config/api/annoviz", (context, lang, config) -> {
                        Language language;
                        if((language = langs.get(lang)) != null && languageConfigs.get(lang).contains(config)) {
                            Pipelines pipelines = language.getInstance(Pipelines.class);
                            MemoryDocument doc = new MemoryDocument("dynamic", context.request().content());
                            pipelines.apply(config, doc);
                            return new Payload("application/json; charset=utf-8", Document2VizJSON.toJson(doc));
                        } else {
                            return new Payload("text/html", "404 Could not find language or configuration", 404);
                        }
                    })
                    .post("/:lang/:config/api/wikipedia/annoviz", (context, lang, config) -> {
                        Language language;
                        if((language = langs.get(lang)) != null && languageConfigs.get(lang).contains(config)) {
                            Pipelines pipelines = language.getInstance(Pipelines.class);
                            WikipediaParser wikihtmlparser = new WikipediaParser(lang, MemoryDocumentFactory.getInstance(), WikipediaParser.Mode.WIKIPEDIA);
                            Document doc = wikihtmlparser.parse("dynamic", context.request().content());

                            pipelines.apply(config, doc);
                            //language.apply(doc, AnchorLookup.class);
                            return new Payload("application/json; charset=utf-8", Document2VizJSON.toJson(doc));
                        } else {
                            return new Payload("text/html", "404 Could not find language or configuration", 404);
                        }
                    })
                    .post("/:lang/:config/api/wikipedia/json", (context, lang, config) -> {
                        Language language;
                        if((language = langs.get(lang)) != null && languageConfigs.get(lang).contains(config)) {
                            Pipelines pipelines = language.getInstance(Pipelines.class);
                            WikipediaParser wikihtmlparser = new WikipediaParser(lang, MemoryDocumentFactory.getInstance(), WikipediaParser.Mode.WIKIPEDIA);
                            Document doc = wikihtmlparser.parse("dynamic", context.request().content());

                            pipelines.apply(config, doc);
                            //language.apply(doc, AnchorLookup.class);
                            return new Payload("application/json; charset=utf-8", doc.toJson());
                        } else {
                            return new Payload("text/html", "404 Could not find language or configuration", 404);
                        }
                    })
                    .post("/:lang/:config/api/wikipedia/binary", (context, lang, config) -> {
                        Language language;
                        if((language = langs.get(lang)) != null && languageConfigs.get(lang).contains(config)) {
                            Pipelines pipelines = language.getInstance(Pipelines.class);
                            WikipediaParser wikihtmlparser = new WikipediaParser(lang, MemoryDocumentFactory.getInstance(), WikipediaParser.Mode.WIKIPEDIA);
                            Document doc = wikihtmlparser.parse("dynamic", context.request().content());

                            pipelines.apply(config, doc);
                            //language.apply(doc, AnchorLookup.class);
                            return new Payload("application/x-docforia", doc.toBytes());
                        } else {
                            return new Payload("text/html", "404 Could not find language or configuration", 404);
                        }
                    })
            ).start(Integer.parseInt(cmdline.getOptionValue("p", "8080")));
        }
        catch (ParseException e) {
            e.printStackTrace();
            HelpFormatter helpFormatter = new HelpFormatter();
            PrintWriter output = new PrintWriter(System.out);
            helpFormatter.printUsage(output, 80, "webpipeline", options);
            output.flush();
        }
    }
}
