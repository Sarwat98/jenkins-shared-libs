def call(Map config = [:]) {
    pipeline {
        agent any
        
        tools {
            maven config.mavenTool ?: "Maven"
        }
        
        environment {
            DOCKER_REGISTRY = config.dockerRegistry ?: 'docker.io'
            IMAGE_NAME = config.imageName
            IMAGE_TAG = config.imageTag ?: "${env.BUILD_NUMBER}"
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
                         branch: config.gitBranch ?: 'main'
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
                            "${IMAGE_NAME}:${IMAGE_TAG}",
                            "-f ${config.dockerfile ?: 'Dockerfile'} ."
                        )
                    }
                }
            }
            
            stage('Push to DockerHub') {
                steps {
                    script {
                        docker.withRegistry(
                            "https://${DOCKER_REGISTRY}", 
                            config.dockerCreds ?: 'dockercred'
                        ) {
                            docker.image("${IMAGE_NAME}:${IMAGE_TAG}").push()
                            docker.image("${IMAGE_NAME}:latest").push()
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
