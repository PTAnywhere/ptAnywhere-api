package uk.ac.open.kmi.forge.ptAnywhere.analytics;


import com.rusticisoftware.tincan.Statement;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.skyscreamer.jsonassert.JSONAssert;
import uk.ac.open.kmi.forge.ptAnywhere.analytics.vocab.BaseVocabulary;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import java.net.MalformedURLException;


public class TinCanAPITest {

    final static String WIDGETURI = "http://testuri/";
    final static String SESSIONID = "b8d5exozT9eNsR1udGjbZQ--";
    final static String SESSIONUUID = "6fc7797b-1a33-4fd7-8db1-1d6e7468db65";
    final static String DEVICE1URI = "http://device1";
    final static String DEVICE2URI = "http://device2";
    final static String DEVICE1NAME = "Device One";
    final static String DEVICE2NAME = "Device Two";
    final static String DEVICETYPE = "router";
    final static String DEVICEGW = "192.168.1.1";
    final static String PORTURI = "http://port1";
    final static String PORTNAME = "Port One";
    final static String PORTIPADDR = "192.168.1.3";
    final static String PORTSUBNETMASK = "255.255.255.0";
    final static String LINKURI = "http://coolLink1234";
    final static String PORT1NAME = "port1";
    final static String PORT2NAME = "port2";
    final static String COMMANDLINE_TEXT = "ping 127.0.0.1";

    TestableTinCanAPI testable;

    @Before
    public void setUp() throws MalformedURLException {
        this.testable = new TestableTinCanAPI();
        this.testable.setURIFactory(new URIFactory(WIDGETURI));
        this.testable.setSession(SESSIONID);
    }

    protected String getJson(String field, String valueInJson) {
        return "{\"" + field + "\":" + valueInJson + "}";
    }

    protected void assertContains(String field, String expectedJsonInField, String gotJson) throws JSONException {
        JSONAssert.assertEquals(getJson(field, expectedJsonInField), gotJson, false);
    }

    protected String getExpectedActor() {
        return "{\"objectType\":\"Agent\",\"account\":" +
                "{\"homePage\":\"http://forge.kmi.open.ac.uk/pt/widget\",}}";
    }

    protected String getExpectedVerb(String verb) {
        return "{\"id\":\"" + verb + "\"}";
    }

    protected String getExpectedContext() {
        return getJson("registration",  "\"" + SESSIONUUID + "\"");
    }

    protected String getExpectedContext(String parentActivity) {
        return this.getExpectedContext(parentActivity, BaseVocabulary.SIMULATION);
    }

    protected String getExpectedContext(String parentActivity, String parentType) {
        final String generalContext = getExpectedContext();
        return generalContext.substring(0, generalContext.length()-1) + // To open closed {}
                ",\"contextActivities\":{\"parent\":[{\"objectType\":\"Activity\",\"id\":\"" + parentActivity +
                "\",\"definition\":{\"type\":\"" + parentType + "\"}}]}}";
    }

    protected String getExpectedActivity(String id, String type) {
        return getExpectedActivity(id, type, null);
    }

    protected String getExpectedActivity(String id, String type, String enDefinition) {
        final String extraChunk = (enDefinition==null)? "" : ", \"name\": {\"en-GB\":\"" + enDefinition + "\"}";
        return "{\"objectType\":\"Activity\",\"id\":\"" + id +
                "\",\"definition\":{\"type\":\"" + type + "\"" + extraChunk + "}}";
    }


    protected void assertNotContains(String field, String gotJson) throws JSONException {
        assertFalse(gotJson.contains("\"" + field + "\":{"));
    }

    @Test
    public void testInteractionStarted() throws JSONException {
        this.testable.interactionStarted();
        final String jsonGenerated = this.testable.statementToRecord.toJSON();
        assertContains("actor", getExpectedActor(), jsonGenerated);
        assertContains("verb", getExpectedVerb(BaseVocabulary.INITIALIZED), jsonGenerated);
        assertContains("object", getExpectedActivity(WIDGETURI, BaseVocabulary.SIMULATION), jsonGenerated);
        assertContains("context", getExpectedContext(), jsonGenerated);
        assertNotContains("result", jsonGenerated);
    }

