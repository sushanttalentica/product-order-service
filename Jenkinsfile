pipeline {
    agent any
    
    environment {
        APP_NAME = 'product-order-service'
        REGISTRY_URL = 'your-registry.com'
        KUBECONFIG = credentials('kubeconfig')
        AWS_ACCESS_KEY_ID = credentials('aws-access-key')
        AWS_SECRET_ACCESS_KEY = credentials('aws-secret-key')
        S3_BUCKET = 'my-pos-bucket-125'
        S3_REGION = 'ap-south-1'
    }
    
    stages {
        stage('Checkout') {
            steps {
                checkout scm
                script {
                    env.GIT_COMMIT_SHORT = sh(
                        script: 'git rev-parse --short HEAD',
                        returnStdout: true
                    ).trim()
                    env.BUILD_TAG = "${APP_NAME}:${BUILD_NUMBER}-${env.GIT_COMMIT_SHORT}"
                }
            }
        }
        
        stage('Build') {
            steps {
                script {
                    echo "Building application..."
                    sh 'mvn clean compile'
                }
            }
        }
        
        stage('Test') {
            parallel {
                stage('Unit Tests') {
                    steps {
                        script {
                            echo "Running unit tests..."
                            sh 'mvn test'
                        }
                    }
                }
                
                stage('Integration Tests') {
                    steps {
                        script {
                            echo "Running integration tests..."
                            sh 'mvn verify -Pintegration-tests'
                        }
                    }
                }
                
                stage('Security Scan') {
                    steps {
                        script {
                            echo "Running security scan..."
                            sh 'mvn dependency-check:check'
                        }
                    }
                }
            }
        }
        
        stage('Quality Gate') {
            steps {
                script {
                    echo "Running quality gate..."
                    sh 'mvn jacoco:report'
                    
                    // Check coverage threshold
                    def coverage = sh(
                        script: 'mvn jacoco:check | grep -o "[0-9]*%" | head -1 | sed "s/%//"',
                        returnStdout: true
                    ).trim()
                    
                    if (coverage.toInteger() < 80) {
                        error "Coverage threshold not met: ${coverage}% < 80%"
                    }
                }
            }
        }
        
        stage('Package') {
            steps {
                script {
                    echo "Packaging application..."
                    sh 'mvn package -DskipTests'
                }
            }
        }
        
        stage('Build Docker Image') {
            steps {
                script {
                    echo "Building Docker image..."
                    sh "docker build -t ${env.BUILD_TAG} ."
                    sh "docker tag ${env.BUILD_TAG} ${REGISTRY_URL}/${APP_NAME}:latest"
                }
            }
        }
        
        stage('Push to Registry') {
            steps {
                script {
                    echo "Pushing to registry..."
                    sh "docker push ${REGISTRY_URL}/${APP_NAME}:${BUILD_NUMBER}-${env.GIT_COMMIT_SHORT}"
                    sh "docker push ${REGISTRY_URL}/${APP_NAME}:latest"
                }
            }
        }
        
        stage('Deploy to UAT') {
            when {
                branch 'develop'
            }
            steps {
                script {
                    echo "Deploying to UAT..."
                    sh "kubectl set image deployment/${APP_NAME} ${APP_NAME}=${REGISTRY_URL}/${APP_NAME}:${BUILD_NUMBER}-${env.GIT_COMMIT_SHORT} -n uat"
                    sh "kubectl rollout status deployment/${APP_NAME} -n uat"
                }
            }
        }
        
        stage('UAT Tests') {
            when {
                branch 'develop'
            }
            steps {
                script {
                    echo "Running UAT tests..."
                    sh "mvn verify -Puat-tests -Dtest.server.url=http://uat-api.yourdomain.com"
                }
            }
        }
        
        stage('Deploy to Production') {
            when {
                branch 'main'
            }
            steps {
                script {
                    echo "Deploying to production..."
                    sh "kubectl set image deployment/${APP_NAME} ${APP_NAME}=${REGISTRY_URL}/${APP_NAME}:${BUILD_NUMBER}-${env.GIT_COMMIT_SHORT} -n prod"
                    sh "kubectl rollout status deployment/${APP_NAME} -n prod"
                }
            }
        }
        
        stage('Production Smoke Tests') {
            when {
                branch 'main'
            }
            steps {
                script {
                    echo "Running production smoke tests..."
                    sh "mvn verify -Psmoke-tests -Dtest.server.url=https://api.yourdomain.com"
                }
            }
        }
    }
    
    post {
        always {
            script {
                echo "Cleaning up..."
                sh 'docker system prune -f'
            }
        }
        
        success {
            script {
                echo "Pipeline completed successfully!"
                // Send success notification
                slackSend(
                    channel: '#deployments',
                    color: 'good',
                    message: "✅ ${APP_NAME} deployed successfully to ${env.BRANCH_NAME} (Build #${BUILD_NUMBER})"
                )
            }
        }
        
        failure {
            script {
                echo "Pipeline failed!"
                // Send failure notification
                slackSend(
                    channel: '#deployments',
                    color: 'danger',
                    message: "❌ ${APP_NAME} deployment failed on ${env.BRANCH_NAME} (Build #${BUILD_NUMBER})"
                )
            }
        }
        
        unstable {
            script {
                echo "Pipeline completed with warnings!"
                // Send warning notification
                slackSend(
                    channel: '#deployments',
                    color: 'warning',
                    message: "⚠️ ${APP_NAME} deployment completed with warnings on ${env.BRANCH_NAME} (Build #${BUILD_NUMBER})"
                )
            }
        }
    }
}
