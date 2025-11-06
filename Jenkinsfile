pipeline {
    agent any
    tools {
        // Ensure you have these tools configured in Jenkins Global Tool Configuration
        maven 'Maven 3'
        jdk 'JDK 17'
        nodejs 'NodeJS 18'
    }
    environment {
        // Optional: Define any environment variables needed
        CI = 'true'
    }
    stages {
        stage('Installation') {
            steps {
                sh 'npm install'
            }
        }
        stage('Backend Tests') {
            steps {
                sh './mvnw -ntp clean verify'
            }
        }
        stage('Frontend Tests') {
            steps {
                sh 'npm test'
            }
        }
        stage('Package Application') {
            steps {
                echo 'Packaging application for production...'
                sh './mvnw -ntp package -Pprod -DskipTests'
            }
        }
    }
    post {
        always {
            // Archive the build artifact
            archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
        }
        success {
            echo 'Build successful!'
        }
        failure {
            echo 'Build failed.'
        }
    }
}