/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package uk.ac.open.kmi.forge.webPacketTracer;

import com.cisco.pt.ipc.events.TerminalLineEvent;
import com.cisco.pt.ipc.events.TerminalLineEventListener;
import com.cisco.pt.ipc.events.TerminalLineEventRegistry;
import com.cisco.pt.ipc.sim.Pc;
import com.cisco.pt.ipc.sim.TerminalLine;
import uk.ac.open.kmi.forge.webPacketTracer.gateway.PTDaemon;
import java.io.IOException;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint("/endpoint/devices/{device}/console")
public class ConsoleOutput implements TerminalLineEventListener {

    PTDaemon common;
    TerminalLine cmd;
    Session session;

    public ConsoleOutput() {
        this.common = new PTDaemon();
    }

    @OnOpen
    public void myOnOpen(final Session session) {
        this.session = session;

        this.common.start();
        final String deviceId = "{" + session.getPathParameters().get("device") + "}";
        final Pc pc0 = (Pc) this.common.getDataAccessObject().getSimDeviceById(deviceId);
        this.cmd = pc0.getCommandLine();
        try {
            final TerminalLineEventRegistry registry = this.common.getTerminalLineEventRegistry();
            this.session.getBasicRemote().sendText(this.cmd.getPrompt());
            registry.addListener(this, this.cmd);
        } catch(IOException e) {
            this.common.getLog().error(e.getMessage(), e);
        }
    }

    @OnClose
    public void myOnClose(final CloseReason reason) {
        try {
            //System.out.println("Closing a WebSocket due to " + reason.getReasonPhrase());
            this.common.getTerminalLineEventRegistry().removeListener(this, this.cmd);
        } catch(IOException e) {
            this.common.getLog().error(e.getMessage(), e);
        } finally {
            this.common.stop();
        }
    }

    @OnMessage
    public void typeCommand(Session session, String msg, boolean last) {
        if (session.isOpen()) {
            this.cmd.enterCommand(msg);
        }
    }

    public void handleEvent(TerminalLineEvent event) {
        if (event.eventName.equals("outputWritten")) {
            try {
                final String msg = ((TerminalLineEvent.OutputWritten) event).newOutput;
                this.session.getBasicRemote().sendText(msg);
            } catch(IOException e) {
                this.common.getLog().error(e.getMessage(), e);
            }
        }
    }
}