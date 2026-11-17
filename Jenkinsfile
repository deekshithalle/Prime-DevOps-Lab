pipeline {
    // Windows agent with JDK 21, Git, Docker Desktop and kubectl on PATH.
    agent { label 'windows-prime' }
    options {
        disableConcurrentBuilds()
        timeout(time: 15, unit: 'MINUTES')
        skipDefaultCheckout(true)
    }
    triggers { pollSCM('H/2 * * * *') }
    parameters {
        string(name: 'DOCKERHUB_USER', defaultValue: '', description: 'Docker Hub username')
        string(name: 'KUBE_CONTEXT', defaultValue: 'docker-desktop', description: 'Target Kubernetes context')
    }
    stages {
        stage('Checkout') { steps { checkout scm } }
        stage('Compile and Test') {
            steps {
                bat 'if not exist out mkdir out'
                bat 'javac -d out src\\PrimeNumber.java src\\PrimeNumberTest.java src\\PrimeServer.java'
                bat 'java -cp out PrimeNumberTest'
                bat 'java -cp out PrimeNumber 17'
            }
        }
        stage('Build Docker image') {
            steps {
                script {
                    if (!(params.DOCKERHUB_USER ==~ /[a-z0-9][a-z0-9_-]{2,29}/)) {
                        error('Set DOCKERHUB_USER to your Docker Hub username.')
                    }
                    if (!(params.KUBE_CONTEXT ==~ /[A-Za-z0-9_.:-]+/)) {
                        error('Invalid Kubernetes context.')
                    }
                    env.IMAGE = params.DOCKERHUB_USER + '/prime-lab:' + env.BUILD_NUMBER
                    env.TARGET_CONTEXT = params.KUBE_CONTEXT
                }
                bat 'docker build -t %IMAGE% .'
            }
        }
        stage('Push image') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'dockerhub', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_TOKEN')]) {
                    bat '@echo off\r\n@echo %DOCKER_TOKEN%| docker login --username %DOCKER_USER% --password-stdin'
                    bat 'docker push %IMAGE%'
                }
            }
        }
        stage('Deploy to Kubernetes') {
            steps {
                withCredentials([file(credentialsId: 'kubeconfig', variable: 'KUBECONFIG')]) {
                    powershell '''
                        $content = Get-Content k8s/app.yaml -Raw
                        $content.Replace('prime-lab:local', $env:IMAGE) | Set-Content -Encoding utf8 out/deployment.yaml
                    '''
                    bat 'kubectl --context=%TARGET_CONTEXT% apply -f out\\deployment.yaml'
                    bat 'kubectl --context=%TARGET_CONTEXT% -n prime-lab rollout status deployment/prime-app --timeout=180s'
                    bat 'kubectl --context=%TARGET_CONTEXT% -n prime-lab get pods,svc'
                }
            }
        }
    }
}
