/*******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.radiora.api.codec;

import com.whizzosoftware.hobson.radiora.api.command.LEDMap;
import com.whizzosoftware.hobson.radiora.api.command.LocalZoneChange;
import com.whizzosoftware.hobson.radiora.api.command.ZoneMap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.CorruptedFrameException;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Netty decoder for RadioRA command frames.
 *
 * @author Dan Noguerol
 */
public class RadioRaFrameDecoder extends DelimiterBasedFrameDecoder {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final static int MAX_FRAME_LENGTH = 1024;
    private final static String FRAME_DELIMETER = "\r";

    public RadioRaFrameDecoder() {
        super(MAX_FRAME_LENGTH, Unpooled.copiedBuffer(FRAME_DELIMETER, CharsetUtil.UTF_8));
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf buffer) throws Exception {
        if (logger.isTraceEnabled()) {
            logger.trace("decode: {}", buffer.toString(CharsetUtil.UTF_8));
        }

        ByteBuf frame = (ByteBuf)super.decode(ctx, buffer);

        if (frame != null) {
            try {
                // if we receive a single byte frame, it should be a '!'
                if (frame.readableBytes() == 1) {
                    byte b = frame.readByte();
                    if (b == '!') {
                        return null;
                    } else {
                        throw new CorruptedFrameException("Unexpected single byte frame");
                    }
                    // otherwise, we assume it's a command frame
                } else {
                    int ix = frame.indexOf(frame.readerIndex(), frame.writerIndex(), (byte) ',');
                    if (ix > 0) {
                        String cmdName = frame.slice(0, ix).toString(CharsetUtil.UTF_8);

                        switch (cmdName) {
                            case ZoneMap.TYPE:
                                if (frame.readableBytes() >= ix + 33) {
                                    return new ZoneMap(frame.slice(ix + 1, 32).toString(CharsetUtil.UTF_8));
                                } else {
                                    throw new CorruptedFrameException("Received invalid zone map size");
                                }

                            case LocalZoneChange.TYPE:
                                if (frame.readableBytes() >= ix + 3) {
                                    String sZoneNum = frame.slice(ix + 1, 2).toString(CharsetUtil.UTF_8);
                                    if (frame.readableBytes() >= ix + 7) {
                                        String state = frame.slice(ix + 4, 3).toString(CharsetUtil.UTF_8).trim();
                                        try {
                                            return new LocalZoneChange(Integer.parseInt(sZoneNum), LocalZoneChange.State.valueOf(state));
                                        } catch (IllegalArgumentException iae) {
                                            throw new CorruptedFrameException("Invalid LZC state string");
                                        }
                                    } else {
                                        throw new CorruptedFrameException("Invalid LZC size (state)");
                                    }
                                } else {
                                    throw new CorruptedFrameException("Invalid LZC size (zoneNum)");
                                }

                            case LEDMap.TYPE:
                                if (frame.readableBytes() >= ix + 16) {
                                    return new LEDMap(frame.slice(ix + 1, 15).toString(CharsetUtil.UTF_8));
                                } else {
                                    throw new CorruptedFrameException("Invalid LED map size");
                                }

                            default:
                                throw new DecoderException("Unrecognized command: " + cmdName);
                        }
                    } else {
                        throw new CorruptedFrameException("Invalid frame format (no comma)");
                    }
                }
            } finally {
                // we consumed the frame, so we need to release it
                frame.release();
            }
        } else {
            return null;
        }
    }
}
