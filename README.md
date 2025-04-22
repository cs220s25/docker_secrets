
## Overview

In the [`aws_secrets_java` repo](https://github.com/cs220s25/aws_secrets_java) we stored a value in Secrets Manager and retrieved it with Java code.  This repo contains the same code and show you how to access that secret from *inside* a Docker container.

This repo assumes you already set up the secret.  Go do those steps if you have not done so already.

NOTE:  **NEVER** place sensitive information like AWS credentials in a Docker image.  Instead, build the image without the credentials and then provide them when you start the container.

## Docker Build

The repo contains a `Dockerfile` that will build the container image.

```
FROM amazonlinux

WORKDIR /app

RUN yum install -y maven-amazon-corretto21

COPY pom.xml .
COPY src src

RUN mvn package

CMD ["java", "-jar", "target/secretsDemo-1.0.0-jar-with-dependencies.jar"]
```

* Based on the `amazonlinux` container image
* Files stored in `/app`
* Install Maven Corretto21 to ensure compatible Java and Maven versions
* Copy `pom.xml` and `src` folder into `/app`
* Create the package during the image build process to ensure compatibility.
* Launch "as normal" with `java -jar`


Build the image as `secrets`:

```
docker build -t secrets .
```



## AWS Credentials

AWS uses a [credential provider chain](https://docs.aws.amazon.com/sdkref/latest/guide/standardized-credentials.html#credentialProviderChain) to search for credentials.  In the [`aws_secrets_java` repo](https://github.com/cs220s25/aws_secrets_java) we saved credentials in `~/.aws/credentials`, which is one provider in this chain.

With our Dockerized version of this program, the `~/.aws/credentials` file is not available inside the running Docker container.  Therefore we have to use a different credential providers:

* On our laptop (dev), we will use [AWS access keys](https://docs.aws.amazon.com/sdkref/latest/guide/feature-static-credentials.html) stored in environment variables.
* On an EC2 instance (prod), we will use the [Instance Metadata Service (IMDS)](https://docs.aws.amazon.com/sdkref/latest/guide/feature-imds-credentials.html).

## Local Setup

In development, we will provide [AWS access keys](https://docs.aws.amazon.com/sdkref/latest/guide/feature-static-credentials.html) using the environment variables `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`, and `AWS_SESSION_TOKEN`.  These are the same values as in the `~/.aws/credentials` file, but in a different form.


* In the Learner lab, click "AWS Details" and then copy the text in the window below "AWS CLI"

```
[default]
aws_access_key_id=ASIAVBIXLOLWZGSECRET
aws_secret_access_key=gyud18iuUSACCESSKEY
aws_session_token=IQoJb3JpZ2luX2VjECsaCXVzLXdlc3QtMiJHMEUCIFNJRpsBQBxwT+nRg1vX7xAFN7zSmvU/OvW9kbS9M1lFAiEAt3PQREALLY_LONG_TOKEN
```

* Paste this string into a file named `aws.env` in the root of this repo.

* Edit this file to:

  1. Remove the `[default]` line
  2. Change all the variable names to all caps


```
AWS_ACCESS_KEY_ID=ASIAVBIXLOLSECRET
AWS_SECRET_ACCESS_KEY=qxfSybGUtIAeHuOSECRET
AWS_SESSION_TOKEN=IQoJb3JpZ2luX2VjECsaCXVzLXdlc3QtMiJHMEUCIFNJRpsBQBxwT+nRg1vX7xAFN7zSmvU/OvW9kbS9M1lFAiEAt3PQREALLY_LONG_TOKEN
```
  
NOTE:  The `.gitignore` file contains `*.env` to help ensure that this file is not added to a commit.



## Local Execution

Like the original [`aws_secrets_java` repo](https://github.com/cs220s25/aws_secrets_java), the code simply loads the secret named `220_Discord_Token` outputs the value associated with the key `DISCORD_TOKEN`.  After completing this work, the container will terminate (i.e. it is not a web server or a bot server that runs indefinitely).


To run the container use

```
docker run --rm --env-file aws.env secrets
```

* `--rm` - Remove the container when it steps
* `--env-file aws.env` - Read the file `aws.env` and make each line an environment variable

If successful, the output will be the value you saved in AWS Secrets Manager.



## EC2 Deploy


To run this system on an EC2 instance we need to:

* Ensure that the EC2 instance has permission to access Secret Manager
* Install Docker (Java and Maven are not necessary - they are INSIDE the container)
* Clone the repo
* Build the container image


The [Instance Metadata Service (IMDS)](https://docs.aws.amazon.com/sdkref/latest/guide/feature-imds-credentials.html) is a *network-based* system, and it will be available *inside* the docker container.  Therefore, we only need to add the lab role to the EC2 instance at launch.  There is nothing we need to provide when we start the container.

#### Launch the Instance with Permissions to Access Secrets Manager


AWS has a service called [Identity and Access Management (IAM)](https://docs.aws.amazon.com/iam/) that contains the concept of a [*role*](https://docs.aws.amazon.com/IAM/latest/UserGuide/id_roles.html).  We will *attach* the `LabRole` to our EC2 instance to give it permissions to access Secrets Manager.  Once this role is attached, we will not need to save credentials in an `.aws/credentials` file on the EC2 instance.

* To attach the LabRole, when you launch the EC2 instance, go to "Advanced details" section under "IAM instance profile" and select "LabInstanceProfile".


![instance profile](https://i.ibb.co/Y7KH8qbD/f32cc23777f7.png)


#### Install Docker

(These steps are the same as the [dockerized weather app](https://github.com/cs220s25/dockerized_weather_app)

```
sudo yum install -y docker
sudo systemctl enable docker
sudo systemctl start docker
sudo usermod -a -G docker ec2-user
```

Log out and log back in to run `docker` commands as `ec2-user`.



#### Build and Run the Application

After we clone this repo on the EC2 instance , we can build the container image (same as local build):

```
docker build -t secrets .
```

NOTE:  We do **NOT** need to create a `.aws/credentials` file or create a `aws.env` file because the `LabRole` attached to the instance will grant permissions to Secrets Manager.


Now we can run the container:


```
docker run secrets
```

The output of this program will be the value you stored in Secrets Manager.




## References

* [AWS: Understand the credential provider chain](https://docs.aws.amazon.com/sdkref/latest/guide/standardized-credentials.html#credentialProviderChain)
* [Docker Docs: The `env_file` attribute](https://docs.docker.com/compose/how-tos/environment-variables/set-environment-variables/#use-the-env_file-attribute)
