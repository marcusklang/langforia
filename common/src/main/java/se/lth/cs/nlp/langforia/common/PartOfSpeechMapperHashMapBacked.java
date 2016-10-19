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

import se.lth.cs.nlp.langforia.kernel.structure.PartOfSpeechMapper;

import java.util.HashMap;

public class PartOfSpeechMapperHashMapBacked implements PartOfSpeechMapper {

	protected final HashMap<String,String[]> toLanguageMap = new HashMap<String, String[]>();
	protected final HashMap<String,String> fromLanguageMap = new HashMap<String, String>();
	private static final String[] emptyArray = new String[0];
	
	@Override
	public String[] toLanguage(String tag) {
		String[] result = toLanguageMap.get(tag);
		if(result == null)
			return emptyArray;
		else
			return result;
	}
	
	@Override
	public String fromLanguage(String tag) {
		return fromLanguageMap.get(tag);
	}

	public void putFromLanguage(String from, String to) {
		fromLanguageMap.put(from, to);
	}
	
	public void putToLanguage(String to, String[] from) {
		toLanguageMap.put(to, from);
	}

}
