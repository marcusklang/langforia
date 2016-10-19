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

public class StringResource extends Resource {
    private String data;

    public StringResource(String name, String data) {
        super(name);
        this.data = data;
    }

    @Override
    public boolean isReadable() {
        return true;
    }

    @Override
    public boolean isWriteable() {
        return false;
    }

    @Override
    public boolean supportsFile() {
        return true;
    }

    @Override
    public File file() {
        try {
            File temp = File.createTempFile("vfs-temp", ".txt");
            temp.deleteOnExit();
            Writer writer = new FileWriter(temp);
            writer.write(data);
            writer.flush();
            writer.close();
            return temp;
        } catch (IOException e) {
            throw new IOError(e);
        }
    }

    @Override
    public InputStream binaryRead() {
        try {
            return new ByteArrayInputStream(data.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new IOError(e);
        }
    }

    @Override
    public Reader textRead() {
        return new StringReader(data);
    }

}
