pipeline {
    agent any

    environment {
        IMAGE          = 'mini-app'
        DOCKERHUB_USER = 'himanshu5452006'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build and Test') {
            steps {
                sh 'mvn -B clean package'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withCredentials([string(credentialsId: 'sonar-token', variable: 'SONAR_TOKEN')]) {
                    sh '''
                        mvn -B org.sonarsource.scanner.maven:sonar-maven-plugin:4.0.0.4121:sonar \
                            -Dsonar.projectKey=mini-app \
                            -Dsonar.host.url=http://localhost:9000 \
                            -Dsonar.token=${SONAR_TOKEN}
                    '''
                }
            }
        }

        stage('Docker Build') {
            steps {
                sh 'docker build -t ${IMAGE}:${BUILD_NUMBER} -t ${IMAGE}:latest .'
            }
        }

        stage('Deploy') {
            steps {
                sh '''
                    docker rm -f mini || true
                    APP_TAG=${BUILD_NUMBER} docker compose up -d --build --remove-orphans
                '''
            }
        }

        stage('Smoke Test') {
            steps {
                sh '''
                    for i in $(seq 1 20); do
                        if curl -sf http://localhost:8081/; then
                            echo
                            curl -sf "http://localhost:8081/hello?name=Jenkins"
                            echo
                            exit 0
                        fi
                        sleep 3
                    done
                    echo "App did not start"
                    exit 1
                '''
            }
        }

        stage('Push to Docker Hub') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'dockerhub-creds',
                                                  usernameVariable: 'DH_USER',
                                                  passwordVariable: 'DH_PASS')]) {
                    sh '''
                        echo "$DH_PASS" | docker login -u "$DH_USER" --password-stdin
                        docker tag ${IMAGE}:${BUILD_NUMBER} ${DOCKERHUB_USER}/${IMAGE}:${BUILD_NUMBER}
                        docker tag ${IMAGE}:${BUILD_NUMBER} ${DOCKERHUB_USER}/${IMAGE}:latest
                        docker push ${DOCKERHUB_USER}/${IMAGE}:${BUILD_NUMBER}
                        docker push ${DOCKERHUB_USER}/${IMAGE}:latest
                        docker logout
                    '''
                }
            }
        }
    }

    post {
        success {
            archiveArtifacts artifacts: 'target/app.jar', fingerprint: true
            echo 'Build PASSED. App: http://localhost:8081'
        }
        failure {
            sh 'docker compose logs --tail=50 || true'
            echo 'Build FAILED'
        }
    }
}
