package com.google.javascript.jscomp;

public class EmbbededCommandLineRunner extends CommandLineRunner {
	public EmbbededCommandLineRunner(final String[] args) {
		super(args);
	}

	public int runEmbeded() {
		int result = 0;
		int runs = 1;
		try {
			for(int i = 0; i < runs && result == 0; i++) {
				result = doRun();
			}
		} catch(AbstractCommandLineRunner.FlagUsageException e) {
			System.err.println(e.getMessage());
			result = -1;
		} catch(Throwable t) {
			t.printStackTrace();
			result = -2;
		}
		return result;
	}
}
