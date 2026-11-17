# Prime Number DevOps External Lab

SET 2(a) Prime checking with Git add/commit/push.
SET 2(b) Jenkins CI with Kubernetes deployment.
The supplied factorial reference is adapted to prime checking.

## 1. Software and local run
Install JDK 21 or later, Git, VS Code, Docker Desktop with Linux containers,
kubectl, and Jenkins. Enable Kubernetes in Docker Desktop.
Use a current Jenkins LTS and a Java version supported by that release.
Use JDK 21 to build the project.

Extract this ZIP and open Prime-DevOps-Lab in VS Code.
Run each command from the folder containing src, web and Jenkinsfile:

    java -version
    javac -version
    git --version
    mkdir out
    javac -d out src/PrimeNumber.java src/PrimeNumberTest.java src/PrimeServer.java
    java -cp out PrimeNumberTest
    java -cp out PrimeNumber
    java -cp out PrimeServer

Enter 17 in the CLI to get "17 is a prime number."
Browser: http://localhost:8081. Keep the server terminal open.
Ctrl+C stops the server.
Optional helper:
    powershell.exe -NoProfile -ExecutionPolicy RemoteSigned -File .\run.ps1

## 2. Git / GitHub
Create an EMPTY GitHub repository called Prime-DevOps-Lab.
Use your real name/email. Replace YOUR_USERNAME in the remote URL.

    git config --global user.name "YOUR NAME"
    git config --global user.email "YOUR EMAIL"
    git init -b main
    git add .
    git commit -m "Add prime application and DevOps pipeline"
    git remote add origin https://github.com/YOUR_USERNAME/Prime-DevOps-Lab.git
    git push -u origin main

Authenticate through Git Credential Manager when prompted.
For later changes: git add . ; git commit -m "Update app" ; git push origin main
Run those three commands separately. Keep credentials out of Git.

## 3. Docker
Start Docker Desktop. Run:

    docker info
    docker build -t prime-lab:local .
    docker run --rm -p 8082:8081 prime-lab:local

Open http://localhost:8082. Ctrl+C stops the container.
The image build compiles Java and runs the 13 automated checks.

## 4. Kubernetes
Enable Kubernetes in Docker Desktop Settings and wait until ready.
These commands explicitly target the local docker-desktop context:

    kubectl --context=docker-desktop get nodes
    kubectl --context=docker-desktop apply -f k8s/app.yaml
    kubectl --context=docker-desktop -n prime-lab rollout status deployment/prime-app --timeout=180s
    kubectl --context=docker-desktop -n prime-lab get pods,svc
    kubectl --context=docker-desktop -n prime-lab port-forward service/prime-app 8083:80

Open http://localhost:8083 while port-forward remains running.
The manifest creates a prime-lab namespace, two replicas, readiness/liveness
checks and a ClusterIP Service. Manual deployment uses prime-lab:local.
If the cluster cannot see a local image, push it to a PUBLIC Docker Hub
repository and replace prime-lab:local in the manifest with that image tag.

## 5. Jenkins prerequisites
Install Jenkins from https://www.jenkins.io/download/ and open localhost:8080.
Read the initial password from the actual file path shown by setup.
Install suggested plugins and create an admin account.
Required plugins: Pipeline, Pipeline: Declarative, Git and Credentials Binding.

Use a WINDOWS build agent on the same host as Docker Desktop for this lab.
Manage Jenkins > Nodes > select the Windows node > Configure.
Set label to windows-prime and make sure it has an executor.
For an isolated personal lab the built-in Windows node can be used.
Use a separate agent in shared environments.
The agent must be able to run java, javac, git, docker and kubectl.
A Windows service account often cannot access interactive Docker Desktop.
Run the agent as your desktop user and verify tools from that account.

## 6. Registry and Jenkins credentials
Create a PUBLIC Docker Hub repository named prime-lab under your username.
Generate a Docker Hub read/write access token for pushing images.
The public repository lets Kubernetes pull images directly.
Private registry images require an additional Kubernetes imagePullSecret.

Manage Jenkins > Credentials > System > Global > Add Credentials:
- Username with password. Docker Hub username/token. ID: dockerhub
- Secret file. Flattened Kubernetes config. ID: kubeconfig

Generate the secret file outside the project:

    kubectl --context=docker-desktop config view --raw --minify --flatten | Set-Content -Encoding utf8 "$env:TEMP\prime-kubeconfig.yaml"

Upload the resulting file in Jenkins Credentials. It grants cluster access.
Keep it out of GitHub. The Kubernetes API must be reachable from the agent.
Using a Windows agent on this same host avoids container localhost issues.

## 7. Configure continuous integration
New Item > Prime-CI-CD > Pipeline.
Definition: Pipeline script from SCM. SCM: Git.
Repository URL: your GitHub Prime-DevOps-Lab URL.
Branch: */main. Script Path: Jenkinsfile.
Public repos can be checked out without credentials.
Private repos need a valid GitHub checkout credential.
Save and run Build Now once to load pipeline parameters.
If the first run fails for empty Docker username, use Build with Parameters.
Set DOCKERHUB_USER to your actual Docker Hub username.
Set KUBE_CONTEXT to docker-desktop.
Build and inspect Console Output.

Stages: Checkout > Compile and Test > Build Docker image > Push image >
Deploy to Kubernetes. Every image uses the Jenkins build number.
Keep the same Jenkins job for this project so tags remain meaningful.
Polling checks Git every two minutes after the first run.
Push a new commit and wait for Jenkins to detect it.
Then port-forward the Service using step 4 and inspect the browser result.

## 8. Troubleshooting
- Port busy: stop the earlier server or set PORT to another available port.
- Jenkins waiting: check windows-prime label and executor.
- Docker access denied: verify agent account access to Docker Desktop.
- ImagePullBackOff: check tag, public repository and network.
- Kubernetes unauthorized/unreachable: update kubeconfig and target context.
- Git push rejected: use an empty repo or reconcile its existing history.
- Diagnose Pods: kubectl --context=docker-desktop -n prime-lab describe pods

## Files
src/PrimeNumber.java - algorithm and CLI
src/PrimeNumberTest.java - edge-case tests
src/PrimeServer.java - HTTP server reusing the algorithm
web/index.html - browser form
Dockerfile - multi-stage build and runtime
Jenkinsfile - CI and deployment pipeline
k8s/app.yaml - Namespace, Deployment, Service
run.ps1, .vscode/tasks.json - local run and test helpers
.gitignore, .dockerignore - generated-file exclusions
EXAM_NOTES.pdf - exam-writing notes

## References
Supplied External-lab-reference-SET-1.docx (factorial lab procedure).
https://www.jenkins.io/doc/book/pipeline/syntax/
https://www.jenkins.io/doc/pipeline/steps/credentials-binding/
https://kubernetes.io/docs/tasks/run-application/update-deployment-rolling/

Prime logic checks every divisor from 2 to n - 1. Its worst-case time is O(n), so large prime inputs take longer.
