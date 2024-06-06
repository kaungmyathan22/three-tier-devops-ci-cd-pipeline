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
                        sh 'docker build -t kaungmyathan/campa:latest .'
                    }
                }
            }
        }
        stage('Trivy Image Scan') {
            steps {
                sh 'trivy image --format table -o fs-report.html kaungmyathan/campa:latest'
            }
        }
        stage('Docker Push Image') {
            steps {
                script {
                    withDockerRegistry(credentialsId: 'docker-credentials', toolName: 'docker') {
                        sh 'docker push kaungmyathan/campa:latest'
                    }
                }
            }
        }
        stage('Deploy to k8s') {
            steps {
                script {
                    withKubeCredentials(kubectlCredentials: [[caCertificate: '', clusterName: 'eks-5', contextName: '', credentialsId: 'k8-token', namespace: 'webapps', serverUrl: 'https://A6ABE060CD2E69460427F5BBEF3C3B5F.gr7.ap-southeast-1.eks.amazonaws.com']]) {
                        sh 'kubectl apply -f Manifests/dss.yml'
                        sleep 60
                    }
                }
            }
        }
        stage('verify the deployment') {
            steps {
                script {
                    withKubeCredentials(kubectlCredentials: [[caCertificate: '', clusterName: 'eks-5', contextName: '', credentialsId: 'k8-token', namespace: 'webapps', serverUrl: 'https://A6ABE060CD2E69460427F5BBEF3C3B5F.gr7.ap-southeast-1.eks.amazonaws.com']]) {
                        sh 'kubectl get pods -n webapps'
                        sh 'kubectl get svc -n webapps'
                    }
                }
            }
        }
    }
}
