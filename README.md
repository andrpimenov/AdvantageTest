Prerequisites to run tests from command line:
- JDK 11+
- maven

Run tests:
mvn clean install -Denv=<env>
    just one environment is configured - "local", so command should be following:

mvn clean install -Denv=local <b>or just</b> mvn clean install