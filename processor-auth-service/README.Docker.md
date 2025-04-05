### Building and running your application locally with Docker Compose

When you're ready, start your application by running: `docker compose -f dev.compose.yaml up [server] --build [-d|--watch]`.

Your application will be available at http://localhost:8080 (or another port depending on configuration).

### Deploying your application to the cloud

1. First, build your image, e.g.: `docker build -t myregistry.com/restful-app:v1.0.0 -t myregistry.com/restful-app:latest .`.
   - If your cloud uses a different CPU architecture than your development machine (e.g., you are on a Mac M1 and your cloud provider is amd64),
   you'll want to build the image for that platform, e.g.:
   `docker build --platform=linux/amd64 -t myregistry.com/restful-app:v1.0.0 -t myregistry.com/restful-app:latest .`.

2. Then, push it to your registry, e.g. `docker push myregistry.com/restful-app:v1.0.0 myregistry.com/restful-app:latest`
(or push all tags of an image by `docker push -a myregistry.com/restful-app`).

3. Finally, on target host machine, run  `docker compose up -d [server]`. Note that image name and platform may be modified
to match your desire.

This whole process can be configured in a CI pineline.
