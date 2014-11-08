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
import io.netty.handler.codec.CorruptedFrameException;
import io.netty.handler.codec.DecoderException;
import io.netty.util.CharsetUtil;
import org.junit.Test;
import static org.junit.Assert.*;

import java.nio.charset.Charset;

public class RadioRaFrameDecoderTest {
    @Test
    public void testZoneMapDecode() throws Exception {
        // test valid zone map
        ByteBuf readData = Unpooled.copiedBuffer("ZMP,000000000000000000000XXXXXXXXXXX\r", Charset.forName("UTF8"));
        RadioRaFrameDecoder decoder = new RadioRaFrameDecoder();
        Object cmd = decoder.decode(null, readData);
        assertTrue(cmd instanceof ZoneMap);
        assertEquals("000000000000000000000XXXXXXXXXXX", ((ZoneMap)cmd).getZoneMap());
        assertNull(((ZoneMap)cmd).getSystem());

        // test no comma after zone map
        readData = Unpooled.copiedBuffer("ZMP\r", Charset.forName("UTF8"));
        decoder = new RadioRaFrameDecoder();
        try {
            decoder.decode(null, readData);
            fail("Should have thrown exception");
        } catch (Exception e) {
            assertTrue(e instanceof CorruptedFrameException);
        }

        // test invalid length zone map
        readData = Unpooled.copiedBuffer("ZMP,0000000000000\r", Charset.forName("UTF8"));
        decoder = new RadioRaFrameDecoder();
        try {
            decoder.decode(null, readData);
            fail("Should have thrown exception");
        } catch (Exception e) {
            assertTrue(e instanceof CorruptedFrameException);
        }
    }

    @Test
    public void testLocalZoneChangeDecode() throws Exception {
        // test valid ON
        ByteBuf readData = Unpooled.copiedBuffer("LZC,01,ON \r", Charset.forName("UTF8"));
        RadioRaFrameDecoder decoder = new RadioRaFrameDecoder();
        Object cmd = decoder.decode(null, readData);
        assertTrue(cmd instanceof LocalZoneChange);
        assertEquals(1, ((LocalZoneChange)cmd).getZoneNumber());
        assertEquals(LocalZoneChange.State.ON, ((LocalZoneChange)cmd).getState());

        // test valid OFF
        readData = Unpooled.copiedBuffer("LZC,10,OFF\r", Charset.forName("UTF8"));
        cmd = decoder.decode(null, readData);
        assertTrue(cmd instanceof LocalZoneChange);
        assertEquals(10, ((LocalZoneChange)cmd).getZoneNumber());
        assertEquals(LocalZoneChange.State.OFF, ((LocalZoneChange)cmd).getState());

        // test valid CHG
        readData = Unpooled.copiedBuffer("LZC,21,CHG\r", Charset.forName("UTF8"));
        cmd = decoder.decode(null, readData);
        assertTrue(cmd instanceof LocalZoneChange);
        assertEquals(21, ((LocalZoneChange)cmd).getZoneNumber());
        assertEquals(LocalZoneChange.State.CHG, ((LocalZoneChange)cmd).getState());

        // test invalid frame (no comma)
        readData = Unpooled.copiedBuffer("LZC\r", Charset.forName("UTF8"));
        try {
            decoder.decode(null, readData);
            fail("Should have thrown exception");
        } catch (Exception e) {
            assertTrue(e instanceof CorruptedFrameException);
        }

        // test invalid frame (no state)
        readData = Unpooled.copiedBuffer("LZC,1\r", Charset.forName("UTF8"));
        try {
            decoder.decode(null, readData);
            fail("Should have thrown exception");
        } catch (Exception e) {
            assertTrue(e instanceof CorruptedFrameException);
        }

        // test invalid frame (invalid state)
        readData = Unpooled.copiedBuffer("LZC,1,O\r", Charset.forName("UTF8"));
        try {
            decoder.decode(null, readData);
            fail("Should have thrown exception");
        } catch (Exception e) {
            assertTrue(e instanceof CorruptedFrameException);
        }
    }

    @Test
    public void testLEDMapDecode() throws Exception {
        RadioRaFrameDecoder decoder = new RadioRaFrameDecoder();

        // test valid frame
        ByteBuf readData = Unpooled.copiedBuffer("LMP,000000000000000\r", Charset.forName("UTF8"));
        Object cmd = decoder.decode(null, readData);
        assertTrue(cmd instanceof LEDMap);
        assertEquals("000000000000000", ((LEDMap) cmd).getStates());

        // test invalid frame (no comma)
        readData = Unpooled.copiedBuffer("LMP\r", Charset.forName("UTF8"));
        try {
            decoder.decode(null, readData);
            fail("Should have thrown exception");
        } catch (Exception e) {
            assertTrue(e instanceof CorruptedFrameException);
        }

        // test invalid frame (map size)
        readData = Unpooled.copiedBuffer("LMP,000\r", Charset.forName("UTF8"));
        try {
            decoder.decode(null, readData);
            fail("Should have thrown exception");
        } catch (Exception e) {
            assertTrue(e instanceof CorruptedFrameException);
        }
    }

    @Test
    public void testExclamationPoint() throws Exception {
        ByteBuf readData = Unpooled.copiedBuffer("!\r", CharsetUtil.UTF_8);
        RadioRaFrameDecoder decoder = new RadioRaFrameDecoder();
        Object cmd = decoder.decode(null, readData);
        assertNull(cmd);
    }

    @Test
    public void testUnknownCommand() throws Exception {
        RadioRaFrameDecoder decoder = new RadioRaFrameDecoder();

        // typical frame structure
        ByteBuf readData = Unpooled.copiedBuffer("BOO,000000000000000000000XXXXXXXXXXX\r", Charset.forName("UTF8"));
        try {
            decoder.decode(null, readData);
            fail("Should have thrown exception");
        } catch (Exception e) {
            assertTrue(e instanceof DecoderException);
        }

        // other frame structure
        readData = Unpooled.copiedBuffer("BO\r", Charset.forName("UTF8"));
        try {
            decoder.decode(null, readData);
            fail("Should have thrown exception");
        } catch (Exception e) {
            assertTrue(e instanceof CorruptedFrameException);
        }
    }
}
