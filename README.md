# Alfresco Java SDK Event Handler Application as Competing Consumers

The "competing consumers" issue with Alfresco Java SDK Event API can be found [here](https://github.com/Alfresco/alfresco-java-sdk/issues/58).

Let's say I create an application that generates a thumbnail after a PDF document is uploaded. I deploy three instances of the application for high availablity and better performance. Upon a PDF document upload, each of the three instances will receive an event to create thumbnail, while I only need one of them to do that. This is because Alfresco Event API uses ActiveMQ Topic behind the scene.

This sample application demonstrates how to implement competing consumers using ActiveMQ Virutal Topic.
1. On Alfresco/ACS side

Use Virtual Topic for the endpoint configuration
```
repo.event2.topic.endpoint=amqp:topic:VirtualTopic.FOO
```
Virtual Topic naming convention is 
```
VirtualTopic.<topic name>
```

2. In the application

Switch Topic to Queue in code. Use a configuration to let application(s) listen to the same Queue that the Virtual Topic writes to.
```
alfresco.events.queueName=Consumer.BAR.VirtualTopic.FOO
```
Consumer Queue of the Virtual Topic has the naming convention 
```
Consumer.<consumer name>.VirtualTopic.<topic name>
```

### References
* [How does a Queue compare to a Topic](https://activemq.apache.org/how-does-a-queue-compare-to-a-topic)
* [Virtual Destinations](https://activemq.apache.org/virtual-destinations)
* [Understanding Virtual Destinations in ActiveMQ with an Example](https://itnext.io/understanding-virtual-destinations-in-activemq-with-an-example-cc814e8613d7)
* [How to Use Docker Compose to Run Multiple Instances of a Service in Development](https://pspdfkit.com/blog/2018/how-to-use-docker-compose-to-run-multiple-instances-of-a-service-in-development/)
  
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
