#!/usr/bin/env bash

appName="glory-presenter"
jarName="worship-service-tool-0.0.1-SNAPSHOT.jar"
mainClass="com.scottscmo.Application"
imageName="custom-jdk"

type="exe"
if [ `uname` == "Darwin" ]; then
    type="dmg"
fi

makeCustomJDK() {
    jlink --output $imageName \
        --add-modules java.desktop \
        --strip-debug --strip-native-commands --compress 2 --no-header-files --no-man-pages
}

package() {
    jpackage --input ../target/ \
        --name $appName \
        --main-jar $jarName \
        --main-class $mainClass \
        --type $type \
        --java-options '--enable-preview' \
        --runtime-image $imageName
}

mkdir -p build

if [ "$0" == "jdk" ]; then
    cd build
    makeCustomJDK
    cd ..
else
    mvn install
    cd build
    package
    cd ..
fi
