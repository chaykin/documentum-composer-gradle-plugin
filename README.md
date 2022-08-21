# Documentum Composer Gradle plugin

Gradle plugin for build Dar files using Documentum Composer.

Plugin requires Documentum Composer headless distributive uploaded in Your enterprise repository as zip-artifact.

## Using

Add plugin to Your `build.gradle` file:

```
plugins {
    ...
    id 'ru.opentext.gradle.documentum-composer'
}
```

Then, add special dependency:

```
dependencies {
    ...
    composer_distr_linux '<COMPOSER DISTRIBUTIVE ARTIFACT IN YOUR ENTERPRISE REPOSITORY>'
    composer_distr_windows '<COMPOSER DISTRIBUTIVE ARTIFACT IN YOUR ENTERPRISE REPOSITORY>'
}
``` 

And, finally, add task for build Dar's:

```
task buildDars(type: ru.opentext.gradle.BuildDarTask) {
    docappsBuildDir = file("${project.projectDir}/docapps")
    outputDir = file("${buildDir}/dars")
}
```

**Note**: plugin expects all Your docapp-projects in one directory (`docappsBuildDir` option). So, collect all needed
docapp-projects in it before start task.
