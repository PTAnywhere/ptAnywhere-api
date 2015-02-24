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

import com.cisco.pt.backpacks.framework.Backpack;
import com.cisco.pt.ipc.IPCError;
import com.cisco.pt.ipc.events.TerminalLineEvent;
import com.cisco.pt.ipc.events.TerminalLineEventListener;
import com.cisco.pt.ipc.events.TerminalLineEventRegistry;
import com.cisco.pt.ipc.sim.Network;
import com.cisco.pt.ipc.sim.Pc;
import com.cisco.pt.ipc.sim.TerminalLine;
import com.cisco.pt.ipc.ui.IPC;
import uk.ac.open.kmi.forge.webPacketTracer.gateway.PTCallable;
import uk.ac.open.kmi.forge.webPacketTracer.gateway.PTCommon;
import uk.ac.open.kmi.forge.webPacketTracer.gateway.PTDaemon;
import uk.ac.open.kmi.forge.webPacketTracer.gateway.PTRunnable;

import java.io.IOException;
import java.nio.ByteBuffer;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import javax.ws.rs.PathParam;

@ServerEndpoint("/devices/{device}/w/console")
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

        //System.out.println ("WebSocket opened: "+session.getId());
        this.common.start();
        final Network network = this.common.getNetwork();
        final String deviceId = (String) session.getPathParameters().get("device");

        final Pc pc0 = (Pc) network.getDevice(deviceId); //"PC0");
        this.cmd = pc0.getCommandLine();

        final TerminalLineEventRegistry registry = this.common.getTerminalLineEventRegistry();
        try {
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
        this.cmd.enterCommand(msg);
        /*try {
            this.cmd.enterCommand("ping 10.2.0.2");
            if (session.isOpen()) {
                System.out.print(cmd.getPrompt());
                this.cmd.enterCommand("ping 10.2.0.2");
                session.getBasicRemote().sendText(msg, last);
            }
        } catch (IOException e) {
            try {
                session.close();
            } catch (IOException e1) {
                // Ignore
            }
        }*/
    }

    /*@OnMessage
    public void echoBinaryMessage(Session session, ByteBuffer bb,
            boolean last) {
        try {
            if (session.isOpen()) {
                session.getBasicRemote().sendBinary(bb, last);
            }
        } catch (IOException e) {
            try {
                session.close();
            } catch (IOException e1) {
                // Ignore
            }
        }
    }*/

    /**
     * Process a received pong. This is a NO-OP.
     *
     * @param pm    Ignored.
     */
    /*@OnMessage
    public void echoPongMessage(PongMessage pm) {
        // NO-OP
    }*/

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