    protected String getExtension(String extUri, String extValue) {
        // Quick check to detect if the value is not already a JSON...
        if (!extValue.startsWith("[") && !extValue.startsWith("{") && !extValue.startsWith("\"") ) {
            extValue = "\"" + extValue + "\"";
        }
        return "\"" + extUri + "\":" + extValue;
    }

    protected String getExpectedResult(String response, String[]... extensions) {
        String ret = "{\"response\":\"" + response + "\"";
        if (extensions.length>0) {
            ret += ",\"extensions\":{";
            for( String[] extension: extensions) {
                ret += getExtension(extension[0], extension[1]) + ",";
            }
            ret = ret.substring(0, ret.length()-1) + "}";
        }
        return ret + "}";
    }

    protected String toPositionJson(double x, double y) {
        final JsonObjectBuilder endpoint1 = Json.createObjectBuilder().
                add(BaseVocabulary.EXT_POSITION_X, x).
                add(BaseVocabulary.EXT_POSITION_Y, y);
        return endpoint1.build().toString();
    }

    @Test
    public void testDeviceCreated() throws JSONException {
        this.testable.deviceCreated(DEVICE1URI, DEVICE1NAME, DEVICETYPE, 44, 66);
        final String jsonGenerated = this.testable.statementToRecord.toJSON();
        assertContains("actor", getExpectedActor(), jsonGenerated);
        assertContains("verb", getExpectedVerb(BaseVocabulary.CREATED), jsonGenerated);
        assertContains("object", getExpectedActivity(BaseVocabulary.SIMULATED_DEVICE + "/" + DEVICETYPE, BaseVocabulary.SIMULATION, "Simulated router"), jsonGenerated);
        assertContains("context", getExpectedContext(WIDGETURI), jsonGenerated);
        final String[][] exts = new String[][] {
                {BaseVocabulary.EXT_DEVICE_NAME, DEVICE1NAME},
                {BaseVocabulary.EXT_DEVICE_URI, DEVICE1URI},
                {BaseVocabulary.EXT_DEVICE_TYPE, DEVICETYPE},
                {BaseVocabulary.EXT_DEVICE_POSITION, toPositionJson(44, 66)}
        };
        assertContains("result", getExpectedResult(DEVICE1NAME, exts), jsonGenerated);
    }

    @Test
    public void testDeviceDeleted() throws JSONException {
        this.testable.deviceDeleted(DEVICE1URI, DEVICE1NAME, DEVICETYPE);
        final String jsonGenerated = this.testable.statementToRecord.toJSON();
        assertContains("actor", getExpectedActor(), jsonGenerated);
        assertContains("verb", getExpectedVerb(BaseVocabulary.DELETED), jsonGenerated);
        assertContains("object", getExpectedActivity(BaseVocabulary.SIMULATED_DEVICE + "/" + DEVICETYPE, BaseVocabulary.SIMULATION, "Simulated router"), jsonGenerated);
        assertContains("context", getExpectedContext(WIDGETURI), jsonGenerated);
        final String[][] exts = new String[][] {
                {BaseVocabulary.EXT_DEVICE_NAME, DEVICE1NAME},
                {BaseVocabulary.EXT_DEVICE_URI, DEVICE1URI},
                {BaseVocabulary.EXT_DEVICE_TYPE, DEVICETYPE}
        };
        assertContains("result", getExpectedResult(DEVICE1NAME, exts), jsonGenerated);
    }

    @Test
    public void testDeviceModified() throws JSONException {
        this.testable.deviceModified(DEVICE1URI, DEVICE1NAME, DEVICETYPE);
        final String jsonGenerated = this.testable.statementToRecord.toJSON();
        assertContains("actor", getExpectedActor(), jsonGenerated);
        assertContains("verb", getExpectedVerb(BaseVocabulary.UPDATED), jsonGenerated);
        assertContains("object", getExpectedActivity(BaseVocabulary.SIMULATED_DEVICE + "/" + DEVICETYPE, BaseVocabulary.SIMULATION, "Simulated router"), jsonGenerated);
        assertContains("context", getExpectedContext(WIDGETURI), jsonGenerated);
        final String[][] exts = new String[][] {
                {BaseVocabulary.EXT_DEVICE_NAME, DEVICE1NAME},
                {BaseVocabulary.EXT_DEVICE_URI, DEVICE1URI},
                {BaseVocabulary.EXT_DEVICE_TYPE, DEVICETYPE}
        };
        assertContains("result", getExpectedResult(DEVICE1NAME, exts), jsonGenerated);
    }

