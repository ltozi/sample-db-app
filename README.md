# Spring boot demo app

A simple app in Spring that can be used to test connectivity to database from within cloud resources and to test resilience
when during a database upgrade for example.


- that connects to a database:
- creates a demo table `example_records` at the startup (if does not exist)
- read record from that table at fixed rate
- write a record to table at fixed rate when /api/toggle-writing is invoked (default is always disabled and reset to disabled when restarted)


## Run app

```shell
mvn spring-boot:run
```

## Run app with custom properties

```shell
mvn spring-boot:run -Dspring-boot.run.profiles=rds
```


## Toggle write to demo table

```shell
curl -X POST http://localhost:9191/api/toggle-writing
```


### Drop example_records when finished

You can drop the table by simply running the app with this attribute set to true in application properties.

```properties
app.drop-table-and-exit=true
```