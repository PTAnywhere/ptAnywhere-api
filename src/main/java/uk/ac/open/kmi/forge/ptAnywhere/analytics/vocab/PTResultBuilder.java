package uk.ac.open.kmi.forge.ptAnywhere.analytics.vocab;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.rusticisoftware.tincan.Extensions;
import com.rusticisoftware.tincan.Result;

import java.net.URISyntaxException;


public class PTResultBuilder {

    Result result = null;

    public PTResultBuilder() {
    }

    public PTResultBuilder response(String response) {
        getResult().setResponse(response);
        return this;
    }

    private Result getResult() {
        if (this.result==null) {
            this.result=new Result();
        }
        return this.result;
    }

    private Extensions getExtensions() {
        if (getResult().getExtensions()==null) {
            this.result.setExtensions(new Extensions());
        }
        return this.result.getExtensions();
    }

    public PTResultBuilder deviceNameExt(String deviceName) throws URISyntaxException {
        getExtensions().put(BaseVocabulary.EXT_DEVICE_NAME, deviceName);
        return this;
    }

    public PTResultBuilder deviceTypeExt(String deviceType) throws URISyntaxException {
        getExtensions().put(BaseVocabulary.EXT_DEVICE_TYPE, deviceType);
        return this;
    }

    public PTResultBuilder deviceURIExt(String deviceUri) throws URISyntaxException {
        getExtensions().put(BaseVocabulary.EXT_DEVICE_URI, deviceUri);
        return this;
    }

    public PTResultBuilder deviceDefaultGwExt(String defaultGateway) throws URISyntaxException {
        getExtensions().put(BaseVocabulary.EXT_DEVICE_GW, defaultGateway);
        return this;
    }

    public PTResultBuilder portURIExt(String portUri) throws URISyntaxException {
        getExtensions().put(BaseVocabulary.EXT_PORT_URI, portUri);
        return this;
    }

    public PTResultBuilder portNameExt(String portName) throws URISyntaxException {
        getExtensions().put(BaseVocabulary.EXT_PORT_NAME, portName);
        return this;
    }

    public PTResultBuilder portIpAddressExt(String ipAddress) throws URISyntaxException {
        getExtensions().put(BaseVocabulary.EXT_PORT_IP_ADDR, ipAddress);
        return this;
    }

    public PTResultBuilder portSubnetMaskExt(String subnetMask) throws URISyntaxException {
        getExtensions().put(BaseVocabulary.EXT_PORT_SUBNET_MASK, subnetMask);
        return this;
    }

    private ObjectNode createEndpointObject(String endpointName, String endpointPort) {
        return JsonNodeFactory.instance.objectNode().
                put(BaseVocabulary.EXT_ENDPOINT_DEVICE, endpointName).
                put(BaseVocabulary.EXT_ENDPOINT_PORT, endpointPort);
    }

    public PTResultBuilder endpointsExt(String endpoint1Name, String endpoint1Port,
                                        String endpoint2Name, String endpoint2Port) throws URISyntaxException {
        final JsonNode array = JsonNodeFactory.instance.arrayNode().
                                                add(createEndpointObject(endpoint1Name, endpoint1Port)).
                                                add(createEndpointObject(endpoint2Name, endpoint2Port));
        getExtensions().put(BaseVocabulary.EXT_ENDPOINTS, array);
        return this;
    }

    public PTResultBuilder linkUriExt(String linkUri) throws URISyntaxException {
        getExtensions().put(BaseVocabulary.EXT_LINK_URI, linkUri);
        return this;
    }

    public PTResultBuilder positionExt(double x, double y) throws URISyntaxException {
        final JsonNode position = JsonNodeFactory.instance.objectNode().
                put(BaseVocabulary.EXT_POSITION_X, x).
                put(BaseVocabulary.EXT_POSITION_Y, y);
        getExtensions().put(BaseVocabulary.EXT_DEVICE_POSITION, position);
        return this;
    }

    public Result build() {
        return this.result;
    }

}
