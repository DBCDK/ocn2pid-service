#!groovy

def workerNode = "devel11"

pipeline {
	agent {label workerNode}
	tools {
		// refers to the name set in manage jenkins -> global tool configuration
		jdk 'jdk11'
		maven 'Maven 3'
	}
	environment {
		GITLAB_PRIVATE_TOKEN = credentials("metascrum-gitlab-api-token")
		MAVEN_OPTS="-Dorg.slf4j.simpleLogger.showThreadName=true"
		SONAR_SCANNER = "$SONAR_SCANNER_HOME/bin/sonar-scanner"
		SONAR_PROJECT_KEY = "ocn2pid-service"
		SONAR_SOURCES = "src"
		SONAR_TESTS = "test"
	}
	triggers {
		pollSCM("H/03 * * * *")
		upstream(upstreamProjects: "Docker-payara6-bump-trigger",
            threshold: hudson.model.Result.SUCCESS)
	}
	options {
		timestamps()
		disableConcurrentBuilds()
	}
	stages {
		stage("clear workspace") {
			steps {
				deleteDir()
				checkout scm
			}
		}
		stage("verify") {
			steps {
				sh "mvn -B verify pmd:pmd"
				junit "**/target/surefire-reports/TEST-*.xml,**/target/failsafe-reports/TEST-*.xml"
			}
		}
		stage("PMD") {
			steps {
				// 3 PMD warnings for unused imports.
				// Must be addressed! (sometime).
				step([$class: 'hudson.plugins.pmd.PmdPublisher',
					  pattern: '**/target/pmd.xml',
					  unstableTotalAll: "4",
					  failedTotalAll: "4"])
			}
		}
		stage("sonarqube") {
			steps {
				withSonarQubeEnv(installationName: 'sonarqube.dbc.dk') {
					script {
						def status = 0

						def sonarOptions = "-Dsonar.branch.name=${BRANCH_NAME}"
						if (env.BRANCH_NAME != 'master') {
							sonarOptions += " -Dsonar.newCode.referenceBranch=master"
						}

						// Do sonar via maven
						status += sh returnStatus: true, script: """
                            mvn -B $sonarOptions sonar:sonar
                        """

						if (status != 0) {
							error("build failed")
						}
					}
				}
			}
		}
		stage("quality gate") {
			steps {
				// wait for analysis results
				timeout(time: 1, unit: 'HOURS') {
					waitForQualityGate abortPipeline: true
				}
			}
		}
		stage("docker build") {
			steps {
				sh "./build docker"
				sh "docker push docker-metascrum.artifacts.dbccloud.dk/ocn2pid-service:${env.BRANCH_NAME}-${env.BUILD_NUMBER}"
			}
		}
		stage("bump docker tag in ocn2pid-secrets") {
			agent {
				docker {
					label workerNode
					image "docker-dbc.artifacts.dbccloud.dk/build-env:latest"
					alwaysPull true
				}
			}
			when {
				branch "master"
			}
			steps {
				script {
					sh """  
                        set-new-version ocn2pid.yml ${env.GITLAB_PRIVATE_TOKEN} metascrum/ocn2pid-secrets ${env.BRANCH_NAME}-${env.BUILD_NUMBER} -b staging
                    """
				}
			}
		}
	}
}
