// =========================================================================
// Jenkinsfile для ЛР №4 — вариант 9 «Система продажи билетов»
//
// ОТЛИЧИЕ ОТ ЛР №3:
//   В ЛР №3 Pipeline был записан прямо в настройках Job, а проект лежал
//   в папке на диске (customWorkspace). Jenkins ничего не знал про Git.
//
//   В ЛР №4 Job работает в режиме «Pipeline script from SCM»:
//     Git-репозиторий (github.com/Ivan-Radyuk/ticket-system)
//            ↓  checkout  (Jenkins сам забирает исходный код)
//     Jenkins Workspace
//            ↓  читает этот Jenkinsfile из репозитория
//     Checkout → Build → Test → Package
//
//   Поэтому здесь НЕТ customWorkspace: рабочая папка = workspace самой Job,
//   и именно туда Jenkins кладёт выкачанный из Git проект.
//
// Стадии:
//   Checkout — получить исходный код из Git (видно в Console Output);
//   Build    — mvn compile;
//   Test     — mvn test  (здесь же выполняется индивидуальное зададание);
//   Package  — mvn package -DskipTests + архивирование JAR.
// =========================================================================

pipeline {

    // ---------------------------------------------------------------------
    // AGENT — любой доступный узел Jenkins.
    //   customWorkspace здесь НЕ используется: при «Pipeline script from SCM»
    //   Jenkins выкачивает репозиторий в стандартный workspace Job
    //   (C:\ProgramData\Jenkins\.jenkins\workspace\Lab4-TicketSystem)
    //   и оттуда же читает этот Jenkinsfile.
    // ---------------------------------------------------------------------
    agent any

    // ---------------------------------------------------------------------
    // TRIGGERS — автоматический запуск Pipeline после изменения репозитория.
    //
    //   pollSCM('H/2 * * * *') — раз в 2 минуты Jenkins спрашивает у Git,
    //   появились ли новые коммиты в ветке main. Если да — Pipeline
    //   запускается САМ, без нажатия Build Now.
    //
    //   ВАЖНО: триггер объявлен именно здесь, в Jenkinsfile, а не вручную
    //   в настройках Job. Jenkins сверяет настройки Job с содержимым
    //   Jenkinsfile (Declarative Pipeline) и, если в Jenkinsfile нет блока
    //   triggers, удаляет триггер, добавленный вручную в UI.
    //   Поэтому описание триггера хранится в Git вместе с проектом.
    // ---------------------------------------------------------------------
    triggers {
        pollSCM('H/2 * * * *')
    }

    // ---------------------------------------------------------------------
    // OPTIONS — общие настройки сборки.
    //   timestamps() — в Console Output у каждой строки будет время.
    //   buildDiscarder — хранить последние 10 сборок.
    // ---------------------------------------------------------------------
    options {
        timestamps()
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }

    // ---------------------------------------------------------------------
    // ENVIRONMENT — пути к Java и Maven заданы явно, чтобы пайплайн
    // работал независимо от настроек инструментов в Jenkins.
    // ---------------------------------------------------------------------
    environment {
        JAVA_HOME  = 'C:\\Program Files\\Java\\jdk-17'
        MAVEN_HOME = 'C:\\Maven\\apache-maven-3.9.16'
        PATH = "C:\\Program Files\\Java\\jdk-17\\bin;C:\\Maven\\apache-maven-3.9.16\\bin;${env.PATH}"
    }

    stages {

        // --- Stage 1: CHECKOUT (получение кода из Git) ---
        // `checkout scm` — принудительно выполняет checkout того SCM,
        // который прописан в настройках Job (Git-репозиторий).
        // В Console Output видно строку вида:
        //   Checking out Revision 1234abc... in /var/...workspace
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        // --- Stage 2: BUILD (компиляция) ---
        // mvn compile собирает src/main/java в target/classes.
        stage('Build') {
            steps {
                bat 'mvn -B -e clean compile'
            }
        }

        // --- Stage 3: TEST (запуск JUnit-тестов) ---
        // Здесь же выполняется ИНДИВИДУАЛЬНОЕ ЗАДАНИЕ варианта 9:
        //   новый тест TicketEventQueryTest для работы с билетом и событием.
        // post { always { junit ... } } — публикуем отчёт о тестах в UI Jenkins.
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

        // --- Stage 4: PACKAGE (сборка JAR) ---
        // -DskipTests — тесты уже прогнаны в стадии Test, повторно не гоняем.
        // archiveArtifacts — сохраняем JAR как артефакт сборки.
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

    // ---------------------------------------------------------------------
    // POST — выводит итог статуса сборки в Console Output.
    // ---------------------------------------------------------------------
    post {
        always {
            echo "===== Pipeline finished: ${currentBuild.currentResult} ====="
        }
    }
}
