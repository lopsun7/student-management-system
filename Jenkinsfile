pipeline {
	agent any

	options {
		timestamps()
		disableConcurrentBuilds()
	}

	triggers {
		githubPush()
	}

	environment {
		APP_NAME = 'student-management-system'
		APP_DIR = '/opt/student-management-system'
		DEPLOY_BRANCH = 'dev'
		EC2_HOST = '3.21.129.124'
		EC2_USER = 'ec2-user'
		EC2_SSH_CREDENTIALS_ID = 'student-management-ec2-key'
		SPRING_PROFILE = 'h2'
		DOWNSTREAM_DEFAULT_NAME = 'Steven'
		SONARQUBE_ENV = 'SonarQube'
	}

	stages {
		stage('Checkout') {
			steps {
				checkout scm
			}
		}

		stage('Test and Coverage') {
			steps {
				sh './mvnw clean verify'
				junit 'target/surefire-reports/*.xml'
				publishHTML(target: [
					reportDir: 'target/site/jacoco',
					reportFiles: 'index.html',
					reportName: 'JaCoCo Coverage Report',
					keepAll: true,
					alwaysLinkToLastBuild: true,
					allowMissing: false
				])
			}
		}

		stage('SonarQube Analysis') {
			steps {
				withSonarQubeEnv("${SONARQUBE_ENV}") {
					sh './mvnw org.sonarsource.scanner.maven:sonar-maven-plugin:sonar'
				}
			}
		}

		stage('Quality Gate') {
			steps {
				timeout(time: 5, unit: 'MINUTES') {
					waitForQualityGate abortPipeline: true
				}
			}
		}

		stage('Deploy to EC2') {
			when {
				expression {
					return !env.CHANGE_ID && (
						env.BRANCH_NAME == env.DEPLOY_BRANCH ||
						env.GIT_BRANCH == "origin/${env.DEPLOY_BRANCH}" ||
						env.GIT_BRANCH == "*/${env.DEPLOY_BRANCH}"
					)
				}
			}
			steps {
				sshagent(credentials: ["${EC2_SSH_CREDENTIALS_ID}"]) {
					sh 'bash scripts/jenkins-deploy-ec2.sh'
				}
			}
		}
	}

	post {
		success {
			echo 'Pipeline completed successfully.'
		}
		failure {
			echo 'Pipeline failed. Check the stage logs above.'
		}
	}
}
