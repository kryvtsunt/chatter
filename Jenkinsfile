pipeline {
   environment {
     jobBaseName = "${env.JOB_NAME}".split('/').first()
   }
  agent any
  
  stages {
   stage('Build') {
     steps {
       echo "Building"
       sh 'mvn -f Project_src/pom.xml compile'
     }
   }
   stage('Test'){
     steps {
       echo "Testing"
       sh 'mvn -f Project_src/pom.xml test'
     }
   }
   
   stage('SonarQube') {
    steps {
      withSonarQubeEnv('SonarQube') {
        sh 'mvn -f Project_src/pom.xml clean install'
        sh 'mvn -f Project_src/pom.xml sonar:sonar -Dsonar.projectKey=${jobBaseName} -Dsonar.projectName=${jobBaseName}'
      }
    }
  }
  
  stage('Quality') {
    steps {
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
