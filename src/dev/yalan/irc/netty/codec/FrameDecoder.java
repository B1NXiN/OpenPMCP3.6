package dev.yalan.irc.netty.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class FrameDecoder extends ByteToMessageDecoder {
    private volatile int currentPacketSize = Integer.MIN_VALUE;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> out) {
        int rl = this.currentPacketSize;
        if (rl == Integer.MIN_VALUE) {
            if (buf.readableBytes() >= 4) {
                rl = buf.readInt();

                if(rl < 0) {
                    ctx.channel().close();
                    return;
                }
            } else {
                return;
            }
        }

        if (buf.readableBytes() >= rl) {
            out.add(buf.readRetainedSlice(rl));

            this.currentPacketSize = Integer.MIN_VALUE;
        } else {
            this.currentPacketSize = rl;
        }
    }
}
