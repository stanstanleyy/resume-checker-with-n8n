# 🐳 Docker Basic Cheat Sheet

Simple and essential Docker commands + Spring Boot workflow.

---

## 📦 Images (Build & Manage)

```bash
docker build -t <app-name> .
docker images
docker rmi my-app


# Container (Run and Control)

docker run -d -p 8080:8080 --name app my-app
docker ps --> to see all running container
docker ps -a --> to see all containers including the exited or crashed ones
docker stop app
docker start app
docker rm <container-id-or-name> --> to remove the container


```

## Push & Pull (Docker Hub)

docker login
docker tag my-app username/my-app:latest
docker push username/my-app:latest
docker pull username/my-app:latest

## Logs & Access

docker logs app
docker logs -f app
docker exec -it app sh

## Clean up

docker system prune -a

## My PROCESS

## Basic Docker Guide For Noob like Stanley

**THINGS TO KNOW FIRST**

1. Image
   - Can Only be Created Using a Dockerfile
   - It is the blueprint of every containers
   - you can create multiple images commits in DockerHub, where each images commits can be pull and creates a container from.

   CONTAINS:
   - Build File or Compiled Output (.jar, .war, /dist)
   - Runtime Environment (Nod)

2. Container
   - It is an instance of an Image.
   - It is a runnable software containing every requirements of a single project to run.
   -


