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
package se.lth.cs.nlp.langforia.lang.de;

import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.de.GermanAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;
import se.lth.cs.docforia.DocumentFactory;
import se.lth.cs.nlp.langforia.common.*;
import se.lth.cs.nlp.langforia.kernel.Language;
import se.lth.cs.nlp.langforia.kernel.LanguageCodes;
import se.lth.cs.nlp.langforia.kernel.LanguageModule;
import se.lth.cs.nlp.langforia.kernel.Models;
import se.lth.cs.nlp.langforia.kernel.resources.JarResource;
import se.lth.cs.nlp.langforia.kernel.structure.*;

/**
 * Created by marcusk on 2016-06-08.
 */
public class GermanLanguage extends Language {
    public GermanLanguage() {
        super(LanguageCodes.GERMAN, new GermanLanguageModule());
    }

    public GermanLanguage(GermanLanguageModule module) {
        super(LanguageCodes.GERMAN, module);
    }

    public GermanLanguage(DocumentFactory factory) {
        super(factory, LanguageCodes.GERMAN, new GermanLanguageModule());
    }

    public GermanLanguage(DocumentFactory factory, GermanLanguageModule module) {
        super(factory, LanguageCodes.GERMAN, module);
    }

    public GermanLanguage(DocumentFactory factory, Injector parentInjector) {
        super(factory, LanguageCodes.GERMAN, parentInjector, new GermanLanguageModule());
    }

    public GermanLanguage(DocumentFactory factory, Injector parentInjector, GermanLanguageModule module) {
        super(factory, LanguageCodes.GERMAN, parentInjector, module);
    }
}