#!/bin/bash

if [ $# -lt 1 ];
then
  echo "USAGE: $0 [-daemon] db table partitionFrom partitionTo"
  exit 1
fi
base_dir=$(dirname $0)

if [ "x$PARAFLOW_LOG4J_OPTS" = "x" ]; then
  export PARAFLOW_LOG4J_OPTS="-Dlog4j.configuration=file:$$base_dir/../config/log4j.properties"
fi

if [ "x$PARAFLOW_HEAP_OPTS" = "x" ]; then
  export PARAFLOW_HEAP_OPTS="-Xmx1G -Xms1G"
fi

EXTRA_ARGS=${EXTRA_ARGS-'-name paraflowLoader'}

COMMAND=$1
case $COMMAND in
  -daemon)
    EXTRA_ARGS="-daemon "$EXTRA_ARGS
    shift
    ;;
  *)
    ;;
esac

MAIN_CLASS=$1

exec $base_dir/paraflow-run-class.sh cn.edu.ruc.iir.paraflow.loader.LoaderServer "$@"