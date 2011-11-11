Mambu Java Client
===================

The Mambu Java Client library is an open source library to interact with Mambu's APIs from your java project. 
The library interacts with Mambu's REST API.

Using the original class files from the Mambu project, the library allows your to easily interact via the Mambu APIs to retrieve and store information. 

The library is current under development and is in beta. This means the APIs are not versioned.

Usage
-----
To use the Mambu apis, one just needs to include two jars in your build path:
* Mambu-APIs-Java-1.10-SNAPSHOT-jar-with-dependencies.jar
* mambu-models-v1.10.jar

Then use the factor to create the service and access the methods:

<code>
	MambuAPIService mambu = MambuAPIFactory.crateService("username", "password", "mydomain.mambu.com");
	Client client = mambu.getClient("abc123");
	System.out.println(client.getLastName());
</code>

See Launch.java for a few more examples of using the library

Contributing to the Project
-----
The Mambu Java APIs uses Maven for the build process. To make contributions to the project, one just need to checkout the library and import it into Eclipse (or your favourite Java IDE) as a Maven 2 project.

Ensure to write JUnit tests for all contributions and rerun all existing tests (under /test)

The Mambu team will update the models jar to account for changes in new releases as needed.

