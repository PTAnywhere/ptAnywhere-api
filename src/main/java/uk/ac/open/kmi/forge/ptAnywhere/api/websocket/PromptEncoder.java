package uk.ac.open.kmi.forge.ptAnywhere.api.websocket;

import javax.json.Json;
import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;


public class PromptEncoder implements Encoder.Text<String> {

    public static final String MESSAGE_TYPE = "prompt";

    @Override
    public String encode(String commands) throws EncodeException {
        return Json.createObjectBuilder().add(MESSAGE_TYPE, commands).build().toString();
    }

    @Override
    public void init(EndpointConfig ec) {}

    @Override
    public void destroy() {}
}
