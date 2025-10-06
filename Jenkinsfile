pipeline {
    agent any
    
    environment {
        DOCKER_REGISTRY = 'your-docker-registry.com'
        IMAGE_NAME = 'product-order-service'
        MAVEN_OPTS = '-Xmx1024m'
    }
    
    parameters {
        choice(
            name: 'ENVIRONMENT',
            choices: ['dev', 'uat', 'prod'],
            description: 'Target environment for deployment'
        )
        booleanParam(
            name: 'RUN_TESTS',
            defaultValue: true,
            description: 'Run tests before deployment'
        )
        booleanParam(
            name: 'SKIP_TESTS',
            defaultValue: false,
            description: 'Skip running tests'
        )
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
                    env.BUILD_TAG = "${env.BUILD_NUMBER}-${env.GIT_COMMIT_SHORT}"
                }
            }
        }
        
        stage('Build') {
            steps {
                script {
                    sh """
                        mvn clean compile -DskipTests=${params.SKIP_TESTS}
                    """
                }
            }
        }
        
        stage('Test') {
            when {
                not { params.SKIP_TESTS }
                expression { params.RUN_TESTS }
            }
            steps {
                script {
                    sh """
                        mvn test -Dspring.profiles.active=${params.ENVIRONMENT}
                    """
                }
            }
            post {
                always {
                    publishTestResults testResultsPattern: 'target/surefire-reports/*.xml'
                    publishHTML([
                        allowMissing: false,
                        alwaysLinkToLastBuild: true,
                        keepAll: true,
                        reportDir: 'target/site/surefire-reports',
                        reportFiles: 'index.html',
                        reportName: 'Test Report'
                    ])
                }
            }
        }
        
        stage('Package') {
            steps {
                script {
                    sh """
                        mvn package -DskipTests=true -Dspring.profiles.active=${params.ENVIRONMENT}
                    """
                }
            }
        }
        
        stage('Docker Build') {
            steps {
                script {
                    def imageTag = "${env.DOCKER_REGISTRY}/${env.IMAGE_NAME}:${env.BUILD_TAG}"
                    def latestTag = "${env.DOCKER_REGISTRY}/${env.IMAGE_NAME}:latest-${params.ENVIRONMENT}"
                    
                    sh """
                        docker build -t ${imageTag} .
                        docker tag ${imageTag} ${latestTag}
                    """
                    
                    env.DOCKER_IMAGE = imageTag
                    env.DOCKER_IMAGE_LATEST = latestTag
                }
            }
        }
        
        stage('Docker Push') {
            steps {
                script {
                    sh """
                        docker push ${env.DOCKER_IMAGE}
                        docker push ${env.DOCKER_IMAGE_LATEST}
                    """
                }
            }
        }
        
        stage('Deploy to Dev') {
            when {
                expression { params.ENVIRONMENT == 'dev' }
            }
            steps {
                script {
                    sh """
                        # Deploy to development environment
                        kubectl apply -f k8s/dev/namespace.yaml
                        kubectl apply -f k8s/dev/configmap.yaml
                        kubectl apply -f k8s/dev/secret.yaml
                        kubectl set image deployment/product-order-service product-order-service=${env.DOCKER_IMAGE} -n dev
                        kubectl rollout status deployment/product-order-service -n dev
                    """
                }
            }
        }
        
        stage('Deploy to UAT') {
            when {
                expression { params.ENVIRONMENT == 'uat' }
            }
            steps {
                script {
                    sh """
                        # Deploy to UAT environment
                        kubectl apply -f k8s/uat/namespace.yaml
                        kubectl apply -f k8s/uat/configmap.yaml
                        kubectl apply -f k8s/uat/secret.yaml
                        kubectl set image deployment/product-order-service product-order-service=${env.DOCKER_IMAGE} -n uat
                        kubectl rollout status deployment/product-order-service -n uat
                    """
                }
            }
        }
        
        stage('Deploy to Prod') {
            when {
                expression { params.ENVIRONMENT == 'prod' }
            }
            steps {
                script {
                    // Production deployment with approval
                    input message: 'Deploy to Production?', ok: 'Deploy'
                    
                    sh """
                        # Deploy to production environment
                        kubectl apply -f k8s/prod/namespace.yaml
                        kubectl apply -f k8s/prod/configmap.yaml
                        kubectl apply -f k8s/prod/secret.yaml
                        kubectl set image deployment/product-order-service product-order-service=${env.DOCKER_IMAGE} -n prod
                        kubectl rollout status deployment/product-order-service -n prod
                    """
                }
            }
        }
        
        stage('Health Check') {
            steps {
                script {
                    def healthCheckUrl = getHealthCheckUrl(params.ENVIRONMENT)
                    sh """
                        # Wait for service to be ready
                        sleep 30
                        
                        # Health check
                        for i in {1..10}; do
                            if curl -f ${healthCheckUrl}/actuator/health; then
                                echo "Health check passed"
                                break
                            else
                                echo "Health check failed, retrying in 10 seconds..."
                                sleep 10
                            fi
                        done
                    """
                }
            }
        }
    }
    
    post {
        always {
            cleanWs()
        }
        success {
            script {
                def message = """
                ✅ Deployment Successful!
                Environment: ${params.ENVIRONMENT}
                Image: ${env.DOCKER_IMAGE}
                Build: ${env.BUILD_TAG}
                """
                echo message
            }
        }
        failure {
            script {
                def message = """
                ❌ Deployment Failed!
                Environment: ${params.ENVIRONMENT}
                Build: ${env.BUILD_TAG}
                """
                echo message
            }
        }
    }
}

def getHealthCheckUrl(environment) {
    switch(environment) {
        case 'dev':
            return 'http://product-order-service-dev.example.com'
        case 'uat':
            return 'http://product-order-service-uat.example.com'
        case 'prod':
            return 'http://product-order-service.example.com'
        default:
            return 'http://localhost:8080'
    }
}
