ocn2pid service
===============

The purpose of this microservice is to provide a means for mapping Worldcat identifiers (OCNs)
to DBC record identifiers (PIDs).

### Configuration

**Environment variables**

* DB_URL database URL (USER:PASSWORD@HOST:PORT/DBNAME) of the underlying ocn store.

### Development

**Requirements**

To build this project JDK 1.8 and Apache Maven is required.

To start a local instance, docker is required.

**Scripts**
* clean - clears build artifacts
* build - builds artifacts
* test - runs unit and integration tests
* validate - analyzes source code and javadoc
* start - starts localhost instance
* stop - stops localhost instance

```bash
./clean && ./build && ./test && ./validate && DB_URL="..." ./start
```

### License

Copyright Dansk Bibliotekscenter a/s. Licensed under GPLv3.
See license text in LICENSE.txt
