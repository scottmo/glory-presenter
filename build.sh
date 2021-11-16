#!/usr/bin/env bash

type="exe"
if [ `uname` == "Darwin" ]; then
    type="dmg"
fi

mvn install
jpackage --input target/ \
    --name glory-presenter \
    --main-jar worship-service-tool-0.0.1-SNAPSHOT.jar \
    --main-class com.scottscmo.Application \
    --type $type \
    --java-options '--enable-preview'
