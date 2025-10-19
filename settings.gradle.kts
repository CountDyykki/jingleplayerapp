pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }

    }
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://maven.pkg.github.com/vituary-solutions/icalendar-kotlin")
            credentials {
                        username = "CountDyykki"
                        password = "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"
                }
            }
    }
}

rootProject.name = "My Application2"
include(":app")

