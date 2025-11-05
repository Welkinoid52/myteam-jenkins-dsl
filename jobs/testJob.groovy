/**
 * Generates the Test Job for a service.
 * This script is 'loaded' by seed.groovy and must return 'this'.
 */

def create(String jobName, Map config, dsl) {

    dsl.pipelineJob("MyTeam/${jobName}-test") {
        displayName("${config.displayName} - Tests")
        description("Run integration and E2E tests for ${config.displayName}")

        parameters {
            choiceParam('TEST_SUITE', ['all', 'unit', 'integration', 'e2e'], 'Test suite to run')
            stringParam('TEST_TAGS', '', 'Test tags to include (comma-separated)')
            booleanParam('GENERATE_REPORT', true, 'Generate HTML test report')
        }

        definition {
            cps {
                // This inline script is now neatly contained within its template
                script("""
                    pipeline {
                        agent any

                        options {
                            timeout(time: 30, unit: 'MINUTES')
                            timestamps()
                        }

                        environment {
                            SERVICE_NAME = '${jobName}'
                            BUILD_TOOL = '${config.buildTools}'
                        }

                        stages {
                            stage('Checkout') {
                                steps {
                                    git branch: '${config.branch}',
                                        url: '${config.gitRepo}'
                                    echo "Checked out branch: ${config.branch}"
                                }
                            }

                            stage('Setup') {
                                steps {
                                    echo "Setting up test environment for \${SERVICE_NAME}"
                                    script {
                                        if (env.BUILD_TOOL == 'npm') {
                                            sh 'echo "npm install (simulated)"'
                                        } else if (env.BUILD_TOOL == 'gradle') {
                                            sh 'echo "./gradlew clean (simulated)"'
                                        }
                                    }
                                }
                            }

                            stage('Run Tests') {
                                steps {
                                    echo "Running \${params.TEST_SUITE} tests"
                                    script {
                                        switch(params.TEST_SUITE) {
                                            case 'unit':
                                                sh 'echo "Running unit tests..." && sleep 2'
                                                break
                                            case 'integration':
                                                sh 'echo "Running integration tests..." && sleep 3'
                                                break
                                            case 'e2e':
                                                sh 'echo "Running E2E tests..." && sleep 5'
                                                break
                                            default:
                                                sh 'echo "Running all tests..." && sleep 7'
                                        }
                                    }

                                    // Simulate test results
                                    sh '''
                                        mkdir -p test-results
                                        echo "Test Suite: \${TEST_SUITE}" > test-results/summary.txt
                                        echo "Tests Run: 42" >> test-results/summary.txt
                                        echo "Passed: 40" >> test-results/summary.txt
                                        echo "Failed: 2" >> test-results/summary.txt
                                        echo "Skipped: 0" >> test-results/summary.txt
                                    '''
                                }
                            }

                            stage('Generate Report') {
                                when {
                                    expression { params.GENERATE_REPORT == true }
                                }
                                steps {
                                    echo "Generating test report..."
                                    sh 'cat test-results/summary.txt'
                                }
                            }
                        }

                        post {
                            always {
                                echo "Test execution completed"
                                archiveArtifacts artifacts: 'test-results/*.txt',
                                                 allowEmptyArchive: true
                            }
                            success {
                                echo "✅ Tests passed for \${SERVICE_NAME}"
                            }
                            failure {
                                echo "❌ Tests failed for \${SERVICE_NAME}"
                            }
                        }
                    }
                """.stripIndent())
                sandbox()
            }
        }

        triggers {
            upstream(
                upstreamProjects: "MyTeam/${jobName}",
                threshold: hudson.model.Result.SUCCESS
            )
        }
    }
}

// Return 'this' to make the 'create' function available to the loading script
return this
