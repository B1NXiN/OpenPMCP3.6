package dev.yalan.irc.netty;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import dev.yalan.irc.IRCClient;
import dev.yalan.irc.IRCUser;
import dev.yalan.irc.events.IRCAuthenticationResultEvent;
import dev.yalan.irc.events.IRCGeneralMessageEvent;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import jnic.JNICInclude;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pmcp.PMCP;
import pmcp.event.EventManager;
import pmcp.utils.client.Helper;

import java.util.HashMap;
import java.util.UUID;
import java.util.function.BiConsumer;

@JNICInclude
public class IRCHandler extends SimpleChannelInboundHandler<ByteBuf> {
    private static final Logger logger = LogManager.getLogger("IRCHandler");
    private static final Minecraft mc = Minecraft.getInstance();
    private final HashMap<Integer, BiConsumer<ChannelHandlerContext, ByteBuf>> functionMap = new HashMap<>();
    private final IRCClient ircClient;
    private final String username;
    private final String password;

    private boolean isProtocolVersionNotChecked = true;

    public IRCHandler(IRCClient ircClient, String username, String password) {
        this.ircClient = ircClient;
        this.username = username;
        this.password = password;

        this.functionMap.put(0, this::handleAlive);
        this.functionMap.put(1, this::handleGeneralMessage);
        this.functionMap.put(2, this::handleClientSetting);
        this.functionMap.put(3, this::handleAuthenticationResult);
        this.functionMap.put(4, this::handlePersonalUserInformation);
        this.functionMap.put(5, this::handleQueriedMinecraftProfile);
        this.functionMap.put(6, this::handleChat);
        this.functionMap.put(7, this::handleKickMC);
    }

    private void handleAlive(ChannelHandlerContext ctx, ByteBuf buf) {
        ircClient.sendPacket(IRCProto.createAlive());
    }

    private void handleGeneralMessage(ChannelHandlerContext ctx, ByteBuf buf) {
        final String message = IRCProto.readUTF(buf);

        mc.execute(() -> EventManager.instance.call(new IRCGeneralMessageEvent(message)));
    }

    private void handleClientSetting(ChannelHandlerContext ctx, ByteBuf buf) {
        final String version = IRCProto.readUTF(buf);

        if (!ircClient.wasLogged.get()) {
            mc.execute(() -> {
                if (!PMCP.Instance.clientVersion.equals(version)) {
                    logger.info("Client out of date Current({}) Server({})", PMCP.Instance.clientVersion, version);
                    mc.stop();
                }
            });
        }
    }

    private void handleAuthenticationResult(ChannelHandlerContext ctx, ByteBuf buf) {
        final int result = buf.readInt();
        final String message = IRCProto.readUTF(buf);

        mc.execute(() -> EventManager.instance.call(new IRCAuthenticationResultEvent(result, message)));
    }

    private void handlePersonalUserInformation(ChannelHandlerContext ctx, ByteBuf buf) {
        final String username = IRCProto.readUTF(buf);
        final String rank = IRCProto.readUTF(buf);

        mc.execute(() -> ircClient.ircUser = new IRCUser(PMCP.Instance.getClientName(), username, rank));
    }

    private void handleQueriedMinecraftProfile(ChannelHandlerContext ctx, ByteBuf buf) {
        final byte mcType = buf.readByte();
        final UUID mcUUID = IRCProto.readUUID(buf);
        final String clientName = IRCProto.readUTF(buf);
        final JsonObject payload = JsonParser.parseString(IRCProto.readUTF(buf)).getAsJsonObject();

        final IRCUser ircUser;

        if (PMCP.Instance.clientName.equals(clientName)) {
            ircUser = new IRCUser(clientName, payload.get("username").getAsString(), payload.get("rank").getAsString());
        } else {
            return;
        }

        switch (mcType) {
            case 0 -> {
                if (mc.level != null) {
                    final Player player = mc.level.getPlayerByUUID(mcUUID);

                    if (player != null) {
                        player.ircUser = ircUser;
                    }
                }
            }
            case 1 -> {
                if (mc.getConnection() != null) {
                    final PlayerInfo playerInfo = mc.getConnection().getPlayerInfo(mcUUID);

                    if (playerInfo != null) {
                        playerInfo.ircUser = ircUser;
                    }
                }
            }
            default -> {
                logger.error("Unknown mc type({})", mcType);
            }
        }
    }

