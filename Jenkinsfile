pipeline {
    agent any

    options {
        timestamps()
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build Job Service') {
            steps {
                dir('job-service/job-service') {
                    bat 'mvnw.cmd clean package -DskipTests'
                }
            }
        }

        stage('Run Unit Tests') {
            steps {
                dir('job-service/job-service') {
                    bat 'mvnw.cmd test'
                }
            }
        }
    }

    post {
        success {
            echo 'Project Express CI pipeline completed successfully!'
        }

        failure {
            echo 'Project Express CI pipeline failed. Check the build logs.'
        }
    }
}
