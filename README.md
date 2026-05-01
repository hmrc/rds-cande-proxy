
# rds-cande-proxy

RDS Cande Proxy repo for EUVAT that connects with RDS Oracle database.

## Developer setup
[Developer setup](https://confluence.tools.tax.service.gov.uk/display/RBD/Local+Machine+Setup+to+run+and+connect+to+Oracle+database)


## Running the service for euvat

- Service Manager for EUVAT: `sm2 --start EUVAT_ALL`

- To check libraries update, run all tests and coverage: `./run_tests.sh`

- To start the server locally: `sbt run` or `sbt 'run 18503'`

- To execute the scala formatter: `./run_fmt_checks.sh`


### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").