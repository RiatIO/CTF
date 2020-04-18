#!/usr/bin/bash

artifact="${PWD}/out/artifacts/CTF_jar/CTF.jar"
plugin="${PWD}/server/plugins"

if [ -e "$artifact" ]
then
  mv "$artifact" "$plugin"
fi