    @Test
    public void testDeviceModifiedWithDefaultGateway() throws JSONException {
        this.testable.deviceModified(DEVICE1URI, DEVICE1NAME, DEVICETYPE, DEVICEGW);
        final String jsonGenerated = this.testable.statementToRecord.toJSON();
        assertContains("actor", getExpectedActor(), jsonGenerated);
        assertContains("verb", getExpectedVerb(BaseVocabulary.UPDATED), jsonGenerated);
        assertContains("object", getExpectedActivity(BaseVocabulary.SIMULATED_DEVICE + "/" + DEVICETYPE, BaseVocabulary.SIMULATION, "Simulated router"), jsonGenerated);
        assertContains("context", getExpectedContext(WIDGETURI), jsonGenerated);
        final String[][] exts = new String[][] {
                {BaseVocabulary.EXT_DEVICE_NAME, DEVICE1NAME},
                {BaseVocabulary.EXT_DEVICE_URI, DEVICE1URI},
                {BaseVocabulary.EXT_DEVICE_TYPE, DEVICETYPE},
                {BaseVocabulary.EXT_DEVICE_GW, DEVICEGW}
        };
        assertContains("result", getExpectedResult(DEVICE1NAME, exts), jsonGenerated);
    }

    @Test
    public void testPortModified() throws JSONException {
        final String portActivityId = WIDGETURI + "device/"  + DEVICE1NAME.hashCode() + "/port/Port%20One";
        this.testable.portModified(PORTURI, DEVICE1NAME, PORTNAME, PORTIPADDR, PORTSUBNETMASK);
        final String jsonGenerated = this.testable.statementToRecord.toJSON();
        assertContains("actor", getExpectedActor(), jsonGenerated);
        assertContains("verb", getExpectedVerb(BaseVocabulary.UPDATED), jsonGenerated);
        assertContains("object", getExpectedActivity(portActivityId, BaseVocabulary.SIMULATED_PORT, "Device One's port Port One"), jsonGenerated);
        assertContains("context", getExpectedContext(WIDGETURI), jsonGenerated);
        final String[][] exts = new String[][] {
                {BaseVocabulary.EXT_PORT_URI, PORTURI},
                {BaseVocabulary.EXT_DEVICE_NAME, DEVICE1NAME},
                {BaseVocabulary.EXT_PORT_NAME, PORTNAME},
                {BaseVocabulary.EXT_PORT_IP_ADDR, PORTIPADDR},
                {BaseVocabulary.EXT_PORT_SUBNET_MASK, PORTSUBNETMASK}
        };
        assertContains("result", getExpectedResult(PORTNAME, exts), jsonGenerated);
    }

    protected String toEndpointJson(String name1, String port1, String name2, String port2) {
        final JsonObjectBuilder endpoint1 = Json.createObjectBuilder().
                add(BaseVocabulary.EXT_ENDPOINT_DEVICE, name1).
                add(BaseVocabulary.EXT_ENDPOINT_PORT, port1);
        final JsonObjectBuilder endpoint2 = Json.createObjectBuilder().
                add(BaseVocabulary.EXT_ENDPOINT_DEVICE, name2).
                add(BaseVocabulary.EXT_ENDPOINT_PORT, port2);
        final JsonArrayBuilder array = Json.createArrayBuilder().add(endpoint1).add(endpoint2);
        return array.build().toString();
    }

    @Test
    public void deviceConnected() throws JSONException {
        this.testable.deviceConnected(LINKURI, DEVICE1NAME, PORT1NAME, DEVICE2URI, PORT2NAME);
        final String jsonGenerated = this.testable.statementToRecord.toJSON();
        assertContains("actor", getExpectedActor(), jsonGenerated);
        assertContains("verb", getExpectedVerb(BaseVocabulary.CREATED), jsonGenerated);
        assertContains("object", getExpectedActivity(BaseVocabulary.SIMULATED_LINK, BaseVocabulary.SIMULATION, "Link"), jsonGenerated);
        assertContains("context", getExpectedContext(WIDGETURI), jsonGenerated);
        final String[][] exts = new String[][] {
                {BaseVocabulary.EXT_ENDPOINTS, toEndpointJson(DEVICE1NAME, PORT1NAME, DEVICE2URI, PORT2NAME)},
                {BaseVocabulary.EXT_LINK_URI, LINKURI}
        };
        assertContains("result", getExpectedResult(LINKURI, exts), jsonGenerated);
    }

