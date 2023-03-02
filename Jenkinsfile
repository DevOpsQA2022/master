pipeline {
    agent any
    tools{
      gradle 'gradle'
    }
    stages {
        stage('Build') {
            
            steps {  
                            
               bat 'gradle build --warning-mode all'
                echo "successfully build"
                
            }
              post{
                 success{
                     echo "Archiving the Artifacts"
                     archiveArtifacts artifacts: '**/debug/*.apk'                             
                 }
            }             
        }      
            stage('Test'){
                post{
                    success{
                        emailext body: '', recipientProviders: [developers()], subject: 'build', to: 'manjula.r@ciglobalsolutions.com'
                    }
                }
                steps {
                    echo "successfully"
                }
            }
          stage('Testing Status') {
            // no agent, so executors are not used up when waiting for approvals
            agent none
            steps {
                script {
                    def deploymentDelay = input id: 'Test', message: 'Deploy to production?', submitter: 'admin', parameters: [choice(choices: ['Testing Pass','Testing Fail'], description: 'Move to deployment?', name: 'deploymentDelay')]
                   // sleep time: deploymentDelay.toInteger(), unit: 'HOURS'
                }
            }
        }
    }
}
