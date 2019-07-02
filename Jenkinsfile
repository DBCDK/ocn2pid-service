#!groovy

def workerNode = "devel9"

pipeline {
    agent {label workerNode}
    tools {
        // refers to the name set in manage jenkins -> global tool configuration
        maven "Maven 3"
    }
    environment {
        MAVEN_OPTS="-XX:+TieredCompilation -XX:TieredStopAtLevel=1 -Dorg.slf4j.simpleLogger.showThreadName=true"
    }
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
                       mvn -B clean
                       mvn -B install
                       mvn -B pmd:pmd verify
                       """
                }
                junit "**/target/surefire-reports/TEST-*.xml,**/target/failsafe-reports/TEST-*.xml"
            }
        }
        stage("PMD") {
            steps {
                step([$class: 'hudson.plugins.pmd.PmdPublisher',
                      pattern: '**/target/pmd.xml',
                      unstableTotalAll: "4",
                      failedTotalAll: "4"])
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
