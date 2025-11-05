/**
 * Generates the main CI Build Job for a service.
 * This script is 'loaded' by seed.groovy and must return 'this'.
 */

// Define a function that the seed job can call.
// 'dsl' is the Job DSL factory, passed from seed.groovy.
def create(String jobName, Map config, dsl) {

    dsl.pipelineJob("MyTeam/${jobName}") {
        displayName(config.displayName)
        description(config.description)

        parameters {
            stringParam('BRANCH', config.branch, 'Git branch to build')
            choiceParam('BUILD_TYPE', ['debug', 'release'], 'Build configuration')
            booleanParam('RUN_TESTS', true, 'Execute unit tests')
            booleanParam('RUN_SONAR', false, 'Run SonarQube analysis')
            stringParam('BUILD_ARGS', '', 'Additional build arguments')
        }

        properties {
            buildDiscarder {
                strategy {
                    logRotator {
                        daysToKeepStr('30')
                        numToKeepStr('50')
                        artifactDaysToKeepStr('7')
                        artifactNumToKeepStr('10')
                    }
                }
            }
            disableConcurrentBuilds()
        }

        definition {
            cpsScm {
                scm {
                    git {
                        remote {
                            name('origin')
                            url(config.gitRepo)
                            // If you need credentials:
                            // credentials('github-credentials-id')
                        }
                        branch('${BRANCH}') // Use parameter
                        extensions {
                            cleanBeforeCheckout()
                            cloneOptions {
                                shallow(true)
                                depth(1)
                                timeout(10)
                            }
                        }
                    }
                }
                scriptPath(config.jenkinsfile)
                lightweight(true) // Faster checkout
            }
        }

        triggers {
            // Poll SCM every 15 minutes
            scm('H/15 * * * *')
            // Build periodically (nightly build)
            cron('H 2 * * *')
        }
    }
}

// Return 'this' to make the 'create' function available to the loading script
return this
