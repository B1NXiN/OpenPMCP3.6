package dev.yalan.irc.netty.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import javax.crypto.Cipher;
import java.security.Key;
import java.util.List;

public class AESDecoder extends MessageToMessageDecoder<ByteBuf> {
    private final Key key;

    public AESDecoder(Key key) {
        this.key = key;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (in.readableBytes() < 4) {
            return;
        }

        ByteBuf decryptBuffer = null;

        try {
            final Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, this.key);

            final int rawDataSize = in.readInt();
            decryptBuffer = ctx.alloc().buffer(cipher.getOutputSize(in.readableBytes()));
            cipher.doFinal(in.nioBuffer(), decryptBuffer.nioBuffer(0, decryptBuffer.capacity()));

            out.add(decryptBuffer.retainedSlice(0, rawDataSize));
        } finally {
            if (decryptBuffer != null) {
                decryptBuffer.release();
            }
        }
    }
}
