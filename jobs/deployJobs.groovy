/**
 * Generates all Deployment Jobs for a service (one per environment).
 * This script is 'loaded' by seed.groovy and must return 'this'.
 */

def create(String jobName, Map config, dsl) {

    // This file handles looping through the environments
    config.environments.each { env ->

        dsl.pipelineJob("MyTeam/${jobName}-deploy-${env}") {
            displayName("${config.displayName} - Deploy to ${env.toUpperCase()}")
            description("Deploy ${config.displayName} to ${env} environment")

            parameters {
                stringParam('VERSION', 'latest', 'Version/tag to deploy')
                stringParam('REPLICAS', env == 'prod' ? '3' : '1', 'Number of instances')
                booleanParam('MIGRATE_DB', env != 'dev', 'Run database migrations')
                booleanParam('ROLLBACK_ON_FAILURE', true, 'Auto-rollback on failure')
            }

            properties {
                disableConcurrentBuilds()

                if (env == 'prod') {
                    // Keep more history for production
                    buildDiscarder {
                        strategy {
                            logRotator {
                                daysToKeepStr('90')
                                numToKeepStr('100')
                            }
                        }
                    }
                }
            }

            definition {
                cps {
                    script("""
                        pipeline {
                            agent any

                            options {
                                timeout(time: ${env == 'prod' ? 30 : 15}, unit: 'MINUTES')
                                timestamps()
                            }

                            environment {
                                SERVICE_NAME = '${jobName}'
                                ENVIRONMENT = '${env}'
                                BUILD_TOOL = '${config.buildTools}'
                            }

                            stages {
                                ${env == 'prod' || env == 'staging' ? '''
                                stage('Approval') {
                                    steps {
                                        script {
                                            def approver = input(
                                                message: "Deploy to ${env.toUpperCase()}?",
                                                ok: 'Deploy',
                                                parameters: [
                                                    string(name: 'APPROVER_NAME',
                                                            defaultValue: '',
                                                            description: 'Your name')
                                                ]
                                            )
                                            echo "Approved by: \${approver}"
                                        }
                                    }
                                }
                                ''' : ''}

                                stage('Checkout') {
                                    steps {
                                        git branch: '${config.branch}',
                                            url: '${config.gitRepo}'
                                        echo "Deploying version: \${params.VERSION}"
                                    }
                                }

                                stage('Pre-Deploy Checks') {
                                    steps {
                                        echo "Running pre-deployment checks for ${env}"
                                        sh '''
                                            echo "Checking deployment prerequisites..."
                                            echo "Service: \${SERVICE_NAME}"
                                            echo "Environment: \${ENVIRONMENT}"
                                            echo "Version: \${params.VERSION}"
                                            echo "Replicas: \${params.REPLICAS}"
                                        '''
                                    }
                                }

                                stage('Database Migration') {
                                    when {
                                        expression { params.MIGRATE_DB == true }
                                    }
                                    steps {
                                        echo "Running database migrations..."
                                        sh '''
                                            echo "Backing up database..."
                                            echo "Running migrations..."
                                            sleep 2
                                            echo "Migration completed"
                                        '''
                                    }
                                }

                                stage('Build Artifacts') {
                                    steps {
                                        echo "Building deployment artifacts"
                                        script {
                                            if (env.BUILD_TOOL == 'npm') {
                                                sh '''
                                                    echo "npm run build (simulated)"
                                                    mkdir -p dist
                                                    echo "Build \${BUILD_NUMBER}" > dist/build.txt
                                                '''
                                            } else if (env.BUILD_TOOL == 'gradle') {
                                                sh '''
                                                    echo "./gradlew bootJar (simulated)"
                                                    mkdir -p build/libs
                                                    echo "Build \${BUILD_NUMBER}" > build/libs/app.jar.txt
                                                '''
                                            }
                                        }
                                    }
                                }

                                stage('Deploy') {
                                    steps {
                                        echo "Deploying to ${env} environment"
                                        sh '''
                                            echo "Deployment started at: \$(date)"
                                            echo "Service: \${SERVICE_NAME}"
                                            echo "Environment: \${ENVIRONMENT}"
                                            echo "Replicas: \${params.REPLICAS}"

                                            # Simulate deployment
                                            for i in \$(seq 1 \${params.REPLICAS}); do
                                                echo "Deploying instance \$i..."
                                                sleep 1
                                            done

                                            echo "Deployment completed"
                                        '''
                                    }
                                }

                                stage('Health Check') {
                                    steps {
                                        echo "Running health checks..."
                                        sh '''
                                            echo "Checking service health..."
                                            sleep 2

                                            # Simulate health check
                                            for i in \$(seq 1 3); do
                                                echo "Health check attempt \$i: OK"
                                                sleep 1
                                            done

                                            echo "✅ Service is healthy"
                                        '''
                                    }
                                }

                                stage('Smoke Tests') {
                                    steps {
                                        echo "Running smoke tests on ${env}"
                                        sh '''
                                            echo "Test 1: API endpoint... PASSED"
                                            echo "Test 2: Database connection... PASSED"
                                            echo "Test 3: Cache connection... PASSED"
                                            echo "Test 4: Queue connection... PASSED"
                                            echo "All smoke tests passed ✅"
                                        '''
                                    }
                                }
                            }

                            post {
                                success {
                                    echo "✅ Successfully deployed \${SERVICE_NAME} to ${env}"
                                    sh '''
                                        echo "==================================="
                                        echo "Deployment Summary"
                                        echo "==================================="
                                        echo "Service: \${SERVICE_NAME}"
                                        echo "Environment: ${env}"
                                        echo "Version: \${params.VERSION}"
                                        echo "Replicas: \${params.REPLICAS}"
                                        echo "Status: SUCCESS ✅"
                                        echo "==================================="
                                    '''
                                }

                                failure {
                                    echo "❌ Deployment to ${env} failed"
                                    script {
                                        if (params.ROLLBACK_ON_FAILURE) {
                                            echo "Initiating rollback..."
                                            sh 'echo "Rolling back to previous version..."'
                                        }
                                    }
                                }

                                always {
                                    echo "Deployment process completed"
                                }
                            }
                        }
                    """.stripIndent())
                    sandbox()
                }
            }
        }
    }
}

// Return 'this' to make the 'create' function available to the loading script
return this
