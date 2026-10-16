pipeline {
    agent any
	
	environment {
		DOCKER_HOST = 'npipe:////./pipe/dockerDesktopLinuxEngine'
		PATH = "C:\\Users\\siman\\AppData\\Local\\Programs\\DockerDesktop\\resources\\bin;${env.PATH}"
    }

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
		
		stage('Build Docker Image') {
			steps {
				dir('job-service/job-service') {
					bat 'docker build -t project-express/job-service:latest .'
				}
			}
		}
		
		stage('Deploy Job Service') {
		
			environment {
				DB_USERNAME = credentials('project-express-db-username')
				DB_PASSWORD = credentials('project-express-db-password')
				JWT_SECRET = credentials('project-express-jwt-secret')
			}
		
			steps {
				bat '''
				@echo off
				docker compose up -d --no-deps --force-recreate --no-build job-service
				'''
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
