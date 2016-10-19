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

import com.google.inject.Binding;
import com.google.inject.ConfigurationException;
import com.google.inject.Key;
import se.lth.cs.nlp.langforia.kernel.resources.Resource;

public abstract class NlpforiaInjectorProxy extends InjectorProxy {
	public <T> T getProperty(Class<T> type, String property) {
		return (T)getInstance(Key.get(type, Properties.named(property)));
	}

    public Resource getModel(String modelId) {
        return getInstance(Key.get(Resource.class, Models.named(modelId)));
    }

    public final <T> boolean hasBinding(Class<T> clazz) {
        try {
            getBinding(Key.get(clazz));
            return true;
        } catch(ConfigurationException ex) {
            return false;
        }
    }

	public final <T> Binding<T> getBindingOrNull(Class<T> clazz) {
		try {
			return getBinding(Key.get(clazz));
		} catch(ConfigurationException ex) {
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	public final <T> Binding<T> getBindingOrNull(Key<?> key) {
		try {
			return (Binding<T>)getBinding(key);
		} catch(ConfigurationException ex) {
			return null;
		}
	}
}
