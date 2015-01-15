# Web PacketTracer

Web API and widget (e.g., web UI) for [PacketTracer](https://www.netacad.com/web/about-us/cisco-packet-tracer). Both components are being developed as part of the [FORGE](http://ict-forge.eu/) project.


The Web API uses [Jersey](https://jersey.java.net/documentation/latest/user-guide.html).


## Usage

As this project uses Maven, you can compile and deploy it using the following commands:

    mvn clean package
    mvn tomcat7:deploy

Please, note that you need to [configure your web server](http://www.mkyong.com/maven/how-to-deploy-maven-based-war-file-to-tomcat/) before using the tomcat deployment command shown above.
