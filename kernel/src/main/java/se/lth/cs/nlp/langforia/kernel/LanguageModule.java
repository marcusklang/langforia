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

import com.google.inject.Singleton;
import se.lth.cs.docforia.DocumentFactory;
import se.lth.cs.nlp.langforia.kernel.structure.*;

public abstract class LanguageModule extends AbstractLanguageModule {

    protected Language lang;

    public LanguageModule() {
    }

    @Override
    public String getLang() {
        return lang.getLanguageCode();
    }

    @Override
    protected void configure() {
        bind(Language.class).toInstance(lang);
        bind(String.class).annotatedWith(LanguageCode.class).toInstance(lang.getLanguageCode());
        bind(DocumentFactory.class).toInstance(lang.factory);
        bind(FullPipeline.class).to(FullPipelineImpl.class).in(Singleton.class);
        bind(Pipelines.class).in(Singleton.class);
    }

    protected void bindPipeline(PipelinesConfiguration pipelineconfig) {
        bind(PipelinesConfiguration.class).toInstance(pipelineconfig);
    }
}
