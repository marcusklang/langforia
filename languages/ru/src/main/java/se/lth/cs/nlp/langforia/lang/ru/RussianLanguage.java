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
package se.lth.cs.nlp.langforia.lang.ru;

import com.google.inject.Injector;
import se.lth.cs.docforia.DocumentFactory;
import se.lth.cs.nlp.langforia.kernel.Language;
import se.lth.cs.nlp.langforia.kernel.LanguageCodes;

public class RussianLanguage extends Language {
    public RussianLanguage() {
        super(LanguageCodes.RUSSIAN, new RussianLanguageModule());
    }

    public RussianLanguage(RussianLanguageModule module) {
        super(LanguageCodes.RUSSIAN, module);
    }

    public RussianLanguage(DocumentFactory factory) {
        super(factory, LanguageCodes.RUSSIAN, new RussianLanguageModule());
    }

    public RussianLanguage(DocumentFactory factory, RussianLanguageModule module) {
        super(factory, LanguageCodes.RUSSIAN, module);
    }

    public RussianLanguage(DocumentFactory factory, Injector parentInjector) {
        super(factory, LanguageCodes.RUSSIAN, parentInjector, new RussianLanguageModule());
    }

    public RussianLanguage(DocumentFactory factory, Injector parentInjector, RussianLanguageModule module) {
        super(factory, LanguageCodes.RUSSIAN, parentInjector, module);
    }
}
