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
package se.lth.cs.nlp.langforia.ext.stagger;

import com.google.inject.Inject;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.lth.cs.docforia.Document;
import se.lth.cs.docforia.graph.text.*;
import se.lth.cs.docforia.query.NodeTVar;
import se.lth.cs.docforia.query.PropositionGroup;
import se.lth.cs.docforia.query.QueryCollectors;
import se.lth.cs.nlp.langforia.kernel.Model;
import se.lth.cs.nlp.langforia.kernel.resources.Resource;
import se.lth.cs.nlp.langforia.kernel.exceptions.LangforiaException;
import se.lth.cs.nlp.langforia.kernel.LanguageCode;
import se.su.ling.stagger.*;
import se.su.ling.stagger.Token;

import static se.lth.cs.docforia.graph.TokenProperties.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Stagger {

	public static final String MODEL_ID = "lang.common.Stagger";

	private Logger logger;
	private Tagger tagger;
	private String lang;
	private TagSet posTagSet;
	private TagSet neTagSet;
	private TagSet neTagTypeSet;

	private Stagger(String lang) {
		if(lang == null)
			throw new NullPointerException("lang must not be null!");

		this.logger = LoggerFactory.getLogger(Stagger.class);
		this.lang = lang;
	}

	@Inject
	public Stagger(@LanguageCode String lang, @Model(MODEL_ID) Resource path) throws LangforiaException {
		this(lang);

		Logger logger = LoggerFactory.getLogger(Stagger.class);

		logger.info("Loading stagger model " + path.name());
		try
		{
			ObjectInputStream modelReader = new ObjectInputStream(new BufferedInputStream(new BZip2CompressorInputStream(path.binaryRead()), 1024*1024));
			tagger = (Tagger)modelReader.readObject();
			modelReader.close();
		}
		catch(Exception ex)
		{
			throw new LangforiaException("Failed to load stagger model.", ex);
		}

		posTagSet = tagger.getTaggedData().getPosTagSet();
		neTagSet = tagger.getTaggedData().getNETagSet();
		neTagTypeSet = tagger.getTaggedData().getNETypeTagSet();
		logger.info("Stagger model loaded.");
	}

	private se.su.ling.stagger.Tokenizer getTokenizer(Reader reader) {
		if(lang.equals("sv"))
			return new SwedishTokenizer(reader);
		else
			return new LatinTokenizer(reader);
	}

	private TaggedToken[] createTaggedToken(final List<se.lth.cs.docforia.graph.text.Token> sentenceList) {
		TaggedToken[] sentence = new TaggedToken[sentenceList.size()];
		int j = 0;
		for(se.lth.cs.docforia.graph.text.Token tok : sentenceList) {
			Token token = new Token(Token.TOK_LATIN, tok.text(), tok.getStart());
			sentence[j++] = new TaggedToken(token, String.valueOf(j));
		}

		return sentence;
	}

	private void fillTaggedTokens(final Sentence sentence, final List<se.lth.cs.docforia.graph.text.Token> sentenceList) {
		if(!sentence.hasTag("stagger.isTagged"))
			tag(sentence, sentenceList);
	}

	private void tag(final Sentence sentence, final List<se.lth.cs.docforia.graph.text.Token> sentenceList) {
		TaggedToken[] tokens = createTaggedToken(sentenceList);
		tokens = tagger.tagSentence(tokens, true, false);

		for(int i = 0; i < tokens.length; i++) {
			TaggedToken token = tokens[i];
			se.lth.cs.docforia.graph.text.Token tok = sentenceList.get(i);
			tok.putTag("stagger.tagged-token", tokens[i]);

			String[] pos = null;
			if(token.posTag >= 0) {
				try {
					pos = posTagSet.getTagName(token.posTag).split("\\|",2);
				} catch (TagNameException e) {
					logger.info("Failed to get tag name: {}", token.posTag);
				}
			}

			if(pos != null)
			{
				tok.putProperty(POS, pos[0]);
				if(pos.length > 1) {
					tok.putProperty(FEATS, pos[1]);
				}
			}
		}

		sentence.putTag("stagger.isTagged", Boolean.TRUE);
	}

	private void lemma(final List<se.lth.cs.docforia.graph.text.Token> sentence) {
		for(int i = 0; i < sentence.size(); i++) {
			TaggedToken token = sentence.get(i).getTag("stagger.tagged-token");
			se.lth.cs.docforia.graph.text.Token tok = sentence.get(i);

			tok.putProperty(LEMMA, token.lf);
		}
	}

	private void ner(Document doc, final List<se.lth.cs.docforia.graph.text.Token> sentence) throws TagNameException {
		for (int i = 0; i < sentence.size(); i++) {
			TaggedToken token = sentence.get(i).getTag("stagger.tagged-token");
			se.lth.cs.docforia.graph.text.Token startTok = sentence.get(i);

			String ne = "O";
			if(token.neTag >= 0)
				ne = neTagSet.getTagName(token.neTag);

			String neType = "";
			if(token.neTypeTag >= 0)
				neType = neTagTypeSet.getTagName(token.neTypeTag);

			if(ne.equals("B"))
			{
				i++;
				while(i < sentence.size() && neTagSet.getTagName(sentence.get(i).<TaggedToken>getTag("stagger.tagged-token").neTag).equals("I")) {
					i++;
				}
				i--;

				se.lth.cs.docforia.graph.text.Token endTok = sentence.get(i);
				new NamedEntity(doc).setLabel(neType).setRange(startTok.getStart(), endTok.getEnd());
			}
		}
	}

	public static class Tokenizer implements se.lth.cs.nlp.langforia.kernel.structure.Tokenizer {
		private final Stagger stagger;

		@Inject
		public Tokenizer(Stagger stagger) {
			this.stagger = stagger;
		}

		@Override
		public void apply(Document doc) {
			se.su.ling.stagger.Tokenizer tokenizer = stagger.getTokenizer(new StringReader(doc.text()));
			try
			{
				ArrayList<Token> sentence;
				while((sentence = tokenizer.readSentence()) != null)
				{
					int tokcounter = 1;
					for(Token tok : sentence) {
						se.lth.cs.docforia.graph.text.Token token = new se.lth.cs.docforia.graph.text.Token(doc).setRange(tok.offset, tok.offset+tok.value.length());
						token.putProperty(ID, String.valueOf(tokcounter));
						tokcounter++;
					}
				}
			}
			catch(IOException ex) {
				throw new IOError(ex);
			}
		}
	}

	public static class Segmenter implements se.lth.cs.nlp.langforia.kernel.structure.TextSegmenter {
		private final Stagger stagger;

		@Inject
		public Segmenter(Stagger stagger) {
			this.stagger = stagger;
		}

		@Override
		public void apply(Document doc) {
			se.su.ling.stagger.Tokenizer tokenizer = stagger.getTokenizer(new StringReader(doc.text()));
			try
			{
				ArrayList<Token> sentence;
				while((sentence = tokenizer.readSentence()) != null)
				{
					int tokcounter = 1;
					for(Token tok : sentence) {
						se.lth.cs.docforia.graph.text.Token token = new se.lth.cs.docforia.graph.text.Token(doc).setRange(tok.offset, tok.offset+tok.value.length());
						token.putProperty(ID, String.valueOf(tokcounter));
						tokcounter++;
					}
					Token lastToken = sentence.get(sentence.size()-1);

					new Sentence(doc).setRange(sentence.get(0).offset, lastToken.offset + lastToken.value.length());
				}
			}
			catch(IOException ex) {
				throw new IOError(ex);
			}
		}
	}

	public static class SentenceSplitter implements se.lth.cs.nlp.langforia.kernel.structure.SentenceSplitter {
		private final Stagger stagger;

		@Inject
		public SentenceSplitter(Stagger stagger) {
			this.stagger = stagger;
		}

		@Override
		public void apply(Document doc) {
			se.su.ling.stagger.Tokenizer tokenizer = stagger.getTokenizer(new StringReader(doc.text()));
			try
			{
				ArrayList<Token> sentence;
				while((sentence = tokenizer.readSentence()) != null)
				{
					Token lastToken = sentence.get(sentence.size()-1);
					new Sentence(doc).setRange(sentence.get(0).offset, lastToken.offset + lastToken.value.length());
				}
			}
			catch(IOException ex) {
				throw new IOError(ex);
			}
		}

	}

	public static class PartOfSpeechTagger implements se.lth.cs.nlp.langforia.kernel.structure.PartOfSpeechTagger {
		private final Stagger stagger;

		@Inject
		public PartOfSpeechTagger(Stagger stagger) {
			this.stagger = stagger;
		}

		@Override
		public void apply(Document doc) {
			NodeTVar<se.lth.cs.docforia.graph.text.Token> T = se.lth.cs.docforia.graph.text.Token.var();
			NodeTVar<Sentence> S = Sentence.var();

			List<PropositionGroup> sentences =
					doc.select(S, T)
					   .where(T).coveredBy(S)
					   .stream()
					   .collect(QueryCollectors.groupBy(doc, S).orderByValue(T).collector());


			for (PropositionGroup sentence : sentences)
			{

				List<se.lth.cs.docforia.graph.text.Token> group = sentence.list(T);
				stagger.fillTaggedTokens(sentence.key().get(S), group);
			}
		}
	}

	public static class Lemmatizer implements se.lth.cs.nlp.langforia.kernel.structure.Lemmatizer {
		private final Stagger stagger;

		@Inject
		public Lemmatizer(Stagger stagger) {
			this.stagger = stagger;
		}

		@Override
		public void apply(Document doc) {
			NodeTVar<se.lth.cs.docforia.graph.text.Token> T = se.lth.cs.docforia.graph.text.Token.var();
			NodeTVar<Sentence> S = Sentence.var();

			List<PropositionGroup> sentences = doc.select(S, T)
												  .where(T).coveredBy(S)
												  .stream()
												  .collect(QueryCollectors.groupBy(doc, S).orderByValue(T).collector());

			for (PropositionGroup sentence : sentences)
			{
				List<se.lth.cs.docforia.graph.text.Token> group = sentence.list(T);
				stagger.fillTaggedTokens(sentence.key().get(S), group);
				stagger.lemma(group);
			}
		}
	}

	public static class NamedEntityRecognizer implements se.lth.cs.nlp.langforia.kernel.structure.NamedEntityRecognizer {
		private final Stagger stagger;

		@Inject
		public NamedEntityRecognizer(Stagger stagger) {
			this.stagger = stagger;
		}

		@Override
		public void apply(Document doc) {
			NodeTVar<se.lth.cs.docforia.graph.text.Token> T = se.lth.cs.docforia.graph.text.Token.var();
			NodeTVar<Sentence> S = Sentence.var();

			List<PropositionGroup> sentences = doc.select(S, T)
												  .where(T).coveredBy(S)
												  .stream()
												  .collect(QueryCollectors.groupBy(doc, S).orderByValue(T).collector());

			for (PropositionGroup sentence : sentences) {
				try {
					List<se.lth.cs.docforia.graph.text.Token> group = sentence.list(T);
					stagger.fillTaggedTokens(sentence.key().get(S), group);
					stagger.ner(doc, group);
				} catch (TagNameException e) {
					stagger.logger.error("Failed to NE tag",e);
				}
			}
		}

	}

	public static class Full implements
			se.lth.cs.nlp.langforia.kernel.structure.Lemmatizer,
			se.lth.cs.nlp.langforia.kernel.structure.NamedEntityRecognizer,
			se.lth.cs.nlp.langforia.kernel.structure.PartOfSpeechTagger
	{
		private final Stagger stagger;

		@Inject
		public Full(Stagger stagger) {
			this.stagger = stagger;
		}

		@Override
		public void apply(Document doc) {
			NodeTVar<se.lth.cs.docforia.graph.text.Token> T = se.lth.cs.docforia.graph.text.Token.var();
			NodeTVar<Sentence> S = Sentence.var();

			List<PropositionGroup> sentences = doc.select(S, T)
												  .where(T).coveredBy(S)
												  .stream()
												  .collect(QueryCollectors.groupBy(doc, S).orderByValue(T).collector());

			for (PropositionGroup sentence : sentences) {
				try {
					List<se.lth.cs.docforia.graph.text.Token> group = sentence.list(T);
					stagger.fillTaggedTokens(sentence.key(S), group);
					stagger.lemma(group);
					stagger.ner(doc, group);
				} catch (TagNameException e) {
					stagger.logger.error("Failed to NE tag",e);
				}
			}
		}
	}
}
