package com.leetftw.complexpipes.common.network;

import com.leetftw.complexpipes.common.pipe.network.ClientPipeConnection;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;

import java.util.List;

import static com.leetftw.complexpipes.common.ComplexPipes.MODID;

public record PipeSyncPayload(ResourceKey<Level> dimension, BlockPos position, List<ClientPipeConnection> connections) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<PipeSyncPayload> TYPE = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(MODID, "sync_pipe"));

    // Each pair of elements defines the stream codec of the element to encode/decode and the getter for the element to encode
    // 'name' will be encoded and decoded as a string
    // 'age' will be encoded and decoded as an integer
    // The final parameter takes in the previous parameters in the order they are provided to construct the payload object
    public static final StreamCodec<ByteBuf, PipeSyncPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.fromCodec(Level.RESOURCE_KEY_CODEC),
            PipeSyncPayload::dimension,
            BlockPos.STREAM_CODEC,
            PipeSyncPayload::position,
            ClientPipeConnection.STREAM_CODEC.apply(ByteBufCodecs.list()),
            PipeSyncPayload::connections,
            PipeSyncPayload::new
    );

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
