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

import java.lang.annotation.Annotation;

public class Properties {
	public static Property named(final String name) {
		return new Property() {

			public Class<? extends Annotation> annotationType() {
				return Property.class;
			}

			public String value() {
				return name;
			}

			@Override
			public int hashCode() {
				// This is specified in java.lang.Annotation.
				return (127 * "value".hashCode()) ^ name.hashCode();
			}

			public boolean equals(Object o) {
				if (!(o instanceof Property)) {
					return false;
				}

				Property other = (Property) o;
				return name.equals(other.value());
			}

			public String toString() {
				return "@" + Property.class.getName() + "(value=" + name + ")";
			}

		};
	}
}
