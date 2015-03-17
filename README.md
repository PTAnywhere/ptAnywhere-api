# Web PacketTracer

Web API and widget (e.g., web UI) for [PacketTracer](https://www.netacad.com/web/about-us/cisco-packet-tracer). Both components are being developed as part of the [FORGE](http://ict-forge.eu/) project.


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
