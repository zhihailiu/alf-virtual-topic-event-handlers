#!/bin/sh

if [ -z "${M2_HOME}" ]; then
  export MVN_EXEC="mvn"
else
  export MVN_EXEC="${M2_HOME}/bin/mvn"
fi

start() {
    docker-compose up --build -d
}

down() {
    docker-compose down
}

build() {
    docker rmi alf-virtual-topic-event-handlers:development
    $MVN_EXEC clean package
}

tail() {
    docker-compose logs -f
}

start_sample() {
    docker-compose up --build -d alf-virtual-topic-event-handlers
}

stop_sample() {
    docker-compose kill alf-virtual-topic-event-handlers
    yes | docker-compose rm -f alf-virtual-topic-event-handlers
}

case "$1" in
  build_start)
    down
    build
    start
    tail
    ;;
  reload_sample)
    stop_sample
    build
    start_sample
    tail
    ;;
  start)
    start
    tail
    ;;
  stop)
    down
    ;;
  tail)
    tail
    ;;
  *)
    echo "Usage: $0 {build_start|reload_sample|start|stop|tail}"
esac