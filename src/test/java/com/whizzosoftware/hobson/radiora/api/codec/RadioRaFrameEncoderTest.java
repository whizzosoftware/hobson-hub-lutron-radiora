/*******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.radiora.api.codec;

import com.whizzosoftware.hobson.radiora.api.command.SetDimmerLevel;
import com.whizzosoftware.hobson.radiora.api.command.SetSwitchLevel;
import com.whizzosoftware.hobson.radiora.api.command.ZoneMapInquiry;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Test;

import java.nio.charset.Charset;

import static org.junit.Assert.*;

public class RadioRaFrameEncoderTest {
    @Test
    public void testSetDimmerLevelEncode() throws Exception {
        RadioRaFrameEncoder encoder = new RadioRaFrameEncoder();

        // test with 3 digit level
        SetDimmerLevel sdl = new SetDimmerLevel(1, 100);
        ByteBuf buf = Unpooled.buffer();
        encoder.encode(null, sdl, buf);
        assertEquals("SDL,1,100\r", buf.toString(Charset.forName("UTF8")));

        // test with 1 digit level
        sdl = new SetDimmerLevel(1, 1);
        buf = Unpooled.buffer();
        encoder.encode(null, sdl, buf);
        assertEquals("SDL,1,1\r", buf.toString(Charset.forName("UTF8")));

        // test with fade time
        sdl = new SetDimmerLevel(1, 1, 50);
        buf = Unpooled.buffer();
        encoder.encode(null, sdl, buf);
        assertEquals("SDL,1,1,50\r", buf.toString(Charset.forName("UTF8")));
    }

    @Test
    public void testSetSwitchLevelEncode() throws Exception {
        RadioRaFrameEncoder encoder = new RadioRaFrameEncoder();

        // test with single digit zone number
        SetSwitchLevel ssl = new SetSwitchLevel(1, true);
        ByteBuf buf = Unpooled.buffer();
        encoder.encode(null, ssl, buf);
        assertEquals("SSL,1,ON\r", buf.toString(Charset.forName("UTF8")));

        // test with double digit zone number
        ssl = new SetSwitchLevel(10, true);
        buf = Unpooled.buffer();
        encoder.encode(null, ssl, buf);
        assertEquals("SSL,10,ON\r", buf.toString(Charset.forName("UTF8")));

        // test with delay time
        ssl = new SetSwitchLevel(10, true, 50);
        buf = Unpooled.buffer();
        encoder.encode(null, ssl, buf);
        assertEquals("SSL,10,ON,50\r", buf.toString(Charset.forName("UTF8")));
    }

    @Test
    public void testZoneMapInquiryEncode() throws Exception {
        RadioRaFrameEncoder encoder = new RadioRaFrameEncoder();
        ZoneMapInquiry zmpi = new ZoneMapInquiry();
        ByteBuf buf = Unpooled.buffer();
        encoder.encode(null, zmpi, buf);
        assertEquals("ZMPI\r", buf.toString(Charset.forName("UTF8")));
    }
}
