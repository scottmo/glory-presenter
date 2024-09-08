# using the openj9 jvm based docker image for lower memory usage
# see https://community.fly.io/t/deployment-of-java-spring-api-using-dockerfile/6708
FROM ibm-semeru-runtimes:open-17-jre-focal
EXPOSE 8080
COPY target/glory-presenter-*.jar app.jar
ENTRYPOINT ["java", "-jar", "-XX:MaxRAM=70m", "/app.jar"]

# for setting swap in case there's memory issue with java
# CMD if [[ ! -z "$SWAP" ]]; then fallocate -l $(($(stat -f -c "(%a*%s/10)*7" .))) _swapfile && mkswap _swapfile && swapon _swapfile && ls -hla; fi; free -m; java -jar -XX:MaxRAM=70m /app.jar
