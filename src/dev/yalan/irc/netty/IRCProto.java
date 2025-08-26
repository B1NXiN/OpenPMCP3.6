package dev.yalan.irc.netty;

import io.netty.buffer.ByteBuf;
import jnic.JNICInclude;
import lombok.Getter;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.function.Consumer;

@JNICInclude
public class IRCProto {
    public static final UUID NIL_NULL = new UUID(0L, 0L);
    public static final int PROTOCOL_VERSION = 1;

    public static IRCPacket createAlive() {
        return new IRCPacket(0);
    }

    public static IRCPacket createHello(long lt, String verifyString) {
        return new IRCPacket(1, buf -> {
            buf.writeLong(lt);
            writeUTF(buf, verifyString);
        });
    }

    public static IRCPacket createAuthentication(String username, String password, String hardwareId) {
        return new IRCPacket(2, buf -> {
            writeUTF(buf, username);
            writeUTF(buf, password);
            writeUTF(buf, hardwareId);
        });
    }

    public static IRCPacket createUpdatePersonalMinecraftProfile(UUID mcUUID) {
        return new IRCPacket(3, buf -> writeUUID(buf, mcUUID));
    }

    public static IRCPacket createQueryMinecraftProfile(int mcType, UUID mcUUID) {
        return new IRCPacket(4, buf -> {
            buf.writeInt(mcType);
            writeUUID(buf, mcUUID);
        });
    }

    public static IRCPacket createChat(String message) {
        return new IRCPacket(5, buf -> writeUTF(buf, message));
    }

    public static IRCPacket createKickMC(String username, String reason) {
        return new IRCPacket(6, buf -> {
            writeUTF(buf, username);
            writeUTF(buf, reason);
        });
    }

    public static void writeUTF(ByteBuf buf, String s) {
        final byte[] bytes = s.getBytes(StandardCharsets.UTF_8);

        buf.writeInt(bytes.length);
        buf.writeBytes(bytes);
    }

    public static void writeUUID(ByteBuf buf, UUID uuid) {
        buf.writeLong(uuid.getMostSignificantBits());
        buf.writeLong(uuid.getLeastSignificantBits());
    }

    public static String readUTF(ByteBuf buf) {
        if (buf.readableBytes() < 4) {
            throw new IllegalStateException("Can't read utf bytes length because readableBytes less than 4 bytes");
        }

        final int len = buf.readInt();

        if (len < 0) {
            throw new IllegalStateException("Can't read utf bytes because length less than 0 byte");
        }

        final byte[] bArray = new byte[len];
        buf.readBytes(bArray);

        return new String(bArray, StandardCharsets.UTF_8);
    }

    public static UUID readUUID(ByteBuf buf) {
        return new UUID(buf.readLong(), buf.readLong());
    }

    @Getter
    public static class IRCPacket {
        private final int id;
        private final Consumer<ByteBuf> writeFunction;

        public IRCPacket(int id) {
            this.id = id;
            this.writeFunction = null;
        }

        public IRCPacket(int id, Consumer<ByteBuf> writeFunction) {
            this.id = id;
            this.writeFunction = writeFunction;
        }
    }
}
