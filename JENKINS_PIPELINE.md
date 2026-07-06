# Jenkins Pipeline Setup

This project includes a `Jenkinsfile` that tests the Spring Boot app and deploys it to one EC2 instance after a pull request is merged into the deployment branch.

## Pipeline Goal

- run tests and coverage checks with `./mvnw clean verify`
- publish the JaCoCo HTML coverage report
- send code quality and coverage data to SonarQube
- wait for the SonarQube quality gate before deployment
- deploy only from the assigned branch, currently `dev`
- copy the Jenkins workspace to EC2 through SSH
- build the Docker image on EC2
- restart the Spring Boot container
- verify `/actuator/health`

## Required Jenkins Plugins

- Pipeline
- Git
- GitHub
- GitHub Branch Source
- Credentials
- SSH Agent
- HTML Publisher
- SonarQube Scanner for Jenkins

## SonarQube Setup

Create a SonarQube server entry in Jenkins:

- Name: `SonarQube`
- Server URL: your SonarQube server URL
- Token credential: a SonarQube user token stored in Jenkins credentials

The name must match the `SONARQUBE_ENV` value in the `Jenkinsfile`.

The Maven build generates the JaCoCo XML report here:

```text
target/site/jacoco/jacoco.xml
```

The `pom.xml` points SonarQube at that report through:

```xml
<sonar.coverage.jacoco.xmlReportPaths>${project.build.directory}/site/jacoco/jacoco.xml</sonar.coverage.jacoco.xmlReportPaths>
```

## JaCoCo Coverage Gate

JaCoCo is configured in `pom.xml` to fail the Maven `verify` phase if instruction coverage is below 90%.

Current local verification:

```text
Tests run: 41, Failures: 0, Errors: 0, Skipped: 0
Instruction coverage: 96.51%
```

## Jenkins Credential

Create one SSH private key credential:

- Kind: `SSH Username with private key`
- ID: `student-management-ec2-key`
- Username: `ec2-user`
- Private key: the EC2 `.pem` private key

Do not commit the private key to GitHub.

## GitHub Integration

Create a Multibranch Pipeline job in Jenkins:

- Repository: `https://github.com/lopsun7/student-management-system`
- Build configuration: `Jenkinsfile`
- Branch include pattern: `*/dev`

Configure the GitHub webhook:

- Payload URL: `http://<JENKINS_HOST>/github-webhook/`
- Content type: `application/json`
- Event: `push`

When a PR is merged into `dev`, GitHub creates a push event on `dev`. The webhook notifies Jenkins, Jenkins checks out the branch, runs tests, and then deploys to EC2.

The deploy step is implemented by:

- [Jenkinsfile](/Users/lopsun/Documents/New project 4/Jenkinsfile:1)
- [scripts/jenkins-deploy-ec2.sh](/Users/lopsun/Documents/New project 4/scripts/jenkins-deploy-ec2.sh:1)

## EC2 Requirements

Install Docker on the EC2 instance:

```bash
sudo dnf update -y
sudo dnf install -y docker
sudo systemctl enable --now docker
```

The EC2 security group should allow:

- SSH `22` from the Jenkins server
- app port `8080` from the demo audience

## Deployment Branch

The current deployment branch is configured in `Jenkinsfile`:

```groovy
DEPLOY_BRANCH = 'dev'
```

To deploy from another branch, change this value and update the Jenkins branch include pattern.

## Runtime Configuration

The pipeline runs the container with:

```bash
SPRING_PROFILES_ACTIVE=h2
DOWNSTREAM_DEFAULT_NAME=Steven
```

This is intended for a low-cost homework demo because it does not require a separate RDS/PostgreSQL instance.
