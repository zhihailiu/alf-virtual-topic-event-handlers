#!/bin/sh

APP_NAME="alf-virtual-topic-event-handlers"

if [ -z "${M2_HOME}" ]; then
  export MVN_EXEC="mvn"
else
  export MVN_EXEC="${M2_HOME}/bin/mvn"
fi

start() {
    docker-compose up --build -d --scale $APP_NAME=2
}

down() {
    docker-compose down
}

build() {
    docker rmi $APP_NAME:development
    $MVN_EXEC clean package
}

tail() {
    docker-compose logs -f
}

start_app() {
    docker-compose up --build -d $APP_NAME
}

stop_app() {
    docker-compose kill $APP_NAME
    yes | docker-compose rm -f $APP_NAME
}

case "$1" in
  build_start)
    down
    build
    start
    tail
    ;;
  reload_app)
    stop_app
    build
    start_app
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
    echo "Usage: $0 {build_start|reload_app|start|stop|tail}"
esac
