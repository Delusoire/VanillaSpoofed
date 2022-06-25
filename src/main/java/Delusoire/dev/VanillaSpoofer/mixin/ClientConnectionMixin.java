package Delusoire.dev.VanillaSpoofer.mixin;

import io.netty.buffer.Unpooled;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.fabricmc.fabric.mixin.networking.accessor.CustomPayloadC2SPacketAccessor;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConnection.class)
public abstract class ClientConnectionMixin
        extends SimpleChannelInboundHandler<Packet<?>> {
    @ModifyVariable(
            method = "send(Lnet/minecraft/network/Packet;Lio/netty/util/concurrent/GenericFutureListener;)V",
            at = @At("HEAD"))
    public Packet<?> onSendPacket(Packet<?> packet) {
        if (packet instanceof CustomPayloadC2SPacketAccessor) {
            String namespace = ((CustomPayloadC2SPacketAccessor) packet).getChannel().getNamespace();
            String path = ((CustomPayloadC2SPacketAccessor) packet).getChannel().getPath();
            if (namespace.equals("minecraft") && path.equals("brand"))
                return new CustomPayloadC2SPacket(
                        CustomPayloadC2SPacket.BRAND,
                        new PacketByteBuf(Unpooled.buffer()).writeString("vanilla"));
        }
        return packet;
    }

    @Inject(at = {@At(value = "HEAD")},
            method = {
                    "send(Lnet/minecraft/network/Packet;Lio/netty/util/concurrent/GenericFutureListener;)V"},
            cancellable = true)
    private void onSendPacket(Packet<?> packet,
                              GenericFutureListener<? extends Future<? super Void>> callback,
                              CallbackInfo ci) {
        if (packet instanceof CustomPayloadC2SPacketAccessor) {
            String namespace = ((CustomPayloadC2SPacketAccessor) packet).getChannel().getNamespace();
            String path = ((CustomPayloadC2SPacketAccessor) packet).getChannel().getPath();
            if ((namespace.equals("minecraft") && path.equals("register")) || namespace.equals("fabric"))
                ci.cancel();
        }
    }
}