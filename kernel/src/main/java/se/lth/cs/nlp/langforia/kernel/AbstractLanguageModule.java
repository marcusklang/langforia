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

import se.lth.cs.nlp.langforia.kernel.exceptions.LangforiaRuntimeException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public abstract class AbstractLanguageModule extends AbstractNlpforiaModule implements Cloneable {

	public abstract String getLang();
	
	protected <T extends AbstractLanguageModule> void install(Class<T> clazz) {
		try {
			Constructor<T> constructor = clazz.getConstructor(String.class);
			install(constructor.newInstance(getLang()));
		} catch (NoSuchMethodException |
				SecurityException |
				IllegalAccessException |
				InstantiationException |
				IllegalArgumentException |
				InvocationTargetException e) {
			throw new LangforiaRuntimeException(e);
		}
	}
}
