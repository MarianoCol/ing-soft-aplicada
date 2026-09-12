pipeline {
    agent any

    options {
        timestamps()
        disableConcurrentBuilds()
    }

    environment {
        COMPOSE_PROJECT_NAME = "shopping-cart-ci-${BUILD_NUMBER}"
        DOCKERHUB_USERNAME = "ci"
        IMAGE_TAG = "${BUILD_NUMBER}"
        TESTCONTAINERS_HOST_OVERRIDE = "host.docker.internal"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Backend tests') {
            steps {
                dir('backend') {
                    sh './mvnw -ntp verify'
                }
            }
        }

        stage('Frontend quality') {
            steps {
                dir('frontend') {
                    sh 'npm ci'
                    sh 'npm run lint'
                    sh 'npm run test -- --configuration=ci'
                    sh 'npm run build'
                }
            }
        }

        stage('Build images') {
            steps {
                sh 'docker build --tag ci/shopping-cart-backend:${BUILD_NUMBER} backend'
                sh 'docker build --tag ci/shopping-cart-frontend:${BUILD_NUMBER} frontend'
            }
        }

        stage('E2E') {
            steps {
                sh 'docker compose up --detach --no-build postgresql backend frontend'
                sh '''
                    for attempt in $(seq 1 40); do
                      if curl --fail --silent http://localhost:8080/health >/dev/null; then
                        exit 0
                      fi
                      sleep 5
                    done
                    docker compose ps
                    exit 1
                '''
                sh '''
                    docker run --rm \
                      --network "${COMPOSE_PROJECT_NAME}_application" \
                      --volumes-from "$(hostname)" \
                      --workdir "${WORKSPACE}/frontend" \
                      cypress/included:15.21.1 \
                      --config baseUrl=http://frontend:8080 \
                      --env apiUrl=http://backend:8080
                '''
            }
        }

    }

    post {
        always {
            sh 'docker compose down --volumes --remove-orphans || true'
        }
    }
}
