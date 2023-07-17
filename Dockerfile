FROM eclipse-temurin:17-jammy

WORKDIR /usr/src/app

RUN apt-get update && apt-get install -y unzip zip

RUN curl -s "https://get.sdkman.io" | bash
RUN bash -c "source $HOME/.sdkman/bin/sdkman-init.sh && \
  sdk install springboot && \
  sdk install gradle"

COPY . .

# Build using Gradle Wrapper
RUN ./gradlew build -x test

# Start the application
CMD ["java", "-jar", "./build/libs/logwatch-0.0.1-SNAPSHOT.jar"]
