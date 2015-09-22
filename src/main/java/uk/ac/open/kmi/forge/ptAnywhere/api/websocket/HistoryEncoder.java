package uk.ac.open.kmi.forge.ptAnywhere.api.websocket;


import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;
import java.util.List;


public class HistoryEncoder implements Encoder.Text<List<String>> {

    public static final String MESSAGE_TYPE = "history";

    @Override
    public String encode(List<String> commands) throws EncodeException {
        final JsonArrayBuilder jsonArray = Json.createArrayBuilder();
        for(String command: commands) {
            if (!command.equals(""))
                jsonArray.add(command);
        }
        return Json.createObjectBuilder().add(MESSAGE_TYPE, jsonArray).build().toString();
    }

    @Override
    public void init(EndpointConfig ec) {}

    @Override
    public void destroy() {}
}
