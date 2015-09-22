package uk.ac.open.kmi.forge.ptAnywhere.api.websocket;


import com.cisco.pt.ipc.events.TerminalLineEvent;

import javax.json.Json;
import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;


public class OutputWritterEventEncoder implements Encoder.Text<TerminalLineEvent.OutputWritten> {

    public static final String MESSAGE_TYPE = "out";

    @Override
    public String encode(TerminalLineEvent.OutputWritten event) throws EncodeException {
        return Json.createObjectBuilder().add(MESSAGE_TYPE, event.newOutput).build().toString();
    }

    @Override
    public void init(EndpointConfig ec) {}

    @Override
    public void destroy() {}
}
