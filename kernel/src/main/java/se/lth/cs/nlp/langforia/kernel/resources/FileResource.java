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

public class FileResource extends Resource {

    protected File _fspath;

    public FileResource(File path) {
        super(path.getName());
        this._fspath = path;
    }

    @Override
    public boolean isReadable() {
        return _fspath.canRead();
    }

    @Override
    public boolean isWriteable() {
        if(!_fspath.exists()) {
            return true;
        }

        return _fspath.canWrite();
    }

    @Override
    public OutputStream binaryWrite() {
        if(!isWriteable())
            throw new IOError(new IOException("This file resource cannot be written to. isWritable() == false."));

        try {
            if(!_fspath.getParentFile().exists())
            {
                if(!_fspath.getParentFile().mkdirs()) {
                    throw new IOError(new IOException("Failed to create the directories needed to write the file."
                            + " Attempted to mkdirs " + _fspath.getParentFile().getAbsolutePath()));
                }
            }

            return new FileOutputStream(_fspath);
        } catch (FileNotFoundException e) {
            throw new IOError(e);
        }
    }

    @Override
    public InputStream binaryRead() {
        if(!isReadable())
            throw new IOError(new IOException("Cannot read this file: " + _fspath.getAbsolutePath()));

        try {
            return new FileInputStream(_fspath);
        } catch (FileNotFoundException e) {
            throw new IOError(e);
        }
    }

    @Override
    public Reader textRead() {
        try {
            return new InputStreamReader(binaryRead(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IOError(e);
        }
    }

    @Override
    public Writer textWrite() {
        try {
            return new OutputStreamWriter(binaryWrite(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IOError(e);
        }
    }

    @Override
    public boolean isValid() {
        return _fspath.exists();
    }

    @Override
    public File file() {
        return _fspath;
    }
}
