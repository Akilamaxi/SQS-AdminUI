# Start with the official Java 17 image as our base
FROM openjdk:17-jdk-slim

# Set the working directory inside the container
WORKDIR /app

# Copy the JAR into the container
COPY ./target/SQS-AdminUI.jar /app/SQS-AdminUI.jar

# Set environment variables (these can be overridden at runtime)
ENV AWS_ACCESS_KEY_ID=1234
ENV AWS_SECRET_KEY=1234
ENV AWS_REGION=ap-southeast-1
ENV SQS_ENDPOINT_URL=http://localhost:4566

# Specify the default command to run the JAR
CMD ["java", "-jar", "/app/SQS-AdminUI.jar"]

# Expose the port the app runs on
EXPOSE 8080
