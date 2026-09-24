package mezz.jei.common.network.packets;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import mezz.jei.common.network.packets.legacy.PacketRecipeTransfer;
import mezz.jei.common.network.packets.legacy.PacketRecipeTransferCounted;
import mezz.jei.common.transfer.TransferOperation;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class PacketRecipeTransferCodecTest {
	@Test
	public void correlationIdsRoundTripInResultPackets() {
		PacketRecipeTransferWithResult packet = new PacketRecipeTransferWithResult(
			List.of(new TransferOperation(3, 4)),
			List.of(4),
			List.of(3),
			false,
			true,
			42
		);
		PacketRecipeTransferCountedWithResult countedPacket = new PacketRecipeTransferCountedWithResult(
			List.of(new TransferOperation(3, 4, 2)),
			List.of(4),
			List.of(3),
			true,
			false,
			43
		);

		assertRoundTrip(PacketRecipeTransferWithResult.STREAM_CODEC, packet);
		assertRoundTrip(PacketRecipeTransferCountedWithResult.STREAM_CODEC, countedPacket);
	}

	@Test
	public void legacyPacketsKeepOriginalPayloadIdsAndEncoding() {
		PacketRecipeTransfer packet = new PacketRecipeTransfer(
			List.of(new TransferOperation(3, 4)),
			List.of(4),
			List.of(3),
			false,
			true
		);
		PacketRecipeTransferCounted countedPacket = new PacketRecipeTransferCounted(
			List.of(new TransferOperation(3, 4, 2)),
			List.of(4),
			List.of(3),
			true,
			false
		);

		assertEquals(Identifier.fromNamespaceAndPath("jei", "recipe_transfer"), PacketRecipeTransfer.TYPE.id());
		assertEquals(Identifier.fromNamespaceAndPath("jei", "recipe_transfer_counted"), PacketRecipeTransferCounted.TYPE.id());
		assertArrayEquals(new byte[]{1, 3, 4, 1, 4, 1, 3, 0, 1}, encode(PacketRecipeTransfer.STREAM_CODEC, packet));
		assertArrayEquals(new byte[]{1, 3, 4, 2, 1, 4, 1, 3, 1, 0}, encode(PacketRecipeTransferCounted.STREAM_CODEC, countedPacket));
	}

	private static <T> void assertRoundTrip(StreamCodec<RegistryFriendlyByteBuf, T> codec, T value) {
		byte[] encoded = encode(codec, value);
		ByteBuf byteBuf = Unpooled.wrappedBuffer(encoded);
		try {
			RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(byteBuf, RegistryAccess.EMPTY);
			T decoded = codec.decode(buffer);
			assertFalse(buffer.isReadable());
			assertArrayEquals(encoded, encode(codec, decoded));
		} finally {
			byteBuf.release();
		}
	}

	private static <T> byte[] encode(StreamCodec<RegistryFriendlyByteBuf, T> codec, T value) {
		ByteBuf byteBuf = Unpooled.buffer();
		try {
			RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(byteBuf, RegistryAccess.EMPTY);
			codec.encode(buffer, value);
			return ByteBufUtil.getBytes(byteBuf);
		} finally {
			byteBuf.release();
		}
	}
}
