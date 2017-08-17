FROM docker.dbc.dk/payara-micro

COPY target/ocn2pid-service-1.0-SNAPSHOT.war wars
COPY target/config /payara-micro/config.d

EXPOSE 4848
EXPOSE 8080
