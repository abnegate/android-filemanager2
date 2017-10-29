#!/usr/bin/env groovy

pipeline {
    agent any

    stages {
        stage('Build') {
            steps {
                echo "Building..."
                checkout scm
                sh "chmod +x gradlew && ./gradlew clean assembleDebug assembleDebugAndroidTest"
            }
        }

        stage('Test_API_26') {
            steps {
                echo "Instantiating Tests..."
                sh "emulator -avd Nexus_5_API_26 && sleep 30"
                sh "./gradlew connectedDebugAndroidTest"
            }

            post {
                always {
                    echo "Pipeline Finished!"
                }

                success {
                    echo "Pipeline Finished Successfully!"
                }

                failure {
                    echo "Test failed!"
                    mail to: 'jakeb994@gmail.com', subject: "FileMan Pipeline Test ${env.BUILD_ID} Failed!", body: "Failed"
                }
            }
        }
    }
}