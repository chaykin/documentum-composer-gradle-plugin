package ru.opentext.gradle;

import java.io.File;
import java.util.Set;

import org.gradle.api.DefaultTask;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ResolvedArtifact;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.gradle.internal.os.OperatingSystem;

import static ru.opentext.gradle.DocumentumComposerPlugin.CONFIG_NAME_LINUX;
import static ru.opentext.gradle.DocumentumComposerPlugin.CONFIG_NAME_WINDOWS;

public abstract class BuildDarTask extends DefaultTask {
    private static final String COMPOSER_DIR = "composer";

    private static final String COMPOSER_LAUNCHER = "org.eclipse.core.launcher.Main";

    @TaskAction
    public void run() {
	initComposerCache();
	File docappsBuildDir = getDocappsBuildDir().getAsFile().get();
	File outputDir = getOutputDir().getAsFile().get();
	File buildFile = new ComposerBuildFileFactory(getProject()).generate(docappsBuildDir, outputDir);

	getProject().javaexec(execSpec -> {
	    execSpec.classpath(new File(getComposerDir(), "startup.jar"));
	    execSpec.getMainClass().set(COMPOSER_LAUNCHER);
	    execSpec.args("-data", docappsBuildDir.getAbsolutePath(),
			    "-application", "org.eclipse.ant.core.antRunner",
			    "-buildfile", buildFile.getAbsolutePath());
	});
    }

    @InputDirectory
    public abstract RegularFileProperty getDocappsBuildDir();

    @OutputDirectory
    public abstract RegularFileProperty getOutputDir();

    private void initComposerCache() {
	if (!getComposerDir().exists()) {
	    String configName = getConfigName();

	    Configuration conf = getProject().getConfigurations().getAt(configName);
	    Set<ResolvedArtifact> artifacts = conf.getResolvedConfiguration().getResolvedArtifacts();

	    boolean unpacked = false;
	    for (ResolvedArtifact artifact : artifacts) {
		File composerZipFile = artifact.getFile();
		unpackComposer(composerZipFile);
		unpacked = true;
		break;
	    }

	    if (!unpacked) {
		throw new RuntimeException("No composer dependency found");
	    }
	}
    }

    private String getConfigName() {
	OperatingSystem os = OperatingSystem.current();
	if (os.isWindows()) {
	    return CONFIG_NAME_WINDOWS;
	} else if (os.isLinux()) {
	    return CONFIG_NAME_LINUX;
	} else {
	    throw new RuntimeException("Unsupported OS: " + os);
	}
    }

    private void unpackComposer(File zipFile) {
	File composerOutput = getComposerDir();
	getLogger().lifecycle("Unpacking composer to {}", composerOutput.toString());
	getProject().copy(copySpec -> {
	    copySpec.from(getProject().zipTree(zipFile));
	    copySpec.into(composerOutput);
	});
    }

    private File getComposerDir() {
	return new File(getProject().getBuildDir(), COMPOSER_DIR);
    }
}