    private void handleChat(ChannelHandlerContext ctx, ByteBuf buf) {
        final String channelName = IRCProto.readUTF(buf);
        final JsonObject payload = JsonParser.parseString(IRCProto.readUTF(buf)).getAsJsonObject();
        final String message = IRCProto.readUTF(buf);

        switch (channelName) {
            case "ServerLog" -> {
                mc.execute(() -> {
                    if (mc.level != null) {
                        mc.gui.getChat().addMessage(Component.literal(String.format(
                                "%s[%sIRC%s] [%sServer%s]: %s%s",
                                ChatFormatting.GRAY,
                                ChatFormatting.RED,
                                ChatFormatting.GRAY,
                                ChatFormatting.AQUA,
                                ChatFormatting.GRAY,
                                ChatFormatting.RESET,
                                message
                        )));
                    }
                });
            }
            case "UserConnectionStatus" -> {
                final String clientName = payload.get("clientName").getAsString();

                if (PMCP.Instance.clientName.equals(clientName)) {
                    final JsonObject userPayload = JsonParser.parseString(payload.get("userPayload").getAsString()).getAsJsonObject();
                    final String username = userPayload.get("username").getAsString();
                    final String rank = userPayload.get("rank").getAsString();
                    final String status = payload.get("status").getAsString();

                    mc.execute(() -> {
                        if (mc.level != null) {
                            mc.gui.getChat().addMessage(Component.literal(String.format(
                                    "%s[%sIRC%s] [%s" + PMCP.Instance.clientName + "%s] %s%s%s(%s%s%s) %s%s now",
                                    ChatFormatting.GRAY,
                                    ChatFormatting.RED,
                                    ChatFormatting.GRAY,
                                    ChatFormatting.LIGHT_PURPLE,
                                    ChatFormatting.GRAY,
                                    ChatFormatting.RESET,
                                    username,
                                    ChatFormatting.GRAY,
                                    ChatFormatting.RESET,
                                    rank,
                                    ChatFormatting.GRAY,
                                    ChatFormatting.RESET,
                                    status
                            )));
                        }
                    });
                }
            }
            case "Public" -> {
                final String clientName = payload.get("clientName").getAsString();

                if (PMCP.Instance.clientName.equals(clientName)) {
                    final String username = payload.get("username").getAsString();
                    final String rank = payload.get("rank").getAsString();

                    mc.execute(() -> {
                        if (mc.level != null) {
                            mc.gui.getChat().addMessage(Component.literal(String.format(
                                    "%s[%sIRC%s] [%s" + PMCP.Instance.clientName + "%s] %s%s%s(%s%s%s): %s%s",
                                    ChatFormatting.GRAY,
                                    ChatFormatting.RED,
                                    ChatFormatting.GRAY,
                                    ChatFormatting.LIGHT_PURPLE,
                                    ChatFormatting.GRAY,
                                    ChatFormatting.RESET,
                                    username,
                                    ChatFormatting.GRAY,
                                    ChatFormatting.RESET,
                                    rank,
                                    ChatFormatting.GRAY,
                                    ChatFormatting.RESET,
                                    message
                            )));
                        }
                    });
                }
            }
            default -> logger.error("Unknown chat channel name: {}", channelName);
        }
    }

    private void handleKickMC(ChannelHandlerContext ctx, ByteBuf buf) {
        final String who = IRCProto.readUTF(buf);
        final String reason = IRCProto.readUTF(buf);

        if (!mc.isSingleplayer() && mc.getConnection() != null) {
            mc.getConnection().onDisconnect(Component.literal("你被" + who + "踢出了! 原因: " + reason));
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf buf) throws Exception {
        if (isProtocolVersionNotChecked) {
            isProtocolVersionNotChecked = false;

            final int serverProtocolVersion = buf.readInt();

            if (serverProtocolVersion == IRCProto.PROTOCOL_VERSION) {
                final String verifyString = "C11H10O";

                ircClient.sendPacket(IRCProto.createHello(System.currentTimeMillis(), verifyString)).syncUninterruptibly();

                if (username != null && password != null) {
                    ircClient.sendPacket(IRCProto.createAuthentication(username, password, ircClient.getHardwareId()));
                }
            } else if (!ircClient.wasLogged.get()) {
                ircClient.closeChannel();
            }

            return;
        }

        if (buf.readableBytes() < 4) {
            throw new Exception("Can't read irc packet because readableBytes less than 4 bytes");
        }

        final Integer id = buf.readInt();
        final BiConsumer<ChannelHandlerContext, ByteBuf> handleFunction = functionMap.get(id);

        if (handleFunction == null) {
            throw new Exception("PacketId(" + id + ") was not found in map");
        } else {
            handleFunction.accept(ctx, buf);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        if (ircClient.isReconnecting.get()) {
            ircClient.isReconnecting.set(false);

            mc.execute(() -> {

                Helper.sendSystemDeBug("IRC重连成功！", Helper.debug.A);

                if (mc.level != null) {
                    Helper.sendMessage("重连成功!");

                    if (mc.player != null) {
                        ircClient.sendPacket(IRCProto.createUpdatePersonalMinecraftProfile(mc.player.getUUID()));
                    }
                }
            });
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("Error occurred while handling IRC Service", cause);

        if (ircClient.isChannelOpen()) {
            ircClient.closeChannel();
        }
    }
}
