package uk.ac.open.kmi.forge.ptAnywhere.api.http.context;

import javax.xml.bind.annotation.XmlRootElement;


// Unsuccessful attempt to generate context file with MOXy.
// I leave it here for documentation purpouses.
//
// In an wrapping class the following does not work:
//   + @XmlElement(name="@context")
//   + @XmlElement(name="\u0040context")
//   + @XmlAttribute(name="@context")  <-- only works with String or types like that, not with POJOs
// The following works but does not fix all the problems:
//   + @XmlElement(name=" @context")  <-- there is only an error when @ is the first character
//   + Without using a wrapper class:
//     - @XmlRootElement(name="@context")
//     - new MoxyJsonConfig().setAttributePrefix("@").setIncludeRoot(true);
//          <- both needed
//          Problem: include root cannot be selectively activated depending on the resource
@XmlRootElement(name="@context")
public class Device {
    String id;
    String label;

    public Device() {
        final String base = "http://schema.org/";
        this.id = base + "url";
        this.label = base + "name";
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}