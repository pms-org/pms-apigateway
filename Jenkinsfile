pipeline {
    agent any

    environment {
        // Updated to your Docker Hub username and repository
        DOCKERHUB_REPO = "gurum110/pms_api_gateway"
        IMAGE_TAG = "${BUILD_NUMBER}" 
    }

    stages {
        stage('Git Checkout') {
            steps {
                // Pulls your code from your GitHub repository
                git branch: 'main', url: 'https://github.com/pms-org/pms-apigateway.git'
            }
        }

        stage('Build Artifact') {
    steps {
        // Use the name you gave in Step 1
        script {
            def mavenHome = tool 'maven-3.9' 
            sh "${mavenHome}/bin/mvn clean package -DskipTests"
        }
    }
}

        stage('Build & Push Docker Image') {
            steps {
                script {
                    // 1. Build the image locally on the Jenkins server
                    // We tag it with the Jenkins Build Number and 'latest'
                    sh "docker build -t ${DOCKERHUB_REPO}:${IMAGE_TAG} -t ${DOCKERHUB_REPO}:latest ."
                    
                    // 2. Log in and Push to Docker Hub
                    // NOTE: Ensure you have a Jenkins credential named 'dockerhub-creds'
                    withCredentials([usernamePassword(credentialsId: 'dockerhub-creds', usernameVariable: 'USER', passwordVariable: 'PASS')]) {
                        sh "echo $PASS | docker login -u $USER --password-stdin"
                        sh "docker push ${DOCKERHUB_REPO}:${IMAGE_TAG}"
                        sh "docker push ${DOCKERHUB_REPO}:latest"
                    }
                }
            }
        }

        stage('Cleanup Local Images') {
            steps {
                // Clean up to prevent the Jenkins server disk from filling up
                sh "docker rmi ${DOCKERHUB_REPO}:${IMAGE_TAG} ${DOCKERHUB_REPO}:latest"
            }
        }
    }



    post {
        success {
            echo "Successfully pushed ${DOCKERHUB_REPO}:${IMAGE_TAG} to guru110's Docker Hub."
        }
        failure {
            echo "Pipeline failed. Please check the Jenkins console output."
        }
    }
}