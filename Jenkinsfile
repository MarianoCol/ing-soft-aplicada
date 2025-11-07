pipeline {
    agent any
    environment {
        CI = 'true'
    }
    stages {
        stage('checkout') {
            steps {
                checkout scm
            }
        }

        stage('check java') {
            steps {
                sh "java -version"
            }
        }

        stage('clean') {
            steps {
                sh "chmod +x mvnw"
                sh "./mvnw -ntp clean -P-webapp"
            }
        }
        stage('nohttp') {
            steps {
                sh "./mvnw -ntp checkstyle:check"
            }
        }

        stage('install tools') {
            steps {
                sh "./mvnw -ntp com.github.eirslett:frontend-maven-plugin:install-node-and-npm@install-node-and-npm"
            }
        }

        stage('npm install') {
            steps {
                sh "./mvnw -ntp com.github.eirslett:frontend-maven-plugin:npm"
            }
        }
        stage('packaging') {
            steps {
                sh "./mvnw -ntp verify -P-webapp -Pprod -DskipTests"
                archiveArtifacts artifacts: '**/target/*.jar', fingerprint: true
            }
        }
        stage('Build and Publish') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'dockerhub-login', passwordVariable: 'DOCKER_REGISTRY_PWD', usernameVariable: 'DOCKER_REGISTRY_USER')]) {
                    sh './mvnw -ntp -Pprod verify jib:build'
                }
            }
        }
    }
    post {
        always {
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