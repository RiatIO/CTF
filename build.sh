#!/usr/bin/bash

#plugin="${PWD}/out/artifacts/CTF_jar/CTF.jar"
plugin="${PWD}/target/CTF-1.0-SNAPSHOT.jar"
server="${PWD}/server/plugins"

if [ -e "$plugin" ]
then
  mv "$plugin" "$server"
fi