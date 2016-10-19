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

import com.google.inject.*;
import com.google.inject.spi.TypeConverterBinding;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A proxy class that will make another class 
 * act as an injector.
 * @author Marcus
 *
 */
public abstract class InjectorProxy implements Injector {
	protected Injector _injector;

	public InjectorProxy() {
		this._injector = null;
	}
	
	public InjectorProxy(Injector injector) {
		this._injector = injector;
	}

	public final void injectMembers(final Object instance) {
		_injector.injectMembers(instance);
	}

	public final <T> MembersInjector<T> getMembersInjector(final TypeLiteral<T> typeLiteral) {
		return _injector.getMembersInjector(typeLiteral);
	}

	public final <T> MembersInjector<T> getMembersInjector(final Class<T> type) {
		return _injector.getMembersInjector(type);
	}

	public final Map<Key<?>, Binding<?>> getBindings() {
		return _injector.getBindings();
	}

	public final Map<Key<?>, Binding<?>> getAllBindings() {
		return _injector.getAllBindings();
	}

	public final <T> Binding<T> getBinding(final Key<T> key) {
		return _injector.getBinding(key);
	}

	public final <T> Binding<T> getBinding(final Class<T> type) {
		return _injector.getBinding(type);
	}

	public final <T> Binding<T> getExistingBinding(final Key<T> key) {
		return _injector.getExistingBinding(key);
	}

	public final <T> List<Binding<T>> findBindingsByType(final TypeLiteral<T> type) {
		return _injector.findBindingsByType(type);
	}

	public final <T> Provider<T> getProvider(final Key<T> key) {
		return _injector.getProvider(key);
	}

	public final <T> Provider<T> getProvider(final Class<T> type) {
		return _injector.getProvider(type);
	}

    public final <T,A extends Annotation> T getInstance(final Class<T> type, final Class<A> annotation) {
        return getInstance(Key.get(type, annotation));
    }

    public final <T,A extends Annotation> T getInstance(final Class<T> type, final A annotation) {
        return getInstance(Key.get(type, annotation));
    }

	public final <T> T getInstance(final Key<T> key) {
		return _injector.getInstance(key);
	}

	public final <T> T getInstance(final Class<T> type) {
		return _injector.getInstance(type);
	}

	public final Injector getParent() {
		return _injector.getParent();
	}

	public final Injector createChildInjector(final Iterable<? extends Module> modules) {
		return _injector.createChildInjector(modules);
	}

	public final Injector createChildInjector(final Module... modules) {
		return _injector.createChildInjector(modules);
	}

	public final Map<Class<? extends Annotation>, Scope> getScopeBindings() {
		return _injector.getScopeBindings();
	}

	public final Set<TypeConverterBinding> getTypeConverterBindings() {
		return _injector.getTypeConverterBindings();
	}
}
