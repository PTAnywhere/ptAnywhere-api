package uk.ac.open.kmi.forge.ptAnywhere.analytics;

import com.rusticisoftware.tincan.Statement;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.skyscreamer.jsonassert.JSONAssert;
import uk.ac.open.kmi.forge.ptAnywhere.analytics.tincanapi.SimpleStatementRecorder;
import uk.ac.open.kmi.forge.ptAnywhere.analytics.vocab.BaseVocabulary;
import uk.ac.open.kmi.forge.ptAnywhere.identity.Identifiable;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import java.net.MalformedURLException;


public class TinCanAPITest {

    final static String USER_NAME = "User name";
    final static String USER_HOMEPAGE = "http://users/";
    final static String USER_ACCOUNT = "name";
    final static String WIDGET_URI = "http://testuri/";
    final static String SESSION_ID = "b8d5exozT9eNsR1udGjbZQ--";
    final static String SESSION_UUID = "6fc7797b-1a33-4fd7-8db1-1d6e7468db65";
    final static String DEVICE1_URI = "http://device1";
    final static String DEVICE1_NAME = "Device One";
    final static String DEVICE2_NAME = "Device Two";
    final static String DEVICE_TYPE = "router";
    final static String DEVICE_GW = "192.168.1.1";
    final static String PORT_URI = "http://port1";
    final static String PORT_NAME = "Port One";
    final static String PORT_IPADDR = "192.168.1.3";
    final static String PORT_SUBNETMASK = "255.255.255.0";
    final static String LINK_URI = "http://coolLink1234";
    final static String PORT1_NAME = "port1";
    final static String PORT2_NAME = "port2";
    final static String COMMANDLINE_TEXT = "ping 127.0.0.1";

    TestableRecorder checkable;
    TinCanAPI tested;

    @Before
    public void setUp() throws MalformedURLException {
        this.checkable = new TestableRecorder();
        this.tested = new TinCanAPI(this.checkable);
        this.tested.setURIFactory(new URIFactory(WIDGET_URI));
        this.tested.setSession(SESSION_ID);
        this.tested.setIdentity(new Identifiable() {
            @Override
            public String getName() {
                return USER_NAME;
            }
            @Override
            public String getHomePage() {
                return USER_HOMEPAGE;
            }
            @Override
            public String getAccountName() {
                return USER_ACCOUNT;
            }
        });
    }

    protected String getJson(String field, String valueInJson) {
        return "{\"" + field + "\":" + valueInJson + "}";
    }

    protected void assertContains(String field, String expectedJsonInField, String gotJson) throws JSONException {
        JSONAssert.assertEquals(getJson(field, expectedJsonInField), gotJson, false);
    }

    protected String getExpectedActor() {
        return "{\"objectType\":\"Agent\",\"name\":\"" + USER_NAME + "\",\"account\":" +
                "{\"homePage\":\"" + USER_HOMEPAGE + "\"," +
                "\"name\":\"" + USER_ACCOUNT + "\"}}";
    }

    protected String getExpectedVerb(String verb) {
        return "{\"id\":\"" + verb + "\"}";
    }

    protected String getExpectedContext() {
        return getJson("registration",  "\"" + SESSION_UUID + "\"");
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
        this.tested.interactionStarted();
        final String jsonGenerated = this.checkable.statementToRecord.toJSON();
        assertContains("actor", getExpectedActor(), jsonGenerated);
        assertContains("verb", getExpectedVerb(BaseVocabulary.INITIALIZED), jsonGenerated);
        assertContains("object", getExpectedActivity(WIDGET_URI, BaseVocabulary.SIMULATION), jsonGenerated);
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
        this.tested.deviceCreated(DEVICE1_URI, DEVICE1_NAME, DEVICE_TYPE, 44, 66);
        final String jsonGenerated = this.checkable.statementToRecord.toJSON();
        assertContains("actor", getExpectedActor(), jsonGenerated);
        assertContains("verb", getExpectedVerb(BaseVocabulary.CREATED), jsonGenerated);
        assertContains("object", getExpectedActivity(BaseVocabulary.SIMULATED_DEVICE + "/" + DEVICE_TYPE, BaseVocabulary.SIMULATION, "Simulated router"), jsonGenerated);
        assertContains("context", getExpectedContext(WIDGET_URI), jsonGenerated);
        final String[][] exts = new String[][] {
                {BaseVocabulary.EXT_DEVICE_NAME, DEVICE1_NAME},
                {BaseVocabulary.EXT_DEVICE_URI, DEVICE1_URI},
                {BaseVocabulary.EXT_DEVICE_TYPE, DEVICE_TYPE},
                {BaseVocabulary.EXT_DEVICE_POSITION, toPositionJson(44, 66)}
        };
        assertContains("result", getExpectedResult(DEVICE1_NAME, exts), jsonGenerated);
    }

