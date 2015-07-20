# PT Anywhere

Web API and widget (e.g., web UI) for [PacketTracer](https://www.netacad.com/web/about-us/cisco-packet-tracer).
Both components are being developed as part of the [FORGE](http://ict-forge.eu/) project.
For more information about _PT Anywhere_, please check [its offical website](http://pt-anywhere.kmi.open.ac.uk).


The Web API uses [Jersey](https://jersey.java.net/documentation/latest/user-guide.html).


## Usage

As this project uses Maven, you can compile and deploy it using the following command:

    mvn clean tomcat7:deploy

By default, the app will be deployed in the development server.
Use any of the following commands to deploy it in other environments:

    mvn clean tomcat7:deploy -Denv=devel  # Development environment (explicitly stated)
    mvn clean tomcat7:deploy -Denv=test  # Testing environment
    mvn clean tomcat7:deploy -Denv=prod  # Production environment


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
