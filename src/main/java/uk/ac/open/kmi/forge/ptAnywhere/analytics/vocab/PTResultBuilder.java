package uk.ac.open.kmi.forge.ptAnywhere.analytics.vocab;

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

    public PTResultBuilder endpointsExt(String[] endpointURLs) throws URISyntaxException {
        getExtensions().put(BaseVocabulary.EXT_ENDPOINTS, endpointURLs);
        return this;
    }

    public PTResultBuilder linkUriExt(String linkUri) throws URISyntaxException {
        getExtensions().put(BaseVocabulary.EXT_LINK_URI, linkUri);
        return this;
    }

    public Result build() {
        return this.result;
    }

}
