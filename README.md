Mambu Java Client
===================

The Mambu Java Client library is an open source library to interact with Mambu's APIs from your java project. 
The library interacts with Mambu's REST APIs.

Using the original class files from the Mambu project, the library allows your to easily interact via the Mambu APIs to retrieve and store information. 

The library is current under development and is in beta. This means the APIs are not versioned.

Usage
-----

To use the Mambu apis, just include the following two jars in your build path (available under /lib)

* Mambu-APIs-Java-3.0-SNAPSHOT-jar-with-dependencies.jar
* mambu-models-v3.1.jar

There is a list of services which are provided through a factory.
The list will be updated constantly and currently contains:

- AccountingService
- ClientService
- IntelligenceService
- LoanService
- OrganizationService
- RepaymentsService
- SavingsService
- UsersService

To use the factory, some date must be provided in order to set it up:

	MambuAPIFactory.setUp("mydomain.mambu.com", "username", "password");
	 
After this step, each service can be taken through a simple call like:

		ClientService clientService = MambuAPIFactory.getClientService();

See the classes from demo package for a few more examples of using the library

Or check out the javadocs here: http://mambu-gmbh.github.com/Mambu-APIs-Java/

Contributing to the Project
-----
The Mambu Java APIs uses Maven for the build process. To make contributions to the project, one just need to checkout the library and import it into Eclipse (or your favourite Java IDE) as a Maven 2 project.

Ensure to write JUnit tests for all contributions and rerun all existing tests (under /test)

The Mambu team will update the models jar to account for changes in new releases as needed.

