/*******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.radiora.api.codec;

import com.whizzosoftware.hobson.radiora.api.command.RadioRaCommand;
import com.whizzosoftware.hobson.radiora.api.command.SetDimmerLevel;
import com.whizzosoftware.hobson.radiora.api.command.SetSwitchLevel;
import com.whizzosoftware.hobson.radiora.api.command.ZoneMapInquiry;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Netty encoder for RadioRA command frames.
 *
 * @author Dan Noguerol
 */
public class RadioRaFrameEncoder extends MessageToByteEncoder<RadioRaCommand> {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    protected void encode(ChannelHandlerContext ctx, RadioRaCommand cmd, ByteBuf out) throws Exception {
        String cmdStr = null;

        switch (cmd.getType()) {

            case SetDimmerLevel.TYPE:
                SetDimmerLevel sdl = (SetDimmerLevel)cmd;
                cmdStr = "SDL," + sdl.getZoneNumber() + "," + sdl.getDimmerLevel();
                if (sdl.hasFadeTime()) {
                    cmdStr += "," + sdl.getFadeTime();
                }
                break;

            case SetSwitchLevel.TYPE:
                SetSwitchLevel ssl = (SetSwitchLevel)cmd;
                cmdStr = "SSL," + ssl.getZoneNumber() + "," + (ssl.isOn() ? "ON" : "OFF");
                if (ssl.hasDelayTime()) {
                    cmdStr += "," + ssl.getDelayTime();
                }
                break;

            case ZoneMapInquiry.TYPE:
                cmdStr = "ZMPI";
                break;
        }

        if (cmdStr != null) {
            logger.trace("encode: {}", cmdStr);
            out.writeBytes((cmdStr + "\r").getBytes());
        } else {
            logger.error("Attempt to send unknown command class: {}", cmd);
        }
    }
}
