#!/usr/bin/env groovy

pipeline {
    agent any

    stages {
        stage('Build') {
            steps {
                echo "Building..."
                checkout scm
            }
        }

        stage('Test') {
            steps {
                echo "Instantiating Tests..."
            }

            post {
                always {
                    
                }

                failure {
                     echo "Test failed!"
                     mail to: 'jakeb994@gmail.com', subject: "FileMan Pipeline Test ${env.BUILD_ID} Failed!"
                }
            }
        }
    }
}