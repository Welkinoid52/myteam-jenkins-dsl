/**
 * Service Configuration
 *
 * This file defines all services that the seed job will
 * generate pipelines for.
 *
 * It returns a Groovy Map.
 */

return [
    'frontend-build': [
        displayName: 'Frontend Build',
        description: 'React/Angular frontend application build pipeline',
        gitRepo: 'https://github.com/Welkinoid52/myteam-jenkins-dsl.git',
        branch: 'main',
        jenkinsfile: 'Jenkinsfile',
        buildTools: 'npm',
        environments: ['dev', 'staging', 'prod']
    ],
    'backend-build': [
        displayName: 'Backend Build',
        description: 'Spring Boot/Node.js backend service build pipeline',
        gitRepo: 'https://github.com/Welkinoid52/myteam-jenkins-dsl.git',
        branch: 'main',
        jenkinsfile: 'Jenkinsfile',
        buildTools: 'gradle',
        environments: ['dev', 'staging', 'prod']
    ]
    // To add a new service, just add a new entry here!
    // 'new-service': [ ... ]
]
