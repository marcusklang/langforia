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
package se.lth.cs.nlp.langforia.lang.en;

import com.google.inject.Injector;
import se.lth.cs.docforia.DocumentFactory;
import se.lth.cs.nlp.langforia.kernel.Language;
import se.lth.cs.nlp.langforia.kernel.LanguageCodes;

/**
 * Created by marcus on 2015-02-16.
 */
public class EnglishLanguage extends Language {
    public EnglishLanguage() {
        super(LanguageCodes.ENGLISH, new EnglishLanguageModule());
    }

    public EnglishLanguage(EnglishLanguageModule module) {
        super(LanguageCodes.ENGLISH, module);
    }

    public EnglishLanguage(DocumentFactory factory) {
        super(factory, LanguageCodes.ENGLISH, new EnglishLanguageModule());
    }

    public EnglishLanguage(DocumentFactory factory, EnglishLanguageModule module) {
        super(factory, LanguageCodes.ENGLISH, module);
    }

    public EnglishLanguage(DocumentFactory factory, Injector parentInjector) {
        super(factory, LanguageCodes.ENGLISH, parentInjector, new EnglishLanguageModule());
    }

    public EnglishLanguage(DocumentFactory factory, Injector parentInjector, EnglishLanguageModule module) {
        super(factory, LanguageCodes.ENGLISH, parentInjector, module);
    }
}
