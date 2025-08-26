package dev.yalan.irc;

import dev.yalan.irc.netty.IRCHandler;
import dev.yalan.irc.netty.IRCProto;
import dev.yalan.irc.netty.codec.AESDecoder;
import dev.yalan.irc.netty.codec.AESEncoder;
import dev.yalan.irc.netty.codec.FrameDecoder;
import dev.yalan.irc.netty.codec.FrameEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;
import jnic.JNICInclude;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import pmcp.utils.client.Helper;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicBoolean;

@JNICInclude
public class IRCClient {
    public static IRCClient INSTANCE;
    private final EventLoopGroup workerGroup = new NioEventLoopGroup(1, new DefaultThreadFactory("MannitolIRCWorker"));
    public final AtomicBoolean isReconnecting = new AtomicBoolean(false);
    public final AtomicBoolean isConnecting = new AtomicBoolean(false);
    public final AtomicBoolean wasLogged = new AtomicBoolean(false);

    @Getter
    private final String hardwareId;

    public IRCUser ircUser;
    private Channel channel;

    public IRCClient() throws Exception {
        hardwareId = generateHardwareId();
    }

    public void connect() {
        this.connect(null, null);
    }

    public void connect(String autoSendUsername, String autoSendPassword) {
        if (isChannelOpen() || isConnecting.get()) {
            return;
        }

        isConnecting.set(true);

        final Bootstrap bootstrap = new Bootstrap()
                .group(workerGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) {
                        final SecretKeySpec aesKey = new SecretKeySpec(
                                Base64.getDecoder().decode("DLqK504BtlJIyZv5dnnT1w=="),
                                "AES"
                        );

                        ch.pipeline().addLast("frame_decoder", new FrameDecoder())
                                .addLast("aes_decoder", new AESDecoder(aesKey))
                                .addLast("frame_encoder", new FrameEncoder())
                                .addLast("aes_encoder", new AESEncoder(aesKey))
                                .addLast("irc_handler", new IRCHandler(IRCClient.this, autoSendUsername, autoSendPassword));
                    }
                });

        bootstrap.connect("7pvz58jb.svipcdn.cn", 18962)
                 .addListener((ChannelFutureListener) this::onConnectionStatus);
    }

    private void onConnectionStatus(ChannelFuture future) {
        isConnecting.set(false);

        if (future.isSuccess()) {
            channel = future.channel();
        } else {
            Helper.sendSystemDeBug("网络连接错误...", Helper.debug.C);
            System.exit(-1);
        }
    }

    public void shutdown() {
        closeChannel();
        workerGroup.shutdownGracefully();
    }

    public ChannelFuture sendPacket(IRCProto.IRCPacket packet) {
        if (channel == null || !isChannelOpen()) {

            return null;
        }

        final ByteBuf buf = channel.alloc().buffer();

        buf.writeInt(packet.getId());

        if (packet.getWriteFunction() != null) {
            packet.getWriteFunction().accept(buf);
        }

        return channel.writeAndFlush(buf).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
    }

    public boolean isChannelOpen() {
        if (channel == null) {
            return false;
        }

        if (!channel.isOpen()) return false;

        return channel != null && channel.isOpen();
    }

    public void closeChannel() {
        if (channel != null) {
            channel.close();
            channel = null;
        }
    }

    public static String generateHardwareId() throws Exception {
        final MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        final SystemInfo systemInfo = new SystemInfo();
        final CentralProcessor processor = systemInfo.getHardware().getProcessor();
        final String input = processor.getProcessorIdentifier().getName()
                + ":" + processor.getProcessorIdentifier().getIdentifier()
                + ":" + systemInfo.getOperatingSystem().getFamily();
        final byte[] digest = messageDigest.digest(input.getBytes(StandardCharsets.UTF_8));
        final StringBuilder digestSB = new StringBuilder();

        for (byte b : digest) {
            final String hexString = Integer.toHexString(b & 0xFF);

            if (hexString.length() == 1) {
                digestSB.append('0').append(hexString);
            } else {
                digestSB.append(hexString);
            }
        }

        return digestSB.toString();
    }
}
