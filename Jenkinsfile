
pipeline {


    agent any


    triggers {
        pollSCM('H/2 * * * *')
    }

    options {
        timestamps()
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }


    environment {
        JAVA_HOME  = 'C:\\Program Files\\Java\\jdk-17'
        MAVEN_HOME = 'C:\\Maven\\apache-maven-3.9.16'
        PATH = "C:\\Program Files\\Java\\jdk-17\\bin;C:\\Maven\\apache-maven-3.9.16\\bin;${env.PATH}"
    }

    stages {


        stage('Checkout') {
            steps {
                checkout scm
            }
        }


        stage('Build') {
            steps {
                bat 'mvn -B -e clean compile'
            }
        }


        stage('Test') {
            steps {
                bat 'mvn -B test'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }


        stage('Package') {
            steps {
                bat 'mvn -B package -DskipTests'
            }
            post {
                success {
                    archiveArtifacts artifacts: 'target/*.jar',
                                     fingerprint: true
                }
            }
        }
    }


    post {
        always {
            echo "===== Pipeline finished: ${currentBuild.currentResult} ====="
        }
    }
}
