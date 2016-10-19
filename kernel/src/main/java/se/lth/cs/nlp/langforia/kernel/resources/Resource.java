package se.lth.cs.nlp.langforia.kernel.resources;
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

import java.io.*;

public abstract class Resource {
    protected final String name;

    public Resource(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }

    public File file() {
        throw new UnsupportedOperationException();
    }

    public boolean isReadable() {
        return false;
    }

    public boolean isWriteable() {
        return false;
    }

    public boolean supportsFile() {
        return false;
    }

    public boolean isValid() {
        return false;
    }

    public OutputStream binaryWrite() {
        throw new UnsupportedOperationException();
    }

    public InputStream binaryRead() {
        throw new UnsupportedOperationException();
    }

    public Reader textRead() {
        throw new UnsupportedOperationException();
    }

    public Writer textWrite() {
        throw new UnsupportedOperationException();
    }

    public boolean isVirtual() {
        return true;
    }
}
