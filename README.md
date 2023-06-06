# Alfresco Java SDK Event Handler Application as Competing Consumers

The "competing consumers" issue with Alfresco Java SDK Event API is best described [here](https://github.com/Alfresco/alfresco-java-sdk/issues/58) by Jeff Potts.

Let's say I create an application that generates a thumbnail after a PDF document is uploaded. I deploy more than one instance of the application for high availablity and better performance. Upon a PDF document upload, EACH of the applications will receive an event then create thumbnail, while I really just need one of them to do that. The reason is that Alfresco event API is a publish/subscribe model based on ActiveMQ Topic.

This sample application demonstrates how to implement competing consumers using ActiveMQ Virutal Topic. In a nut-shell, it consists of
1. Set Virtual Topic endpoint in Alfresco repository/ACS

```
repo.event2.topic.endpoint=amqp:topic:VirtualTopic.events2
```

2. Set matching queue name in Event API application
```
alfresco.events.queueName=Consumer.FOO.VirtualTopic.events2
```
These names follow ActiveMQ virtual topic and its consumer queue naming conventions.

3. Override Topic with Queue in Event API application
This switches event producer/consumer from Topic (one to many) to Queue (one to one).

## Usage

### Pre-Requisites

To properly build and run the project in a local environment it is required to have installed some tools.

* Java 11:
```bash
$ java -version

openjdk version "11.0.1" 2018-10-16
OpenJDK Runtime Environment 18.9 (build 11.0.1+13)
OpenJDK 64-Bit Server VM 18.9 (build 11.0.1+13, mixed mode)
```

* [Maven](https://maven.apache.org/install.html) version 3.3 or higher:
```bash
$ mvn -version

Apache Maven 3.6.1 (d66c9c0b3152b2e69ee9bac180bb8fcc8e6af555; 2019-04-04T21:00:29+02:00)
```

* [Docker](https://docs.docker.com/install/) version 1.12 or higher:
```bash
$ docker -v

Docker version 20.10.2, build 2291f61
```

* [Docker compose](https://docs.docker.com/compose/install/):
```bash
$ docker-compose -v

docker-compose version 1.27.4, build 40524192
```

### Build and run

This sample project local development environment is based on Docker, so a ```run.sh/run.bat``` utility script has been included in order to build, run or stop 
easily. This script will require execution permissions, so add it if it hasn't.

```bash
$ chmod +x run.sh
```

See [```run.sh```](run.sh) or [```run.bat```](run.bat) if you would like to know how each function exactly works.

#### How To Run

Build all and run:

```bash
$ ./run.sh build_start
```

Only start the environment (without building it):

```bash
$ ./run.sh build_start
```

#### How To Stop

Stop all the containers of the environment:

```bash
$ ./run.sh stop
```

#### How To Reload the Sample App

If you want to rebuild and redeploy *only* the sample app:

```bash
$ ./run.sh reload_sample
```
