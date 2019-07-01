FROM docker.dbc.dk/payara5-micro:latest

COPY target/ocn2pid-service-1.0-SNAPSHOT.war deployments
COPY target/config/config.json deployments

EXPOSE 8080
