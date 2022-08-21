package ru.opentext.gradle;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;
import org.gradle.api.Project;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Generate Ant build script for Composer
 */
public class ComposerBuildFileFactory {
    private static final String DAR_OUTPUT_DIR_PLACEHOLDER = "#DAR.OUTPUT.DIR#";
    private static final String DOCAPP_NAME_PLACEHOLDER = "#NAME#";
    private static final Pattern DOCAPP_LIST_PATTERN = Pattern.compile("#DARS-START#(.*)?#DARS-END#");

    private static final Collection<String> EXCLUDE_DIRS = new HashSet<>(
		    Arrays.asList(".metadata", "DocumentumCoreProject", "TCMReferenceProject"));

    private final Project project;

    public ComposerBuildFileFactory(Project project) {
	this.project = project;
    }

    public File generate(File docappsBuildDir, File outputDir) {
	try {
	    String template = loadTemplate();
	    template = template.replace(DAR_OUTPUT_DIR_PLACEHOLDER, outputDir.getAbsolutePath());

	    Matcher matcher = DOCAPP_LIST_PATTERN.matcher(template);
	    StringBuffer content = new StringBuffer();

	    while (matcher.find()) {
		String docappTemplate = matcher.group(1);
		String docappContent = createDocappsList(docappsBuildDir, docappTemplate);
		matcher.appendReplacement(content, Matcher.quoteReplacement(docappContent));
	    }

	    matcher.appendTail(content);

	    File generatedFile = getComposerBuildFile();
	    try (OutputStream out = Files.newOutputStream(generatedFile.toPath())) {
		IOUtils.write(content, out, UTF_8);
	    }

	    return generatedFile;
	} catch (IOException e) {
	    throw new RuntimeException(e);
	}
    }

    private String createDocappsList(File docappsBuildDir, String template) {
	File[] docapps = getDocappsDirs(docappsBuildDir);
	return Stream.of(docapps).map(f -> createDarItem(f, template)).collect(Collectors.joining("\r\n"));
    }

    private File[] getDocappsDirs(File docappsBuildDir) {
	return docappsBuildDir.listFiles(f -> f.isDirectory() && !EXCLUDE_DIRS.contains(f.getName()));
    }

    private String createDarItem(File file, String template) {
	return template.replaceAll(DOCAPP_NAME_PLACEHOLDER, file.getName());
    }

    private File getComposerBuildFile() {
	return new File(project.getBuildDir(), "build.dar.xml");
    }

    private String loadTemplate() throws IOException {
	try (InputStream templateStream = getClass().getResourceAsStream("build-template.xml")) {
	    if (templateStream == null) {
		throw new IOException("Build template not found");
	    }
	    return IOUtils.toString(templateStream, UTF_8);
	}
    }
}
