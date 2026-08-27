#!/bin/bash
# setup.sh

if [ ! -d "run/" ]; then
  echo "No server found, copying from template..."
  cp -r run-template/ run/
fi
