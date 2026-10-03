// Alternative to GitHub Actions: declarative Jenkins pipeline
pipeline {
  agent any
  environment {
    VERSION  = "1.0.${env.BUILD_NUMBER}"
    REGISTRY = "ghcr.io/darshil-jasani/college-event-mgmt"
  }
  stages {
    stage('Build & Test') {
      steps { sh "mvn -B -Drevision=${VERSION} verify" }
      post { always { junit 'target/surefire-reports/*.xml' } }
    }
    stage('Dependency/Secret Scan') {
      steps { sh 'trivy fs --exit-code 1 --severity CRITICAL,HIGH --scanners vuln,secret .' }
    }
    stage('Docker Build') {
      steps { sh "docker build --build-arg APP_VERSION=${VERSION} -t ${REGISTRY}:${VERSION} ." }
    }
    stage('Image Scan') {
      steps { sh "trivy image --exit-code 1 --severity CRITICAL ${REGISTRY}:${VERSION}" }
    }
    stage('Push') {
      steps {
        withCredentials([usernamePassword(credentialsId: 'ghcr', usernameVariable: 'U', passwordVariable: 'P')]) {
          sh "echo \$P | docker login ghcr.io -u \$U --password-stdin && docker push ${REGISTRY}:${VERSION}"
        }
      }
    }
    stage('Deploy') {
      when { branch 'main' }
      steps {
        sh """
          kubectl -n college-events set image deployment/college-event-mgmt app=${REGISTRY}:${VERSION}
          kubectl -n college-events rollout status deployment/college-event-mgmt --timeout=180s || (kubectl -n college-events rollout undo deployment/college-event-mgmt && exit 1)
        """
      }
    }
  }
}
