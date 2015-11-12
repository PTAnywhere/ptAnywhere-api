package uk.ac.open.kmi.forge.ptAnywhere.session.management;

import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement
public class File {
    String url;
    String filename;

    public File() {
    }

    public File(String url, String fname) {
        this.url = url;
        this.filename = fname;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
