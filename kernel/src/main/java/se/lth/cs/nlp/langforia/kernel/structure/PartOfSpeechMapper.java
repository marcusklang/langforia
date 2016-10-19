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

public interface PartOfSpeechMapper {
	String[] toLanguage(String tag);
	String fromLanguage(String tag);
	
	public enum Tags {
		VERB("VERB"),
		NOUN("NOUN"),
        PROPN("PROPN"),
		PRON("PRON"),
		ADJECTIVE("ADJ"),
		ADVERBS("ADV"),
		ADPOSITIONS("ADP"),
		CONJUCTION("CONJ"),
		DETERMINER("DET"),
		NUMBER("NUM"),
		PARTICLE("PRT"),
		OTHER("X"),
		PUNCTUATION(".");
		
		private final String value;
		
		private Tags(String value) {
			this.value = value;
		}
		
		@Override
		public String toString() {
			return value;
		}
	}	
}
