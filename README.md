# Alfresco Java SDK Event Handler Application as Competing Consumers

The "competing consumers" issue with Alfresco Java SDK Event API can be found [here](https://github.com/Alfresco/alfresco-java-sdk/issues/58).

Let's say I create an application that generates a thumbnail after a PDF document is uploaded. I deploy three instances of the application for high availablity and better performance. Upon a PDF document upload, each of the three instances will receive an event to create thumbnail, while I only need one of them to do that. This is because Alfresco Event API uses ActiveMQ Topic behind the scene.

This sample application demonstrates how to implement competing consumers using ActiveMQ Virutal Topic.

- On Alfresco/ACS side

Use Virtual Topic for the endpoint configuration `repo.event2.topic.endpoint=amqp:topic:VirtualTopic.FOO`. Virtual Topic naming convention is `VirtualTopic.<topic name>`.

- In the application

Switch Topic to Queue in code. Use a configuration to let application(s) listen to the same Queue that the Virtual Topic writes to `alfresco.events.queueName=Consumer.BAR.VirtualTopic.FOO`.

Consumer Queue of the Virtual Topic has the naming convention `Consumer.<consumer name>.VirtualTopic.<topic name>`.

### References
* [How does a Queue compare to a Topic](https://activemq.apache.org/how-does-a-queue-compare-to-a-topic)
* [Virtual Destinations](https://activemq.apache.org/virtual-destinations)
* [Understanding Virtual Destinations in ActiveMQ with an Example](https://itnext.io/understanding-virtual-destinations-in-activemq-with-an-example-cc814e8613d7)
* [How to Use Docker Compose to Run Multiple Instances of a Service in Development](https://pspdfkit.com/blog/2018/how-to-use-docker-compose-to-run-multiple-instances-of-a-service-in-development/)
  
## Usage

### Pre-Requisites

To properly build and run the project in a local environment it is required to have installed some tools.

* Java 17:

* [Maven](https://maven.apache.org/install.html) version 3.3 or higher:


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

```
$ chmod +x run.sh
```
 
See [```run.sh```](run.sh) or [```run.bat```](run.bat) if you would like to know how each function exactly works.

#### How To Run

Build all and run:

```
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
$ ./run.sh reload_app
```

#### How To Test

In run.sh, docker-compose "--scale" option starts two instances of this event handler application. Upload a document in Share and use "docker logs" to verify that only ONE of them captures the event like this:

```
A new node named sample.doc of type cm:content has been created!
```
