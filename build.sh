#!/usr/bin/bash

plugin="${PWD}/out/artifacts/CTF_jar/CTF.jar"
server="${PWD}/server/plugins"

if [ -e "$plugin" ]
then
  mv "$plugin" "$server"
fi