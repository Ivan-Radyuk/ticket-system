
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
        // Кириллица в сообщениях Checkstyle и журнале приложения — UTF-8
        MAVEN_OPTS = '-Dfile.encoding=UTF-8'
    }

    stages {


        stage('Checkout') {
            steps {
                checkout scm
            }
        }


        stage('Build') {
            steps {
                bat 'chcp 65001 >nul & mvn -B -e clean compile'
            }
        }


        // Этап Static Analysis (ЛР №5): проверка исходного кода Checkstyle.
        // При нарушении правила этап падает, и следующие этапы (Test, Package)
        // не выполняются.
        stage('Static Analysis') {
            steps {
                echo 'Stage: Static Analysis -> mvn -B checkstyle:check'
                bat 'chcp 65001 >nul & mvn -B checkstyle:check'
            }
            post {
                success {
                    archiveArtifacts artifacts: 'target/checkstyle-result.xml',
                                     fingerprint: true
                }
            }
        }


        stage('Test') {
            steps {
                bat 'chcp 65001 >nul & mvn -B test'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }


        stage('Package') {
            steps {
                bat 'chcp 65001 >nul & mvn -B package -DskipTests'
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
