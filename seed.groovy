/**
 * Main Seed Job Entry Point
 * This script loads the configuration and job templates, then generates all jobs.
 */

// 1. Create the top-level folder
folder('MyTeam') {
    displayName('My Team')
    description('Team-specific jobs with Git integration')
}

// 2. Load the configuration and job generators
// 'load' executes a script and returns its 'return' value.
def teamServices = load 'config/services.groovy'
def buildJobGenerator = load 'jobs/buildJob.groovy'
def testJobGenerator = load 'jobs/testJob.groovy'
def deployJobsGenerator = load 'jobs/deployJobs.groovy'

// 3. Loop through each service and generate its jobs
println "Found ${teamServices.size()} services to configure."
teamServices.each { jobName, config ->
    println "--> Generating jobs for: ${jobName}"

    // 'this' refers to the Job DSL factory, which must be passed
    // to the generators so they can call 'pipelineJob', etc.
    buildJobGenerator.create(jobName, config, this)
    testJobGenerator.create(jobName, config, this)
    deployJobsGenerator.create(jobName, config, this)
}

println "Job generation complete."
