package dev.yalan.irc.netty.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

import javax.crypto.Cipher;
import java.nio.ByteBuffer;
import java.security.Key;
import java.util.List;

public class AESEncoder extends MessageToMessageEncoder<ByteBuf> {
    private final Key key;

    public AESEncoder(Key key) {
        this.key = key;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        ByteBuf encryptBuffer = null;

        try {
            final Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, this.key);

            encryptBuffer = ctx.alloc().buffer(4 + cipher.getOutputSize(msg.readableBytes()));
            encryptBuffer.writeInt(msg.readableBytes());

            final ByteBuffer _encryptBuffer = encryptBuffer.nioBuffer(4, encryptBuffer.capacity() - 4);
            _encryptBuffer.position(0);
            _encryptBuffer.limit(_encryptBuffer.capacity());
            cipher.doFinal(msg.nioBuffer(), _encryptBuffer);
            encryptBuffer.writerIndex(4 + _encryptBuffer.position());

            out.add(encryptBuffer.retain());
        } finally {
            if (encryptBuffer != null) {
                encryptBuffer.release();
            }
        }
    }
}
