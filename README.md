# Slalom DXM AEM Training

This project is for training purposes and is maintained by the Slalom DXM team.

## Installation Instructions

* Download/Install Java JDK 1.8
	* If you don't have JDK 1.8 installed, go to https://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html and download the respective file for your OS. Installation instructions can be found here https://docs.oracle.com/javase/8/docs/technotes/guides/install/install_overview.html#A1097257

* Download/Install Maven
	* If you don't have maven installed, go to https://maven.apache.org/download.cgi and download either the tar or zip file
	* Follow these instructions for installing maven https://maven.apache.org/install.html

* Download/Install Eclipse
	* For the purposes of this training session, we will be using eclipse. You can, however, use Brackets or other IDEs.
	* https://www.eclipse.org/downloads/
	* The following instructions will help you set up the AEM plugin for Eclipse https://helpx.adobe.com/experience-manager/6-4/sites/developing/using/aem-eclipse.html

* Download/Install AEM 6.4
	* Go to https://bitbucket.org/slalom-consulting/aem-6.4/downloads/ and download all of the files.
	* Place the files in a directory called 'aem' on your machine. We recommend the home directory for Mac users (/Users/kyle.loux)
	* Either double click the file or run the following command 
		java -Xmx1024m -jar aem64-author-p4502.jar -gui


## How to build

To build all the modules run in the project root directory and deploy to a running AEM instance, run 

    mvn clean install -PautoInstallPackage -Padobe-public

Or to deploy only a specific bundle, run

    mvn clean install -PautoInstallBundle



## Modules

The main parts of the template are:

* core: Java bundle containing all core functionality like OSGi services, listeners or schedulers, as well as component-related Java code such as servlets or request filters.
* ui.apps: contains the /apps (and /etc) parts of the project, ie JS&CSS clientlibs, components, templates, runmode specific configs as well as Hobbes-tests
* ui.content: contains sample content using the components from the ui.apps
* ui.tests: Java bundle containing JUnit tests that are executed server-side. This bundle is not to be deployed onto production.
* ui.launcher: contains glue code that deploys the ui.tests bundle (and dependent bundles) to the server and triggers the remote JUnit execution
