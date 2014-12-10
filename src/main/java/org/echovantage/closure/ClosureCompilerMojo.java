package org.echovantage.closure;

import static java.nio.file.Files.copy;
import static java.nio.file.Files.createDirectories;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.google.javascript.jscomp.EmbbededCommandLineRunner;

@Mojo(name = "compile")
public class ClosureCompilerMojo extends AbstractMojo {
	/**
	 * A List of source source directories that will be passed to the Closure
	 * compiler via the --js option. Each entry in the sources list will generate
	 * a separate --js option.
	 */
	@Parameter(required = true)
	private List<SourceDir> sourceDirs;
	/**
	 * Path of the combined output file where the result of the compilation will
	 * be saved. Relative to docroot.
	 */
	@Parameter(required = true)
	private String outputFile;
	/**
	 * Location to output generated source map file relative to docroot. If not
	 * specified no source map will be created.
	 */
	@Parameter
	private String sourceMapOutput;
	/**
	 * Root of all outputs
	 */
	@Parameter(defaultValue = "${project.build.directory}/docroot")
	private File docroot;
	/**
	 * Location to copy raw sources to after compilation. This value is relative
	 * to the docroot parameter.
	 */
	@Parameter
	private String rawDestination;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		getLog().debug("Starting compilation");
		compileJs();
		getLog().debug("Compilation complete");
		try {
			getLog().debug("Copying sources");
			copyRawSources();
		} catch(IOException e) {
			throw new MojoExecutionException("Error copying raw source files", e);
		}
	}

	private void copyRawSources() throws IOException {
		if(rawDestination == null) {
			return;
		}
		Path destRoot = docroot.toPath().resolve(rawDestination);
		getLog().debug("Dest root: " + destRoot);
		for(SourceDir source : sourceDirs) {
			getLog().debug("Copying source dir: " + source);
			Path destination = destRoot.resolve(source.outputPrefix());
			Files.createDirectories(destination);
			getLog().debug("destination: " + destination);
			Files.list(source.sourceDir().toPath()).forEach((sourceFile) -> {
				getLog().debug("Copying source file: " + sourceFile);
				Path destFile = destination.resolve(sourceFile.getFileName());
				getLog().debug("Dest file: " + destFile);
				try {
					copy(sourceFile, destFile, REPLACE_EXISTING);
				} catch(Exception e) {
					throw new RuntimeException(e);
				}
			});
		}
	}

	private void compileJs() throws MojoExecutionException {
		ArrayList<String> params = new ArrayList<>();
		for(SourceDir source : sourceDirs) {
			params.add("--js='" + new File(source.sourceDir(), "**.js").getAbsolutePath() + "'");
			if(sourceMapOutput != null) {
				params.add("--source_map_location_mapping");
				params.add(source.sourceDir().getAbsolutePath() + "|/" + new File(rawDestination, source.outputPrefix()));
			}
		}
		params.add("--js_output_file");
		params.add(new File(docroot, outputFile).getAbsolutePath().toString());
		if(sourceMapOutput != null) {
			Path sourceMap = docroot.toPath().resolve(sourceMapOutput);
			try {
				createDirectories(sourceMap.getParent());
			} catch(IOException e) {
				throw new MojoExecutionException("Error creating source map output directory", e);
			}
			params.add("--create_source_map");
			params.add(sourceMap.toString());
			params.add("--output_wrapper");
			params.add("%output%\n//# sourceMappingURL=/" + sourceMapOutput);
		}
		String[] paramsArray = params.toArray(new String[params.size()]);
		getLog().debug("Closure compiler parameters: " + Arrays.toString(paramsArray));
		EmbbededCommandLineRunner runner = new EmbbededCommandLineRunner(paramsArray);
		int status = runner.runEmbeded();
		if(status != 0) {
			throw new MojoExecutionException("Closure compiler exited with non-zero status: " + status);
		}
	}
}
