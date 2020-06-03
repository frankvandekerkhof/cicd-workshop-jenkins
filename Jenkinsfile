pipeline {
    agent any

    stages{
        stage("Sequential: clean, compile, test and build"){
            stages{
                stage("clean") {
                    steps { sh "git clean -xdf" }
                }
                stage("compile") {
                    steps { sh "./gradlew compileJava" }
                }
                stage("test") {
                    steps { sh './gradlew test --no-daemon' }
                }
                stage("build"){
                   steps{ sh './gradlew build -x test --no-daemon' }
                }
                stage("Checkstyle"){
                    steps{ sh './gradlew checkstyleMain --no-daemon' //run a gradle task }
                }
				stage('Deploy') {	
					environment {
						HEROKU_API_KEY = credentials('heroku-api-key-frank')
						}
					steps {
						script {
							id("com.heroku.sdk.heroku-gradle") version "1.0.4"
						}
					}
				}
            }
        }
    }
}