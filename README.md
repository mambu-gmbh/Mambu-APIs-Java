Mambu Java Client
===================

The Mambu Java Client library is an open source library to interact with Mambu's APIs from your Java project. 
The library interacts with Mambu's REST APIs.

Using the original class files from the Mambu project, the library allows your to easily interact via the Mambu APIs to retrieve and store information. 

The library is current under development and is in beta. This means the APIs are not versioned.

Usage
-----

To use the Mambu Java API Wrapper, please include the following jars in your build path

* build/Mambu-APIs-Java-3.7-bin.jar
* lib/mambu-models-v3.7.jar
* further dependencies to run and test (see pom.xml for versions)
 * httpclient
 * gson
 * commons-io
 * jdo-api
 * guice
 * mockito-all
 * junit
 * gwt-user

There is a list of services which are provided through a factory.
The list will be updated constantly and currently contains:

- AccountingService
- ClientsService
- DocumentsService
- IntelligenceService
- LoansService
- OrganizationService
- RepaymentsService
- SavingsService
- SearchService
- TasksService
- UsersService

To use the factory, some date must be provided in order to set it up:
	MambuAPIServiceFactory serviceFactory = MambuAPIServiceFactory.getFactory("mydomain.mambu.com", "username", "password");

After this step, each service can be taken through a simple call like:

		ClientService clientService = serviceFactory.getClientService();

See the classes from demo package for a few more examples of using the library

Or check out the javadocs here: http://mambu-gmbh.github.com/Mambu-APIs-Java/

Contributing to the Project
-----
This is a community project and we'd love if you can contribute to make the Mambu API wrapper for Java better.

The project uses Maven for the build process. To make contributions to the project, fork it on GitHub, checkout out the project and import it into Eclipse (or your favourite Java IDE) as a Maven 2 project.

Ensure to write JUnit tests for all contributions and rerun all existing tests (under /test) to ensure a high code quality.

When you're done with your changes, commit and push them to your GitHub fork and create a pull request so that we can review your code and incorporate the changes.

The Mambu team will update the Mambu models jar to account for changes in new releases as needed.

