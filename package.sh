#!/bin/sh

CURRENT_DIR=$PWD
SCRIPT_DIR=$CURRENT_DIR/$(dirname ${BASH_SOURCE:-$0})
OUTPUT_DIR=$SCRIPT_DIR/output

main() {
  mkdir -p $OUTPUT_DIR

  package

  mv $OUTPUT_DIR target
}

package() {
  mvn clean install -DskipTests
  cd eventcloud/eventcloud-bundle
  mvn assembly:single
  mv target/eventcloud* $OUTPUT_DIR
  cd $SCRIPT_DIR
  mvn clean
}

main $@

