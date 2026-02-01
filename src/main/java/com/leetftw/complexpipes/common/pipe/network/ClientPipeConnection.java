package com.leetftw.complexpipes.common.pipe.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.Direction;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record ClientPipeConnection(Direction side, PipeConnectionMode mode) {
    public static final StreamCodec<ByteBuf, ClientPipeConnection> STREAM_CODEC = StreamCodec.composite(
            Direction.STREAM_CODEC,
            ClientPipeConnection::side,
            ByteBufCodecs.idMapper(ordinal -> PipeConnectionMode.values()[ordinal], PipeConnectionMode::ordinal),
            ClientPipeConnection::mode,
            ClientPipeConnection::new
    );
}
