# Base image
FROM eclipse-temurin:17-jammy

WORKDIR /usr/src/app

# Update the package repository and install required packages
RUN apt-get update && apt-get install -y unzip zip

# Download the SDKMAN installation script and install it
RUN curl -s "https://get.sdkman.io" | bash
RUN bash -c "source $HOME/.sdkman/bin/sdkman-init.sh && \
  sdk install springboot && \
  sdk install gradle"

# Copy your Gradle project into the image
COPY . .

# Build the project using Gradle Wrapper
RUN ./gradlew build -x test

# Start the application
CMD ["java", "-jar", "./build/libs/logwatch-0.0.1-SNAPSHOT.jar"]
