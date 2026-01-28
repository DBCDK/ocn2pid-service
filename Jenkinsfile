#!groovy

def workerNode = "devel12"

pipeline {
    agent {label workerNode}
    environment {
        DOCKER_IMAGE_TAG = "${env.BRANCH_NAME}-${env.BUILD_NUMBER}"
    }
    triggers {
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
        stage("build") {
            steps {
                withSonarQubeEnv(installationName: 'sonarqube.dbc.dk') {
                    script {
                        def status = sh returnStatus: true, script: """
                        mvn -B -Dmaven.repo.local=$WORKSPACE/.repo --no-transfer-progress verify
                        """

                        def sonarOptions = "-Dsonar.branch.name=$BRANCH_NAME"
                        if (env.BRANCH_NAME != 'master') {
                            sonarOptions += " -Dsonar.newCode.referenceBranch=master"
                        }
                        status += sh returnStatus: true, script: """
                        mvn -B -Dmaven.repo.local=$WORKSPACE/.repo --no-transfer-progress $sonarOptions sonar:sonar
                        """

                        junit testResults: '**/target/*-reports/*.xml'

                        def javadoc = scanForIssues tool: [$class: 'JavaDoc']
                        publishIssues issues: [javadoc], unstableTotalAll: 1

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
                                metascrum-staging:OCN2PID_SERVICE_VERSION=$DOCKER_IMAGE_TAG
                           '''
                    }
                }
            }
		}
	}
}
