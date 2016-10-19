package se.lth.cs.nlp.langforia.ext.clearnlp;
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

import com.clearnlp.component.AbstractComponent;
import com.clearnlp.dependency.DEPArc;
import com.clearnlp.dependency.DEPNode;
import com.clearnlp.dependency.DEPTree;
import com.clearnlp.dependency.srl.SRLArc;
import com.clearnlp.nlp.NLPGetter;
import com.clearnlp.nlp.NLPMode;
import com.clearnlp.reader.AbstractReader;
import com.google.inject.Inject;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import se.lth.cs.docforia.Document;
import se.lth.cs.docforia.graph.TokenProperties;
import se.lth.cs.docforia.graph.text.*;
import se.lth.cs.docforia.query.NodeTVar;
import se.lth.cs.docforia.query.PropositionGroup;
import se.lth.cs.docforia.query.QueryCollectors;
import se.lth.cs.nlp.langforia.kernel.structure.DependencyGrammarParser;
import se.lth.cs.nlp.langforia.kernel.structure.PartOfSpeechTagger;
import se.lth.cs.nlp.langforia.kernel.structure.SemanticRoleLabeller;

import java.util.List;
import java.util.stream.Collectors;

public class ClearNLP implements PartOfSpeechTagger, DependencyGrammarParser, SemanticRoleLabeller {

    private static final AbstractComponent[] pipeline = setupComponents();

    private static AbstractComponent[] setupComponents() {
        AbstractComponent[] components = null;

        try {
            String language = AbstractReader.LANG_EN;
            String modelType = "general-en";

            AbstractComponent tagger = NLPGetter.getComponent(modelType, language, NLPMode.MODE_POS);
            AbstractComponent morphological = NLPGetter.getComponent(modelType, language, NLPMode.MODE_MORPH);
            AbstractComponent parser = NLPGetter.getComponent(modelType, language, NLPMode.MODE_DEP);
            AbstractComponent identifier = NLPGetter.getComponent(modelType, language, NLPMode.MODE_PRED);
            AbstractComponent classifier = NLPGetter.getComponent(modelType, language, NLPMode.MODE_ROLE);
            AbstractComponent labeler = NLPGetter.getComponent(modelType, language, NLPMode.MODE_SRL);
            components = new AbstractComponent[]{tagger, morphological, parser, identifier, classifier, labeler};//, identifier, classifier, labeler};
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return components;
    }

    @Inject
    public ClearNLP() {

    }

    @Override
    public void apply(Document doc) {
        Object2IntOpenHashMap<String> counts = new Object2IntOpenHashMap<>();
        doc.putTag("clearnlp", counts);

        int parseErrorAcc = 0;
        int parsePredicateAcc = 0;

        NodeTVar<Sentence> S = Sentence.var();
        NodeTVar<Token> T = Token.var();
        for (PropositionGroup sentences : doc.select(S, T).where(T).coveredBy(S).stream().collect(QueryCollectors.groupBy(doc, S).orderByValue(T).collector())) {
            List<Token> tokens = sentences.list(T);
            List<String> strTokens = sentences.list(T).stream().map(Token::text).collect(Collectors.toList());

            DEPTree tree = NLPGetter.toDEPTree(strTokens);
            for (AbstractComponent component : pipeline)
                component.process(tree);

            for (int i = 0; i < tokens.size(); i++) {
                DEPNode cleartok = tree.get(i+1);
                Token tok = tokens.get(i);
                tok.setPartOfSpeech(cleartok.pos);
                String lemma = cleartok.lemma;
                if(lemma != null) {
                    tok.setLemma(lemma);
                }

                String morph = cleartok.getFeats().entrySet().stream().map(e -> e.getKey() + "=" + e.getValue()).collect(Collectors.joining("|"));
                if(!morph.isEmpty()) {
                    tok.setFeatures(morph);
                }
            }

            for (DEPNode depNode : tree) {
                if(depNode.id != 0) {
                    DEPArc headArc = depNode.getHeadArc();
                    Token tok = tokens.get(depNode.id - 1);
                    if(headArc.getNode().id == 0) {
                        tok.putProperty(TokenProperties.HEAD, 0);
                        tok.putProperty(TokenProperties.DEPREL, headArc.getLabel());
                    } else {
                        tok.putProperty(TokenProperties.HEAD, headArc.getNode().id - 1);
                        tok.putProperty(TokenProperties.DEPREL, headArc.getLabel());
                        new DependencyRelation(doc).connect(tok, tokens.get(headArc.getNode().id - 1)).setRelation(headArc.getLabel());
                    }
                }
            }

            int i=0;
            for(String roleSet:tree.getRolesetIDs()) {
                if(roleSet != null) {
                    int predicateIndex = i - 1;
                    if(predicateIndex < 0 || predicateIndex >= tokens.size()) {
                        parseErrorAcc += 1;
                        continue;
                    }

                    Token token = tokens.get(predicateIndex);
                    Predicate predicate = new Predicate(doc).setRange(token.getStart(), token.getEnd());
                    predicate.setSense(roleSet);
                    parsePredicateAcc++;
                }
                i++;
            }

            for(int j=1; j<tree.size(); j++) {
                for (SRLArc arc : tree.get(j).getSHeads()) {
                    int childIndex = j - 1;
                    int headIndex = arc.getNode().id - 1;

                    if(childIndex < 0 || childIndex >= tokens.size()) {
                        parseErrorAcc += 1;
                        continue;
                    }

                    if(headIndex < 0 || headIndex >= tokens.size()) {
                        parseErrorAcc += 1;
                        continue;
                    }

                    String label = arc.getLabel();
                    if(!arc.getFunctionTag().isEmpty())
                        label = label + "-" + arc.getFunctionTag();

                    Token child = tokens.get(childIndex);
                    Token head = tokens.get(headIndex);
                    child.connect(head, new SemanticRole(doc).setRole(label));
                }
            }
        }

        counts.put("parseErrorAcc", parseErrorAcc);
        counts.put("parsePredicateAcc", parsePredicateAcc);
    }
}
