package com.github.davidmoten.logmetrics;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import com.google.common.base.Throwables;

public final class Util {

    public static FileReader createReader(File file, long position) {
	try {
	    FileReader reader = new FileReader(file);
	    reader.skip(position);
	    return reader;
	} catch (IOException e) {
	    throw Throwables.propagate(e);
	}
    }

}
