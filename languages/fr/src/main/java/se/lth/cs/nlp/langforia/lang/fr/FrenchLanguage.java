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
package se.lth.cs.nlp.langforia.lang.fr;

import com.google.inject.Injector;
import se.lth.cs.docforia.DocumentFactory;
import se.lth.cs.nlp.langforia.kernel.Language;
import se.lth.cs.nlp.langforia.kernel.LanguageCodes;

public class FrenchLanguage extends Language {

    public FrenchLanguage() {
        super(LanguageCodes.FRENCH, new FrenchLanguageModule());
    }

    public FrenchLanguage(FrenchLanguageModule module) {
        super(LanguageCodes.FRENCH, module);
    }

    public FrenchLanguage(DocumentFactory factory) {
        super(factory, LanguageCodes.FRENCH, new FrenchLanguageModule());
    }

    public FrenchLanguage(DocumentFactory factory, FrenchLanguageModule module) {
        super(factory, LanguageCodes.FRENCH, module);
    }

    public FrenchLanguage(DocumentFactory factory, Injector parentInjector) {
        super(factory, LanguageCodes.FRENCH, parentInjector, new FrenchLanguageModule());
    }

    public FrenchLanguage(DocumentFactory factory, Injector parentInjector, FrenchLanguageModule module) {
        super(factory, LanguageCodes.FRENCH, parentInjector, module);
    }
}
