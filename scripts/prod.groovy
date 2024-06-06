pipeline {
    agent any
    tools {
        nodejs 'node21'
    }
    environment {
        SCANNER_HOME = tool 'sonar-scanner'
    }
    stages {
        stage('Git checkout') {
            steps {
                git branch: 'main', credentialsId: 'git-cred', url: 'https://github.com/kaungmyathan22/three-tier-devops-ci-cd-pipeline'
            }
        }
        stage('Install package dependencies') {
            steps {
                sh 'npm install'
            }
        }
        stage('Unit Tests Cases') {
            steps {
                sh 'npm test'
            }
        }
        stage('Trivy fs scan') {
            steps {
                sh 'trivy fs --format table -o fs-report.html .'
            }
        }
        stage('Sonarqube') {
            steps {
                withSonarQubeEnv('sonar') {
                    sh '$SCANNER_HOME/bin/sonar-scanner -Dsonar.projectKey=Campground -Dsonar.projectName=Campground'
                }
            }
        }
        stage('Docker Build & Tag') {
            steps {
                script {
                    withDockerRegistry(credentialsId: 'docker-credentials', toolName:'docker') {
                        sh 'docker build -t kaungmyathan/camp:latest .'
                    }
                }
            }
        }
        stage('Trivy Image Scan') {
            steps {
                sh 'trivy image --format table -o fs-report.html kaungmyathan/camp:latest'
            }
        }
        stage('Docker Push Image') {
            steps {
                script {
                    withDockerRegistry(credentialsId: 'docker-credentials', toolName: 'docker') {
                        sh 'docker push kaungmyathan/camp:latest'
                    }
                }
            }
        }
        stage('Docker deploy') {
            steps {
                script {
                    withDockerRegistry(credentialsId: 'docker-credentials', toolName: 'docker') {
                        sh 'docker run -d -p 3000:3000 kaungmyathan/camp:latest'
                    }
                }
            }
        }
    }
}
