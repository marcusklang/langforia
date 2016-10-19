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
package se.lth.cs.nlp.langforia.kernel;

import com.google.inject.Guice;
import com.google.inject.Injector;
import se.lth.cs.docforia.Document;
import se.lth.cs.docforia.DocumentFactory;
import se.lth.cs.docforia.memstore.MemoryDocumentFactory;
import se.lth.cs.nlp.langforia.kernel.structure.LanguageTool;

public abstract class Language extends NlpforiaInjectorProxy {

	protected final String languageIso639code;
	protected final Injector globalInjector;
    protected final DocumentFactory factory;

    public Language(final String lang, final LanguageModule module) {
        this(MemoryDocumentFactory.getInstance(), lang, null, module);
    }

	public Language(final DocumentFactory factory, final String lang, final LanguageModule module) {
		this(factory, lang, null, module);
	}
	
	public Language(final DocumentFactory factory, final String lang, final Injector parentInjector, final LanguageModule module) {
		super();
        this.factory = factory;
		this.languageIso639code = lang;
		this.globalInjector = parentInjector;
		module.lang = this;


		if(module == null)
			throw new NullPointerException("Language " + lang + " is missing a configuration.");

		if(parentInjector == null)
            _injector = Guice.createInjector(module);
		else
            _injector = parentInjector.createChildInjector(module);
	}

	public final String getLanguageCode() {
        return languageIso639code;
    }

    public Document apply(final String text, final Class<? extends LanguageTool>...tools) {
        Document doc = getInstance(DocumentFactory.class).create("dynamic", text);
        return apply(doc, tools);
    }

	public <T extends LanguageTool> Document apply(final Document doc, final Class<? extends T>...tools) {
		if(tools.length == 0)
			throw new IllegalArgumentException("The length of tools must be larger than 0");

		for(Class<? extends T> tool : tools) {
			apply(doc, tool);
		}
		
		return doc;
	}

	public final <T extends LanguageTool> boolean isSupported(final Class<T> tool) {
        return hasBinding(tool);
	}

	public <T extends LanguageTool> Document apply(final Document doc, final Class<? extends T> tool) {
        return apply(doc, getInstance(tool));
	}
	
	public <T extends LanguageTool> Document apply(final Document doc, final T toolinstance) {
		toolinstance.apply(doc);
		return doc;
	}

	public Document newDocument(String id) {
		return factory.createFragment(id, "");
	}

	public Document newDocument(String id, String text) {
		return factory.createFragment(id, text);
	}

	/**
	 * Apply tools that are already resolved with respect to dependency
	 * @param tools
	 * @param doc
	 * @return
	 */
	public final <T extends LanguageTool> Document apply(Document doc, Iterable<T> tools) {
		for(T toolinstance : tools)  {
			apply(doc, toolinstance);
		}
		return doc;
	}
}
