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
                      if curl --fail --silent http://localhost:8088/health >/dev/null; then
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

        stage('Push Docker Hub') {
            steps {
                withCredentials([
                    usernamePassword(
                        credentialsId: 'dockerhub-creds',
                        usernameVariable: 'DOCKERHUB_USER',
                        passwordVariable: 'DOCKERHUB_TOKEN'
                    )
                ]) {
                    sh '''
                        set +x
                        printf '%s' "$DOCKERHUB_TOKEN" | docker login --username "$DOCKERHUB_USER" --password-stdin

                        docker tag "ci/shopping-cart-backend:${BUILD_NUMBER}" "$DOCKERHUB_USER/shopping-cart-backend:${BUILD_NUMBER}"
                        docker tag "ci/shopping-cart-backend:${BUILD_NUMBER}" "$DOCKERHUB_USER/shopping-cart-backend:latest"
                        docker tag "ci/shopping-cart-frontend:${BUILD_NUMBER}" "$DOCKERHUB_USER/shopping-cart-frontend:${BUILD_NUMBER}"
                        docker tag "ci/shopping-cart-frontend:${BUILD_NUMBER}" "$DOCKERHUB_USER/shopping-cart-frontend:latest"

                        docker push "$DOCKERHUB_USER/shopping-cart-backend:${BUILD_NUMBER}"
                        docker push "$DOCKERHUB_USER/shopping-cart-backend:latest"
                        docker push "$DOCKERHUB_USER/shopping-cart-frontend:${BUILD_NUMBER}"
                        docker push "$DOCKERHUB_USER/shopping-cart-frontend:latest"
                        docker logout
                    '''
                }
            }
        }
    }

    post {
        always {
            sh 'docker compose down --volumes --remove-orphans || true'
        }
    }
}
