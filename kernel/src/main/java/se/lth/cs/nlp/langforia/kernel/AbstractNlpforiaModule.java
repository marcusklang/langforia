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

import com.google.inject.AbstractModule;
import se.lth.cs.nlp.langforia.kernel.resources.Resource;

public abstract class AbstractNlpforiaModule extends AbstractModule {

    protected final <T> void bindProperty(String property, Class<T> clazz, T instance) {
        bind(clazz).annotatedWith(Properties.named(property)).toInstance(instance);
    }

    protected final void bindProperty(String property, double value) {
        bindProperty(property, Double.class, value);
        bindProperty(property, double.class, value);
    }

    protected final void bindProperty(String property, float value) {
        bindProperty(property, Float.class, value);
        bindProperty(property, float.class, value);
    }

    protected final void bindProperty(String property, boolean value) {
        bindProperty(property, Boolean.class, value);
        bindProperty(property, boolean.class, value);
    }

    protected final void bindProperty(String property, int value) {
        bindProperty(property, Integer.class, value);
        bindProperty(property, int.class, value);
    }

    protected final void bindProperty(String property, long value) {
        bindProperty(property, Long.class, value);
        bindProperty(property, long.class, value);
    }

    protected final void bindProperty(String property, String value) {
        bindProperty(property, String.class, value);
    }

    protected void bindModel(String modelId, Resource res) {
        bind(Resource.class).annotatedWith(Models.named(modelId)).toInstance(res);
    }

    protected final void bindProperty(String property, Long value) {
        bindProperty(property, Long.class, value);
        bindProperty(property, long.class, value);
    }
}
