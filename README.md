# PT Anywhere API

Web API for [PacketTracer](https://www.netacad.com/web/about-us/cisco-packet-tracer).
This API is being developed as part of the [FORGE](http://ict-forge.eu/) project.
For more information about _PT Anywhere_, please check [its official website](http://pt-anywhere.kmi.open.ac.uk).


If you are you looking for a client-side application which uses this API check [this project](https://github.com/PTAnywhere/ptAnywhere-widgets).


## Usage

As this project uses Maven, you can compile and deploy it using the following command:

    mvn clean exec:exec

By default, the app will be deployed in the development server.
Use any of the following commands to deploy it in other environments:

    mvn clean exec:exec -Denv=devel  # Development environment (explicitly stated)
    mvn clean exec:exec -Denv=test  # Testing environment
    mvn clean exec:exec -Denv=prod  # Production environment

Additionally, you can also define your own environment in a property file.
The easiest way to do this is by using an existing property file as a template (e.g., the development environment one).
If you choose this approach, use the following command:

    mvn exec:exec -Denv=custom -DpropFile=[PATH-TO-YOUR-PROPERTY-FILE]

Please, note that you need to [configure your web server](http://www.mkyong.com/maven/how-to-deploy-maven-based-war-file-to-tomcat/) before using the tomcat deployment command shown above.
If you are using Tomcat6, you might need to [consider this too](http://stackoverflow.com/questions/8726987/cant-access-tomcat-6-manager-app).


## Missing dependencies

If you are reading this section is probably because you cannot compile this project.
This happens because the following dependencies are missing: _tincan_ and _ptipc_.

__Tincan__ is not yet available in Maven repositories.
However, the author claims that: "At some point in the future we [plan to make the maven artifacts available via some publicly available repository](https://github.com/RusticiSoftware/TinCanJava/issues/31)".
Let's cross the fingers.

In the meantime, you can install their latest jar in your local maven repository using the following command:

    mvn install:install-file -Dfile=tincan-0.7.0.jar -DgroupId=com.rusticisoftware -DartifactId=tincan -Dversion=0.7.0 -Dpackaging=jar

__Ptipc__ is a Java library made by Cisco to communicate with their Packet Tracer using IPC (Inter-Process Communication).
The bad news: I do not have the intellectual property about this piece of software and therefore I cannot share it.
Hopefully this will change in the future.
In the meantime, you can contact me to try to sort this problem out together.