    @Test
    public void deviceDisconnected() throws JSONException {
        this.testable.deviceDisconnected(LINKURI, DEVICE1NAME, PORT1NAME, DEVICE2URI, PORT2NAME);
        final String jsonGenerated = this.testable.statementToRecord.toJSON();
        assertContains("actor", getExpectedActor(), jsonGenerated);
        assertContains("verb", getExpectedVerb(BaseVocabulary.DELETED), jsonGenerated);
        assertContains("object", getExpectedActivity(BaseVocabulary.SIMULATED_LINK, BaseVocabulary.SIMULATION, "Link"), jsonGenerated);
        assertContains("context", getExpectedContext(WIDGETURI), jsonGenerated);
        final String[][] exts = new String[][] {
                {BaseVocabulary.EXT_ENDPOINTS, toEndpointJson(DEVICE1NAME, PORT1NAME, DEVICE2URI, PORT2NAME)},
                {BaseVocabulary.EXT_LINK_URI, LINKURI}
        };
        assertContains("result", getExpectedResult(LINKURI, exts), jsonGenerated);
    }

    @Test
    public void commandLineStarted() throws JSONException {
        final String consoleActivityId = WIDGETURI + "device/"  + DEVICE1NAME.hashCode() + "/console";
        this.testable.commandLineStarted(DEVICE1NAME);
        final String jsonGenerated = this.testable.statementToRecord.toJSON();
        assertContains("actor", getExpectedActor(), jsonGenerated);
        assertContains("verb", getExpectedVerb(BaseVocabulary.INITIALIZED), jsonGenerated);
        assertContains("object", getExpectedActivity(consoleActivityId, BaseVocabulary.COMMAND_LINE, DEVICE1NAME + "'s command line"), jsonGenerated);
        assertContains("context", getExpectedContext(WIDGETURI), jsonGenerated);
        assertNotContains("result", jsonGenerated);
    }

    @Test
    public void commandLineUsed() throws JSONException {
        final String consoleActivityId = WIDGETURI + "device/"  + DEVICE1NAME.hashCode() + "/console";
        this.testable.commandLineUsed(DEVICE1NAME, COMMANDLINE_TEXT);
        final String jsonGenerated = this.testable.statementToRecord.toJSON();
        assertContains("actor", getExpectedActor(), jsonGenerated);
        assertContains("verb", getExpectedVerb(BaseVocabulary.USED), jsonGenerated);
        assertContains("object", getExpectedActivity(consoleActivityId, BaseVocabulary.COMMAND_LINE, DEVICE1NAME + "'s command line"), jsonGenerated);
        assertContains("context", getExpectedContext(WIDGETURI), jsonGenerated);
        final String[][] exts = new String[][] {
                {BaseVocabulary.EXT_DEVICE_NAME, DEVICE1NAME}
        };
        assertContains("result", getExpectedResult(COMMANDLINE_TEXT, exts), jsonGenerated);
    }

    @Test
    public void commandLineEnded() throws JSONException {
        final String consoleActivityId = WIDGETURI + "device/"  + DEVICE1NAME.hashCode() + "/console";
        this.testable.commandLineEnded(DEVICE1NAME);
        final String jsonGenerated = this.testable.statementToRecord.toJSON();
        assertContains("actor", getExpectedActor(), jsonGenerated);
        assertContains("verb", getExpectedVerb(BaseVocabulary.TERMINATED), jsonGenerated);
        assertContains("object", getExpectedActivity(consoleActivityId, BaseVocabulary.COMMAND_LINE, DEVICE1NAME + "'s command line"), jsonGenerated);
        assertContains("context", getExpectedContext(WIDGETURI), jsonGenerated);
        assertNotContains("result", jsonGenerated);
    }
}

class TestableTinCanAPI extends TinCanAPI {

    Statement statementToRecord;

    @Override
    protected void record(final Statement statement) {
        this.statementToRecord = statement;
    }
}