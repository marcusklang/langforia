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

public class FullPipelineImpl implements FullPipeline {

	private final Pipelines pipeline;
	private final Language lang;
	
	@Inject
	public FullPipelineImpl(Language lang, Pipelines pipeline) {
		this.pipeline = pipeline;
		this.lang = lang;
	}
	
	@Override
	public void apply(Document doc) {
		pipeline.apply(doc);
	}

}
