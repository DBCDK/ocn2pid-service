FROM docker.dbc.dk/payara-micro

COPY target/ocn2pid-service-1.0-SNAPSHOT.war wars
COPY target/config /payara-micro/config

ENV ADD_PAYARA_ARGS "--postbootcommandfile /payara-micro/config/add_connections.asadmin"

EXPOSE 4848
EXPOSE 8080
