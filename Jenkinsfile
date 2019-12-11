#!groovy

def workerNode = "devel9"

pipeline {
	agent {label workerNode}
	tools {
		// refers to the name set in manage jenkins -> global tool configuration
		maven "Maven 3"
	}
	environment {
		GITLAB_PRIVATE_TOKEN = credentials("metascrum-gitlab-api-token")
		MAVEN_OPTS="-Dorg.slf4j.simpleLogger.showThreadName=true"
	}
	triggers {
		pollSCM("H/03 * * * *")
		upstream(upstreamProjects: "Docker-payara5-bump-trigger",
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
				sh "mvn verify pmd:pmd"
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
		stage("docker build") {
			when {
                branch "master"
            }
			steps {
				sh "./build docker"
				sh "docker push docker-io.dbc.dk/ocn2pid-service:${env.BRANCH_NAME}-${env.BUILD_NUMBER}"
			}
		}
		stage("bump docker tag in ocn2pid-secrets") {
			agent {
				docker {
					label workerNode
					image "docker.dbc.dk/build-env:latest"
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
