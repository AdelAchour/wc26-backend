# syntax=docker/dockerfile:1

# ============================================================
# Stage 1: BUILD — uses the full JDK to compile the application
# ============================================================
FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

# Copy Gradle wrapper and build files first.
# Why first? Docker caches each layer. If only source code changes
# but build files don't, this layer is cached and dependencies
# don't need to be re-downloaded. Big speedup.
COPY gradlew gradlew.bat ./
COPY gradle gradle
COPY build.gradle.kts settings.gradle.kts ./
COPY gradle/libs.versions.toml gradle/libs.versions.toml

# Make gradlew executable
RUN chmod +x gradlew

# Download dependencies. This layer is cached unless build files change.
# The `--no-daemon` flag is important in containers — gradle's background
# daemon doesn't make sense in an ephemeral build context.
RUN ./gradlew --no-daemon dependencies || true

# Now copy source code and build
COPY src src

# Build the fat JAR. Shadow's "shadowJar" task is added by the Shadow plugin.
RUN ./gradlew --no-daemon shadowJar

# ============================================================
# Stage 2: RUNTIME — minimal image, just JRE + the built JAR
# ============================================================
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy ONLY the fat JAR from the build stage. The build tools and
# source code stay behind, dramatically shrinking the final image.
COPY --from=build /app/build/libs/*-all.jar app.jar

# Force JVM to UTC. Matches the application.yaml policy.
ENV TZ=UTC
ENV JAVA_TOOL_OPTIONS="-Duser.timezone=UTC"

# The app listens on 8080 by default. EXPOSE is documentation;
# it doesn't actually open the port. Port mapping is done at run time.
EXPOSE 8080

# Run as a non-root user for security.
RUN useradd -r -u 1001 -g root appuser
USER 1001

# Start command
CMD ["java", "-jar", "app.jar"]