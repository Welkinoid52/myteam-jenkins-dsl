Jenkins Job DSL Repository

This repository stores the Jenkins Job DSL scripts for generating all pipelines for MyTeam.

Structure

/seed.groovy: The main entry point that Jenkins runs. It reads the configuration and calls the job generators.

/config/services.groovy: This is where you add or edit services. This file is a simple list of all microservices, their repos, and their environments.

/jobs/*.groovy: These are the "template" files. Each file is responsible for generating a specific type of job (e.g., buildJob.groovy, testJob.groovy). You should only edit these if you want to change the structure of the generated Jenkins jobs (e.g., add a new parameter, change a trigger).

How it Works

A "Seed Job" in Jenkins (e.g., MyTeam-Job-Generator) is configured to pull this repository.

It is configured to execute the seed.groovy script.

The seed.groovy script:

Loads the service list from config/services.groovy.

Loads the job templates from jobs/.

Loops through each service and calls the templates to generate the full set of Jenkins jobs.

How to Add a New Service

Open the config/services.groovy file.

Copy an existing entry (like frontend-build).

Paste it and change the key (e.g., 'my-new-service') and update its displayName, gitRepo, buildTools, etc.

Commit and push your change.

Run the main "Seed Job" in Jenkins to generate the new pipelines.