    @Test
    public void testDeviceDeleted() throws JSONException {
        this.tested.deviceDeleted(DEVICE1_URI, DEVICE1_NAME, DEVICE_TYPE);
        final String jsonGenerated = this.checkable.statementToRecord.toJSON();
        assertContains("actor", getExpectedActor(), jsonGenerated);
        assertContains("verb", getExpectedVerb(BaseVocabulary.DELETED), jsonGenerated);
        assertContains("object", getExpectedActivity(BaseVocabulary.SIMULATED_DEVICE + "/" + DEVICE_TYPE, BaseVocabulary.SIMULATION, "Simulated router"), jsonGenerated);
        assertContains("context", getExpectedContext(WIDGET_URI), jsonGenerated);
        final String[][] exts = new String[][] {
                {BaseVocabulary.EXT_DEVICE_NAME, DEVICE1_NAME},
                {BaseVocabulary.EXT_DEVICE_URI, DEVICE1_URI},
                {BaseVocabulary.EXT_DEVICE_TYPE, DEVICE_TYPE}
        };
        assertContains("result", getExpectedResult(DEVICE1_NAME, exts), jsonGenerated);
    }

    @Test
    public void testDeviceModified() throws JSONException {
        this.tested.deviceModified(DEVICE1_URI, DEVICE1_NAME, DEVICE_TYPE, DEVICE2_NAME);
        final String jsonGenerated = this.checkable.statementToRecord.toJSON();
        assertContains("actor", getExpectedActor(), jsonGenerated);
        assertContains("verb", getExpectedVerb(BaseVocabulary.UPDATED), jsonGenerated);
        assertContains("object", getExpectedActivity(BaseVocabulary.SIMULATED_DEVICE + "/" + DEVICE_TYPE, BaseVocabulary.SIMULATION, "Simulated router"), jsonGenerated);
        assertContains("context", getExpectedContext(WIDGET_URI), jsonGenerated);
        final String[][] exts = new String[][] {
                {BaseVocabulary.EXT_DEVICE_NAME, DEVICE1_NAME},
                {BaseVocabulary.EXT_DEVICE_URI, DEVICE1_URI},
                {BaseVocabulary.EXT_DEVICE_TYPE, DEVICE_TYPE}
        };
        assertContains("result", getExpectedResult(DEVICE2_NAME, exts), jsonGenerated);
    }

    @Test
    public void testDeviceModifiedWithDefaultGateway() throws JSONException {
        this.tested.deviceModified(DEVICE1_URI, DEVICE1_NAME, DEVICE_TYPE, DEVICE2_NAME, DEVICE_GW);
        final String jsonGenerated = this.checkable.statementToRecord.toJSON();
        assertContains("actor", getExpectedActor(), jsonGenerated);
        assertContains("verb", getExpectedVerb(BaseVocabulary.UPDATED), jsonGenerated);
        assertContains("object", getExpectedActivity(BaseVocabulary.SIMULATED_DEVICE + "/" + DEVICE_TYPE, BaseVocabulary.SIMULATION, "Simulated router"), jsonGenerated);
        assertContains("context", getExpectedContext(WIDGET_URI), jsonGenerated);
        final String[][] exts = new String[][] {
                {BaseVocabulary.EXT_DEVICE_NAME, DEVICE1_NAME},
                {BaseVocabulary.EXT_DEVICE_URI, DEVICE1_URI},
                {BaseVocabulary.EXT_DEVICE_TYPE, DEVICE_TYPE},
                {BaseVocabulary.EXT_DEVICE_GW, DEVICE_GW}
        };
        assertContains("result", getExpectedResult(DEVICE2_NAME, exts), jsonGenerated);
    }

