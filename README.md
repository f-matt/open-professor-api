# Install

## Postgresql Driver

```
$ module add --name=org.postgresql --resources=<path>/postgresql-42.7.1.jar --dependencies=javax.api,javax.transaction.api
$ /subsystem=datasources/jdbc-driver=postgresql:add(driver-name="postgresql", driver-module-name="org.postgresql",driver-class-name=org.postgresql.Driver)
```

## Datasource (postgresql)

```
data-source add --jndi-name=java:/openProfessorDS --name=openProfessorDS --connection-url=jdbc:postgresql://localhost:5432/open_professor --driver-name=postgresql --user-name=open_professor --password=<password>
/subsystem=datasources/data-source=openProfessorDS:test-connection-in-pool()
/subsystem=datasources/data-source=openProfessorDS:write-attribute(name=min-pool-size, value=5)
/subsystem=datasources/data-source=openProfessorDS:write-attribute(name=max-pool-size, value=20)
/subsystem=datasources/data-source=openProfessorDS:write-attribute(name=pool-prefill, value=true)
/subsystem=datasources/data-source=openProfessorDS:write-attribute(name=validate-on-match, value=true)
/subsystem=datasources/data-source=openProfessorDS:write-attribute(name=background-validation, value=false)
/subsystem=datasources/data-source=openProfessorDS:write-attribute(name=valid-connection-checker-class-name, value=org.jboss.jca.adapters.jdbc.extensions.postgres.PostgreSQLValidConnectionChecker)
/subsystem=datasources/data-source=sghospDS:write-attribute(name=exception-sorter-class-name, value=org.jboss.jca.adapters.jdbc.extensions.postgres.PostgreSQLExceptionSorter)
```
