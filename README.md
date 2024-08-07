# Data Mapping Access Objects Generator (DMAOG)
[![Main build](https://github.com/herminiogg/dmaog/actions/workflows/scala.yml/badge.svg?branch=main)](https://github.com/herminiogg/dmaog/actions/workflows/scala.yml?query=branch%3Amain)
[![Maven Central](https://img.shields.io/maven-central/v/com.herminiogarcia/dmaog_3?color=blue)](https://central.sonatype.com/artifact/com.herminiogarcia/dmaog_3)
[![DOI](https://zenodo.org/badge/DOI/10.5281/zenodo.12782289.svg)](https://doi.org/10.5281/zenodo.12782289)
[![SWH](https://archive.softwareheritage.org/badge/origin/https://github.com/herminiogg/dmaog/)](https://archive.softwareheritage.org/browse/origin/?origin_url=https://github.com/herminiogg/dmaog)

DMAOG is a library that allows to use data mapping rules to automatically generate code to access
the data generated from these mapping rules. In this way a developer only has to write the mapping
rules to generate a Knowledge Graph, then DMAOG will apply the mapping rules and generate the data access code 
necessary to query them. Additionally, DMAOG can take care of the data changes – and reapplying mapping rules – as a background task.

# How it works
DMAOG is able to run mapping rules in ShExML and RML. After applying the mapping rules the data is explored through
SPARQL queries in order to get the details of each type. It is important to remark that every desired data object to be
generated must contain a type declaration in the form of `:subject a :type`. From the analysis DMAOG builds a set of
Java classes that compounds the Data Access layer for the data generated by the mapping rules. This layer can be
used in any Java-based project that uses DMAOG as a dependency. When accessing the data, DMAOG relies on reflection
techniques to create and populate the objects that are then returned to the user through the created data access layer.

# How to start
The DMAOG library is composed of two different entry points that are intended for the two steps described earlier: 
code generation and data access. The CLI is intended for the code generation from the command line in an easy and 
consistent way. Data access layer is used by the generated code to effectively encapsulate data query.

As an example we can use the mapping rules hosted in this project under the films.shexml file to generate the data access layer with the following command
(see the CLI section to get further details about the configuration possibilities):
```
$ java -jar dmaog.jar -m films.shexml -o . -p com.herminiogarcia.dmaog
```
This command will create the following files: Actor.java, Film.java, ActorService.java, FilmService.java and DataAccessSingleton.java. 
Inside DataAccessSingleton.java all the configuration parameters can be set up. Then the services can be directly used, for example:
`new FilmService().getAll();` and the methods will return the results encapsulated in the generated DTO objects. To see all the methods supported by the services take a look to the "Supported methos in services" 
section.

## CLI
```
Usage: dmaog [-hV] [--static] [-d=<datafile>] [-dr=<drivers>]
             [-m=<mappingRules>] [-ml=<mappingLanguage>] -o=<outputPath>
             -p=<packageName> [-ps=<password>] [-se=<sparqlEndpoint>]
             [-sep=<sparqlEndpointPassword>] [-seu=<sparqlEndpointUsername>]
             [-sl=<sparqlQueryLimit>] [-u=<username>]
Generate data access objects and services from your mapping rules.
  -d, --datafile=<datafile>
                  Path where the datafile is located if no mapping rules are
                    provided.
      -dr, --drivers=<drivers>
                  Drivers string in case it is not included in ShExML
  -h, --help      Show this help message and exit.
  -m, --mapping=<mappingRules>
                  Path to the file with the mappings
      -ml, --mappingLanguage=<mappingLanguage>
                  Mapping language to use: ShExML or RML
  -o, --output=<outputPath>
                  Path where to generate the output files
  -p, --package=<packageName>
                  Package information for the generated files
      -ps, --password=<password>
                  Password in case of database connection
      -se, --sparqlEndpoint=<sparqlEndpoint>
                  URL pointing to the SPARQL endpoint
      -sep, --sparqlEndpointPassword=<sparqlEndpointPassword>
                  Password for the SPARQL endpoint
      -seu, --sparqlEndpointUsername=<sparqlEndpointUsername>
                  Username for the SPARQL endpoint
      -sl, --sparqlQueryLimit=<sparqlQueryLimit>
                  Limit the amount of requested results by adding a LIMIT x
                    statement to the queries. This could limit performance
                    issues when dealing with big graphs

      --static    Exploit mapping rules without executing them
  -u, --username=<username>
                  Username in case of database connection
  -V, --version   Print version information and exit.

```

## Supported features
* Generation of data against files and SPARQL endpoints
* Using already existing data files and SPARQL endpoints without mapping rules
* Update actions on files and SPARQL endpoints (authentication included)
* Static analysis of ShExML rules (not need to run the mapping rules to generate classes)
* Multilingual strings
* Pagination of the results

## Supported methods in services
* getAll(): List[Type] -> Returns all the results for the type
* getAll(Long limit, Long offset): List[Type] -> Returns all the results for the type within the given page
* getAll(String rdfFormat): String -> Returns all the results in the requested format
* count(): Long -> Returns the total number of objects of this type
* getById(String id): Type -> Return the object with the given id for the type. Take into account that the id is refering
to the local part of the subject URI when talking about RDF data.
* getById(String id, String rdfFormat): String  -> Return the object with the given id in the requested format.
* getByField(String fieldName, String value): List<Type> -> Return all the results whose indicated field value matches with
the given value. Take into account that the fieldName refers to the localPart of the predicate URI when talking about RDF data.
* getByField(String fieldName, String value, String rdfFormat): String -> Return all the results whose indicated field 
value matches with the given value in the requested format.
* commit(Type instance) -> Deletes the instance (if it exists) in the data store and inserts the new data. It can act 
as create or update method.
* delete(Type instance) -> Deletes the instance in the data store.

## Requirements
The minimal versions for this software to work are:
- JDK 8 (or the Open JDK 8)
- Scala 2.12.17
- SBT 1.7.2

## Webpage
A live playground is also offered online (https://dmaog.herminiogarcia.com). However, due to hardware limitations it is not
intended for intensive use.

## Build
The library uses sbt as the package manager and building tool, therefore to compile the project you can use the following command:
```
$ sbt compile
```
To run the project from within sbt you can use the command below, where `<options>` can be replaced by the arguments explained in the [CLI](#cli)
```
$ sbt "run <options>"
```
To generate an executable JAR file you can call the following command. Take into account that if you want to test the library before
generating the artifact you need to set up the testing environment as explained in the [Testing](#testing) section and omit
the `"set test in assembly := {}"` option from the command.
```
$ sbt "set test in assembly := {}" clean update assembly
```
## Testing
The project contains a full suite of tests that checks that all the features included in the engine work as expected. These
tests units are included under the src/test/scala folder. To run them you can use the command below. Notice that it is of utmost
importance to test that the project pass the tests for all the cross-compiled versions used within the project
(see the [Cross-compilation](#cross-compilation) section for more details.)
```
$ sbt test
```
The test environment uses some external resources that need to be set up before running them which mainly involves starting and configuring
a triple store. This process is described on the 
[Github workflow file](https://github.com/herminiogg/dmaog/blob/master/.github/workflows/scala.yml).

## Cross-compilation
The project is enabled to work with three different versions of Scala (i.e., 2.12.x, 2.13.x and 3.x) so it can be used across different
Scala environments. Therefore, all the commands will work by default with the 3.x version but it is possible to run the same command
for all the versions at the same time or just for one specific version. Below you can see how to do so with the test command.

Testing against all the cross-compiled versions:
```
$ sbt "+ test"
```

Testing against a specific version where <version> is one of the configured versions in the build.sbt file:
```
$ sbt "++<version> test"
```

## Dependencies
The following dependencies are used by this library:

| Dependency                                   | License                                 |
|----------------------------------------------|-----------------------------------------|
| org.apache.jena / jena-base                  | Apache License 2.0                      |
| org.apache.jena / jena-core                  | Apache License 2.0                      |
| org.apache.jena / jena-arq                   | Apache License 2.0                      |
| info.picocli / picocli                       | Apache License 2.0                      |
| be.ugent.rml / rmlmapper                     | MIT License                             |
| com.herminiogarcia / shexml                  | MIT License                             |
| com.typesafe.scala-logging / scala-logging   | Eclipse Public License v1.0 or LGPL-2.1 |
| ch.qos.logback / logback-classic             | Eclipse Public License v1.0 or LGPL-2.1 |

For performing a more exhaustive licenses check, including subdependecies and testing ones the
[sbt-license-report](https://github.com/sbt/sbt-license-report) plugin is included in the project, enabling the generation
of a report with the command:
```
$ sbt dumpLicenseReport
```
The results are available, after the execution of this command, under the directory `target/license-reports`.

# Future work
* Handling Blank Nodes
* Possibility to use Shapes for code generation
* Static analysis of RML rules

