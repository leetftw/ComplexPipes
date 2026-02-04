package com.leetftw.complexpipes.common.network;

import com.leetftw.complexpipes.common.blocks.PipeBlockEntity;
import com.leetftw.complexpipes.common.pipe.network.PipeConnection;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

import static com.leetftw.complexpipes.common.ComplexPipes.MODID;

public record PipeScreenNumericSyncPayload(ResourceKey<Level> dimension, BlockPos position, Direction side, int priority, int ratio) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<PipeScreenNumericSyncPayload> TYPE = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(MODID, "sync_numeric_from_screen"));

    public static final StreamCodec<ByteBuf, PipeScreenNumericSyncPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.fromCodec(Level.RESOURCE_KEY_CODEC),
            PipeScreenNumericSyncPayload::dimension,
            BlockPos.STREAM_CODEC,
            PipeScreenNumericSyncPayload::position,
            Direction.STREAM_CODEC,
            PipeScreenNumericSyncPayload::side,
            ByteBufCodecs.INT,
            PipeScreenNumericSyncPayload::priority,
            ByteBufCodecs.INT,
            PipeScreenNumericSyncPayload::ratio,
            PipeScreenNumericSyncPayload::new
    );

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handleReceivedOnServer(IPayloadContext iPayloadContext) {
        Level level = iPayloadContext.player().level();

        if (!level.isLoaded(position))
            return;

        BlockEntity be = level.getBlockEntity(position);
        if (!(be instanceof PipeBlockEntity pipeBE))
            return;

        Optional<PipeConnection> connectionOptional = pipeBE.getConnectionForSide(side);
        if (connectionOptional.isEmpty())
            return;

        PipeConnection connection = connectionOptional.get();
        if (Math.abs(priority) < 10000)
            connection.setRatio(priority);
        if (Math.abs(ratio) < 10000)
            connection.setRatio(ratio);
    }
}
