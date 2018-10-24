pipeline {
 environment {
   jobBaseName = "${env.JOB_NAME}".split('/').first()
 }
 agent any

 stages {
   stage('Build') {
     steps {
       echo "Building Chatter"
       sh 'mvn -f Project_src/Chatter/pom.xml compile'
       echo "Building Prattle"
       sh 'mvn -f Project_src/Prattle/pom.xml compile'
     }
   }

   stage('SonarQube') {
    steps {
      withSonarQubeEnv('SonarQube') {
        sh 'mvn -f Project_src/Prattle/pom.xml clean install'
        sh 'mvn -f Project_src/Prattle/pom.xml sonar:sonar -Dsonar.projectKey=${jobBaseName} -Dsonar.projectName=${jobBaseName}'
      }

      sh 'sleep 30'
      timeout(time: 10, unit: 'SECONDS') {
       retry(5) {
        script {
          def qg = waitForQualityGate()
          if (qg.status != 'OK') {
            error "Pipeline aborted due to quality gate failure: ${qg.status}"
          }
        }
      }
    }
  }
}
}
}
