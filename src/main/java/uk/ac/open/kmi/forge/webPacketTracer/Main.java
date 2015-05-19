package uk.ac.open.kmi.forge.webPacketTracer;

import com.rusticisoftware.tincan.*;
import com.rusticisoftware.tincan.lrsresponses.StatementLRSResponse;
import uk.ac.open.kmi.forge.webPacketTracer.properties.InteractionRecordingProperties;
import uk.ac.open.kmi.forge.webPacketTracer.properties.PropertyFileManager;

import java.net.MalformedURLException;
import java.net.URISyntaxException;


public class Main {

    public static void main(String args[]) throws MalformedURLException, URISyntaxException {
        RemoteLRS lrs = new RemoteLRS();

        final PropertyFileManager pfm = new PropertyFileManager();
        final InteractionRecordingProperties irp = pfm.getInteractionRecordingDetails();
        lrs.setEndpoint(irp.getEndpoint()); //"http://192.168.35.2/data/xAPI/");
        lrs.setVersion(TCAPIVersion.V100);
        lrs.setUsername(irp.getUsername()); //"3a05e4a0ead6d4fd85097d5a2508148c1c7f1abc");
        lrs.setPassword(irp.getPassword()); //"454be7efb061cd2606d3f6ea07e227d8df4fe2ca");

        Agent agent = new Agent();
        agent.setMbox("mailto:info@tincanapi.com");

        Verb verb = new Verb("http://adlnet.gov/expapi/verbs/attempted");

        Activity activity = new Activity("http://rusticisoftware.github.com/TinCanJava");

        Statement st = new Statement();
        st.setActor(agent);
        st.setVerb(verb);
        st.setObject(activity);

        StatementLRSResponse lrsRes = lrs.saveStatement(st);
        if (lrsRes.getSuccess()) {
            // success, use lrsRes.getContent() to get the statement back
            System.out.println("BIEN!");
        } else {
            // failure, error information is available in lrsRes.getErrMsg()
            System.out.println("MAL!");
        }
    }
}