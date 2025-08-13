// vars/vprofilePipeline.groovy
def call(Map config = [:]) {
    // Validate required parameters
    if (!config.imageName) {
        error("imageName parameter is required")
    }

    pipeline {
        agent any
        
        tools {
            maven config.mavenTool ?: "Maven"
        }
        
        environment {
            DOCKER_REGISTRY = "${config.dockerRegistry ?: 'docker.io'}"
            IMAGE_NAME = "${config.imageName}"
            IMAGE_TAG = "${config.imageTag ?: env.BUILD_NUMBER}"
        }
        
        stages {
            stage('Clean Workspace') {
                steps {
                    cleanWs()
                }
            }
            
            stage('Checkout vProfile') {
                steps {
                    git url: 'https://github.com/Sarwat98/vprofile-project.git', 
                         branch: config.gitBranch ?: 'master'
                }
            }
            
            stage('Build') {
                steps {
                    sh "mvn clean package -DskipTests"
                }
            }
            
            stage('Docker Build') {
                steps {
                    script {
                        docker.build(
                            "${env.IMAGE_NAME}:${env.IMAGE_TAG}",
                            "-f ${config.dockerfile ?: 'Dockerfile'} ."
                        )
                    }
                }
            }
            
            stage('Push to DockerHub') {
                steps {
                    script {
                        docker.withRegistry(
                            "https://${env.DOCKER_REGISTRY}", 
                            config.dockerCreds ?: 'dockercred'
                        ) {
                            docker.image("${env.IMAGE_NAME}:${env.IMAGE_TAG}").push()
                            docker.image("${env.IMAGE_NAME}:latest").push()
                        }
                    }
                }
            }
        }
        
        post {
            success {
                echo "vProfile deployed successfully!"
            }
            failure {
                echo "Pipeline failed"
            }
        }
    }
}
