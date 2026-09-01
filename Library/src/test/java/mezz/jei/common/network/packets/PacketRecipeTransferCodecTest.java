package mezz.jei.common.network.packets;

import mezz.jei.common.Constants;
import mezz.jei.common.network.PacketIdClient;
import mezz.jei.common.network.PacketIdServer;
import net.minecraft.network.FriendlyByteBuf;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PacketRecipeTransferCodecTest {
	@Test
	public void resultPacketsUseFeatureChannelAndEncodeCorrelationIds() {
		PacketRecipeTransferResult packet = new PacketRecipeTransferResult(42, true);
		assertEquals(Constants.RECIPE_TRANSFER_RESULT_CHANNEL_ID, packet.getChannelId());

		Pair<FriendlyByteBuf, Integer> packetData = packet.getPacketData();
		FriendlyByteBuf buffer = packetData.getLeft();
		try {
			assertEquals(PacketIdClient.RECIPE_TRANSFER_RESULT.ordinal(), buffer.readUnsignedByte());
			assertEquals(42, buffer.readVarInt());
			assertTrue(buffer.readBoolean());
			assertFalse(buffer.isReadable());
		} finally {
			buffer.release();
		}
	}

	@Test
	public void resultPacketIdsAreAppendedAfterLegacyRecipeTransferIds() {
		assertTrue(PacketIdServer.RECIPE_TRANSFER_WITH_RESULT.ordinal() > PacketIdServer.RECIPE_TRANSFER.ordinal());
		assertTrue(PacketIdServer.RECIPE_TRANSFER_COUNTED_WITH_RESULT.ordinal() > PacketIdServer.RECIPE_TRANSFER_COUNTED.ordinal());
	}
}
