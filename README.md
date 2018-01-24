# swagger-vertx-mongo
Starter template for anyone exploring web app stack with Vert.x and MongoDB using Swagger Routing. Sample bidding app to demonstrate end-to-end stack capabilities

# Getting Started
These instructions can get you started on your local machine.

# Install
1. Vert.x 3.5.0
2. Docker
3. Java 1.8

##Compile, Packaging and Deployment
1. Get mongo running on your local using following docker command:
docker run --name vertx-mongo -v ~/mongo-datadir:/data/db -p 27017:27017 -d mongo

2. Package 
mvn clean package

3. Run
java -jar target/job-discovery-api-1.0.0-SNAPSHOT-fat.jar -conf src/main/conf/application-conf.json
