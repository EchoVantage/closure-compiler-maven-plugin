package org.echovantage.closure;

import static java.lang.String.format;

import org.apache.maven.plugin.logging.Log;

import com.google.javascript.jscomp.BasicErrorManager;
import com.google.javascript.jscomp.CheckLevel;
import com.google.javascript.jscomp.JSError;

public class MojoErrorManager extends BasicErrorManager {
	private final Log log;

	public MojoErrorManager(final Log log) {
		this.log = log;
	}

	@Override
	public void println(final CheckLevel level, final JSError error) {
		String errorString = format("JS %s in %s [%d]: %s", level, error.description, error.sourceName, error.lineNumber);
		switch(level) {
			case ERROR:
				log.error(errorString);
				break;
			case WARNING:
				log.warn(errorString);
				break;
			default:
				log.info(errorString);
				break;
		}
	}

	@Override
	protected void printSummary() {
		log.info(String.format("JS compilation completed with %d errors, %d warnings", getErrorCount(), getWarningCount()));
	}
}