    @Test
    public void testPortModified() throws JSONException {
        final String portActivityId = WIDGET_URI + "device/"  + DEVICE1_NAME.hashCode() + "/port/Port%20One";
        this.tested.portModified(PORT_URI, DEVICE1_NAME, PORT_NAME, PORT_IPADDR, PORT_SUBNETMASK);
        final String jsonGenerated = this.checkable.statementToRecord.toJSON();
        assertContains("actor", getExpectedActor(), jsonGenerated);
        assertContains("verb", getExpectedVerb(BaseVocabulary.UPDATED), jsonGenerated);
        assertContains("object", getExpectedActivity(portActivityId, BaseVocabulary.SIMULATED_PORT, "Device One's port Port One"), jsonGenerated);
        assertContains("context", getExpectedContext(WIDGET_URI), jsonGenerated);
        final String[][] exts = new String[][] {
                {BaseVocabulary.EXT_PORT_URI, PORT_URI},
                {BaseVocabulary.EXT_DEVICE_NAME, DEVICE1_NAME},
                {BaseVocabulary.EXT_PORT_NAME, PORT_NAME},
                {BaseVocabulary.EXT_PORT_IP_ADDR, PORT_IPADDR},
                {BaseVocabulary.EXT_PORT_SUBNET_MASK, PORT_SUBNETMASK}
        };
        assertContains("result", getExpectedResult(PORT_NAME, exts), jsonGenerated);
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
    public void testDeviceConnected() throws JSONException {
        this.tested.deviceConnected(LINK_URI, DEVICE1_NAME, PORT1_NAME, DEVICE2_NAME, PORT2_NAME);
        final String jsonGenerated = this.checkable.statementToRecord.toJSON();
        assertContains("actor", getExpectedActor(), jsonGenerated);
        assertContains("verb", getExpectedVerb(BaseVocabulary.CREATED), jsonGenerated);
        assertContains("object", getExpectedActivity(BaseVocabulary.SIMULATED_LINK, BaseVocabulary.SIMULATION, "Link"), jsonGenerated);
        assertContains("context", getExpectedContext(WIDGET_URI), jsonGenerated);
        final String[][] exts = new String[][] {
                {BaseVocabulary.EXT_ENDPOINTS, toEndpointJson(DEVICE1_NAME, PORT1_NAME, DEVICE2_NAME, PORT2_NAME)},
                {BaseVocabulary.EXT_LINK_URI, LINK_URI}
        };
        assertContains("result", getExpectedResult(LINK_URI, exts), jsonGenerated);
    }

    @Test
    public void testDeviceDisconnected() throws JSONException {
        this.tested.deviceDisconnected(LINK_URI, DEVICE1_NAME, PORT1_NAME, DEVICE2_NAME, PORT2_NAME);
        final String jsonGenerated = this.checkable.statementToRecord.toJSON();
        assertContains("actor", getExpectedActor(), jsonGenerated);
        assertContains("verb", getExpectedVerb(BaseVocabulary.DELETED), jsonGenerated);
        assertContains("object", getExpectedActivity(BaseVocabulary.SIMULATED_LINK, BaseVocabulary.SIMULATION, "Link"), jsonGenerated);
        assertContains("context", getExpectedContext(WIDGET_URI), jsonGenerated);
        final String[][] exts = new String[][] {
                {BaseVocabulary.EXT_ENDPOINTS, toEndpointJson(DEVICE1_NAME, PORT1_NAME, DEVICE2_NAME, PORT2_NAME)},
                {BaseVocabulary.EXT_LINK_URI, LINK_URI}
        };
        assertContains("result", getExpectedResult(LINK_URI, exts), jsonGenerated);
    }

    @Test
    public void testCommandLineStarted() throws JSONException {
        final String consoleActivityId = WIDGET_URI + "device/"  + DEVICE1_NAME.hashCode() + "/console";
        this.tested.commandLineStarted(DEVICE1_NAME);
        final String jsonGenerated = this.checkable.statementToRecord.toJSON();
        assertContains("actor", getExpectedActor(), jsonGenerated);
        assertContains("verb", getExpectedVerb(BaseVocabulary.OPENED), jsonGenerated);
        assertContains("object", getExpectedActivity(consoleActivityId, BaseVocabulary.COMMAND_LINE, DEVICE1_NAME + "'s command line"), jsonGenerated);
        assertContains("context", getExpectedContext(WIDGET_URI), jsonGenerated);
        assertNotContains("result", jsonGenerated);
    }

    @Test
    public void testCommandLineUsed() throws JSONException {
        final String consoleActivityId = WIDGET_URI + "device/"  + DEVICE1_NAME.hashCode() + "/console";
        this.tested.commandLineUsed(DEVICE1_NAME, COMMANDLINE_TEXT);
        final String jsonGenerated = this.checkable.statementToRecord.toJSON();
        assertContains("actor", getExpectedActor(), jsonGenerated);
        assertContains("verb", getExpectedVerb(BaseVocabulary.USED), jsonGenerated);
        assertContains("object", getExpectedActivity(consoleActivityId, BaseVocabulary.COMMAND_LINE, DEVICE1_NAME + "'s command line"), jsonGenerated);
        assertContains("context", getExpectedContext(WIDGET_URI), jsonGenerated);
        final String[][] exts = new String[][] {
                {BaseVocabulary.EXT_DEVICE_NAME, DEVICE1_NAME}
        };
        assertContains("result", getExpectedResult(COMMANDLINE_TEXT, exts), jsonGenerated);
    }

    @Test
    public void testCommandLineRead() throws JSONException {
        final String consoleActivityId = WIDGET_URI + "device/"  + DEVICE1_NAME.hashCode() + "/console";
        this.tested.commandLineRead(DEVICE1_NAME, COMMANDLINE_TEXT);
        final String jsonGenerated = this.checkable.statementToRecord.toJSON();
        assertContains("actor", getExpectedActor(), jsonGenerated);
        assertContains("verb", getExpectedVerb(BaseVocabulary.READ), jsonGenerated);
        assertContains("object", getExpectedActivity(consoleActivityId, BaseVocabulary.COMMAND_LINE, DEVICE1_NAME + "'s command line"), jsonGenerated);
        assertContains("context", getExpectedContext(WIDGET_URI), jsonGenerated);
        final String[][] exts = new String[][] {
                {BaseVocabulary.EXT_DEVICE_NAME, DEVICE1_NAME}
        };
        assertContains("result", getExpectedResult(COMMANDLINE_TEXT, exts), jsonGenerated);
    }

    @Test
    public void testCommandLineEnded() throws JSONException {
        final String consoleActivityId = WIDGET_URI + "device/"  + DEVICE1_NAME.hashCode() + "/console";
        this.tested.commandLineEnded(DEVICE1_NAME);
        final String jsonGenerated = this.checkable.statementToRecord.toJSON();
        assertContains("actor", getExpectedActor(), jsonGenerated);
        assertContains("verb", getExpectedVerb(BaseVocabulary.CLOSED), jsonGenerated);
        assertContains("object", getExpectedActivity(consoleActivityId, BaseVocabulary.COMMAND_LINE, DEVICE1_NAME + "'s command line"), jsonGenerated);
        assertContains("context", getExpectedContext(WIDGET_URI), jsonGenerated);
        assertNotContains("result", jsonGenerated);
    }
}

class TestableRecorder extends SimpleStatementRecorder {
    Statement statementToRecord;

    @Override
    public void record(final Statement statement) {
        this.statementToRecord = statement;
    }
}