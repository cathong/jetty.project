//
//  ========================================================================
//  Copyright (c) 1995-2018 Mort Bay Consulting Pty. Ltd.
//  ------------------------------------------------------------------------
//  All rights reserved. This program and the accompanying materials
//  are made available under the terms of the Eclipse Public License v1.0
//  and Apache License v2.0 which accompanies this distribution.
//
//      The Eclipse Public License is available at
//      http://www.eclipse.org/legal/epl-v10.html
//
//      The Apache License v2.0 is available at
//      http://www.opensource.org/licenses/apache2.0.php
//
//  You may elect to redistribute this code under either of these licenses.
//  ========================================================================
//

package org.eclipse.jetty.websocket.core.extensions;

import org.eclipse.jetty.toolchain.test.ByteBufferAssert;
import org.eclipse.jetty.util.BufferUtil;
import org.eclipse.jetty.util.Callback;
import org.eclipse.jetty.websocket.core.Extension;
import org.eclipse.jetty.websocket.core.Frame;
import org.eclipse.jetty.websocket.core.IncomingFramesCapture;
import org.eclipse.jetty.websocket.core.OpCode;
import org.eclipse.jetty.websocket.core.OutgoingFramesCapture;
import org.eclipse.jetty.websocket.core.internal.IdentityExtension;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class IdentityExtensionTest extends AbstractExtensionTest
{
    /**
     * Verify that incoming frames are unmodified
     */
    @Test
    public void testIncomingFrames()
    {
        IncomingFramesCapture capture = new IncomingFramesCapture();

        Extension ext = new IdentityExtension();
        ext.setNextIncomingFrames(capture);

        Frame frame = new Frame(OpCode.TEXT).setPayload("hello");
        ext.onFrame(frame, Callback.NOOP);

        capture.assertFrameCount(1);
        capture.assertHasOpCount(OpCode.TEXT, 1);
        Frame actual = capture.frames.poll();

        assertThat("Frame.opcode", actual.getOpCode(), is(OpCode.TEXT));
        assertThat("Frame.fin", actual.isFin(), is(true));
        assertThat("Frame.rsv1", actual.isRsv1(), is(false));
        assertThat("Frame.rsv2", actual.isRsv2(), is(false));
        assertThat("Frame.rsv3", actual.isRsv3(), is(false));

        ByteBuffer expected = BufferUtil.toBuffer("hello", StandardCharsets.UTF_8);
        assertThat("Frame.payloadLength", actual.getPayloadLength(), is(expected.remaining()));
        ByteBufferAssert.assertEquals("Frame.payload", expected, actual.getPayload().slice());
    }

    /**
     * Verify that outgoing frames are unmodified
     *
     * @throws IOException on test failure
     */
    @Test
    public void testOutgoingFrames() throws IOException
    {
        OutgoingFramesCapture capture = new OutgoingFramesCapture();

        Extension ext = new IdentityExtension();
        ext.setNextOutgoingFrames(capture);

        Frame frame = new Frame(OpCode.TEXT).setPayload("hello");
        ext.sendFrame(frame, null, false);

        capture.assertFrameCount(1);
        capture.assertHasOpCount(OpCode.TEXT, 1);

        Frame actual = capture.frames.poll();

        assertThat("Frame.opcode", actual.getOpCode(), is(OpCode.TEXT));
        assertThat("Frame.fin", actual.isFin(), is(true));
        assertThat("Frame.rsv1", actual.isRsv1(), is(false));
        assertThat("Frame.rsv2", actual.isRsv2(), is(false));
        assertThat("Frame.rsv3", actual.isRsv3(), is(false));

        ByteBuffer expected = BufferUtil.toBuffer("hello", StandardCharsets.UTF_8);
        assertThat("Frame.payloadLength", actual.getPayloadLength(), is(expected.remaining()));
        ByteBufferAssert.assertEquals("Frame.payload", expected, actual.getPayload().slice());
    }
}