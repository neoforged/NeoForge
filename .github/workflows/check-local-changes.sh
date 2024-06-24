#!/bin/bash

GIT_OUT=$(git status --porcelain ./*/src/generated/resources/*)
echo "$GIT_OUT"
if [[ -z "$GIT_OUT" ]];
then
  exit 1
else
  exit 0
fi
