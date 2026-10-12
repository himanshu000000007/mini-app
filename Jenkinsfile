pipeline {
    agent any

    environment {
        IMAGE     = 'mini-app'
        CONTAINER = 'mini'
        HOST_PORT = '8081'      // 8080 par Jenkins hai
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

        stage('Docker Build') {
            steps {
                sh 'docker build -t ${IMAGE}:${BUILD_NUMBER} -t ${IMAGE}:latest .'
            }
        }

        stage('Deploy') {
            steps {
                sh '''
                    docker rm -f ${CONTAINER} || true
                    docker run -d --name ${CONTAINER} -p ${HOST_PORT}:8080 ${IMAGE}:${BUILD_NUMBER}
                '''
            }
        }

        stage('Smoke Test') {
            steps {
                sh '''
                    for i in $(seq 1 20); do
                        if curl -sf http://localhost:${HOST_PORT}/; then
                            echo
                            curl -sf "http://localhost:${HOST_PORT}/hello?name=Jenkins"
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
    }

    post {
        success {
            archiveArtifacts artifacts: 'target/app.jar', fingerprint: true
            echo "Build PASSED. App: http://localhost:8081"
        }
        failure {
            sh 'docker logs ${CONTAINER} || true'
            echo 'Build FAILED'
        }
    }
}
