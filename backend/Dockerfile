# Use OpenJDK 17
FROM eclipse-temurin:17-jdk-alpine

# Set working directory
WORKDIR /app

# Copy only pom.xml
COPY pom.xml ./

# Copy project source
COPY src ./src

# Install Maven
RUN apk add --no-cache maven

# Download dependencies
RUN mvn -q dependency:go-offline

# Build the project
RUN mvn -q clean package -DskipTests

# Run the application
CMD ["java", "-jar", "target/blockchain-supply-chain-1.0.0.jar"]
