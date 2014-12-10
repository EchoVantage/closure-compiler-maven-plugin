package org.echovantage.closure;

import java.io.File;

public class SourceDir {
	private File sourceDir;
	private String outputPrefix;

	public File sourceDir() {
		return sourceDir;
	}

	public String outputPrefix() {
		return outputPrefix == null ? "" : outputPrefix;
	}

	@Override
	public String toString() {
		return String.format("source: %s, prefix: %s", sourceDir, outputPrefix());
	}
}
