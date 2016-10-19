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
import java.nio.file.Files;

public class JarResource extends Resource {
    private String resource;

    public JarResource(String name, String resource) {
        super(name);
        this.resource = resource;
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
            InputStream stream = getClass().getClassLoader().getResourceAsStream(resource);
            if(stream == null)
                throw new IOError(new IOException("Resource not found in jar: " + resource));

            File temp = Files.createTempDirectory("temp-res").toFile();
            temp.deleteOnExit();

            File tempfile = new File(temp, name);
            FileOutputStream output = new FileOutputStream(tempfile);

            byte[] buffer = new byte[256*1024];
            int read = 0;
            while(read != -1) {
                read = stream.read(buffer);
                if(read > 0)
                    output.write(buffer, 0, read);
            }

            output.flush();
            output.close();
            stream.close();

            return tempfile;
        } catch (IOException e) {
            throw new IOError(e);
        }
    }

    @Override
    public InputStream binaryRead() {
        InputStream is = getClass().getClassLoader().getResourceAsStream(resource);
        if(is == null)
            throw new IOError(new IOException("Resource not found in jar: " + resource));
        return is;
    }

    @Override
    public Reader textRead() {
        try {
            return new InputStreamReader(binaryRead(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IOError(e);
        }
    }
}
