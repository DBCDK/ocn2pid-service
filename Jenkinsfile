#!groovy

def workerNode = "devel9"

pipeline {
    agent {label workerNode}
    triggers {
        pollSCM("H/03 * * * *")
    }
    options {
        timestamps()
    }
    stages {
        stage("build") {
            steps {
                script {
                    sh """#!/usr/bin/env bash
                       set -xe
                       mvn package
                       mvn test
                       junit "**/target/surefire-reports/TEST-*.xml,**/target/failsafe-reports/TEST-*.xml"
                       """
                }
            }
        }
        stage("PMD") {
            steps {
                step([$class: 'hudson.plugins.pmd.PmdPublisher',
                      pattern: '**/target/pmd.xml',
                      unstableTotalAll: "0",
                      failedTotalAll: "0"])
            }
        }
        stage("push"){
            steps {
                script {
                    sh """#!/usr/bin/env bash
                    docker tag docker-io.dbc.dk/ocn2pid-service:latest docker-io.dbc.dk/ocn2pid-service:${env.BRANCH_NAME}-${env.BUILD_NUMBER}
                    docker push docker-io.dbc.dk/ocn2pid-service:${env.BRANCH_NAME}-${env.BUILD_NUMBER} 
                    """
                }
            }
        }
    }
}
