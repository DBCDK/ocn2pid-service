#!groovy

def workerNode = "devel11"

pipeline {
	agent {label workerNode}
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
				sh "mvn -B verify"
				junit "**/target/surefire-reports/TEST-*.xml,**/target/failsafe-reports/TEST-*.xml"
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
        stage("bump docker tag") {
            when {
                branch "master"
            }
            steps {
                script {
                    withCredentials([sshUserPrivateKey(credentialsId: "gitlab-isworker", keyFileVariable: 'sshkeyfile')]) {
                        env.GIT_SSH_COMMAND = "ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no -i ${sshkeyfile}"
                        sh '''
                              nix run --refresh git+https://gitlab.dbc.dk/public-de-team/gitops-secrets-set-variables.git \
                                metascrum-staging:OCN2PID_SERVICE_VERSION=${env.BRANCH_NAME}-${env.BUILD_NUMBER}
                           '''
                    }
                }
            }
		}
	}
}
