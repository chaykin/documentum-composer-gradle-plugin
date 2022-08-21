package ru.opentext.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class DocumentumComposerPlugin implements Plugin<Project> {
    static final String CONFIG_NAME_LINUX = "composer_distr_linux";
    static final String CONFIG_NAME_WINDOWS = "composer_distr_windows";

    @Override
    public void apply(Project project) {
	project.getConfigurations().create(CONFIG_NAME_LINUX);
	project.getConfigurations().create(CONFIG_NAME_WINDOWS);

	project.getTasks().create("buildDar", BuildDarTask.class);
    }
}
