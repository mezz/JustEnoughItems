package mezz.jei.test;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import mezz.jei.api.recipe.transfer.RecipeTransferResult;
import mezz.jei.common.network.packets.PacketRecipeTransferResult;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class RecipeTransferResultPacketTest {
	@ParameterizedTest
	@CsvSource({"SUCCESS, 1", "REJECTED, 0"})
	public void resultPacketsPreserveCorrelationIdsAndResults(RecipeTransferResult result, int resultByte) {
		PacketRecipeTransferResult packet = new PacketRecipeTransferResult(42, result == RecipeTransferResult.SUCCESS);
		ByteBuf byteBuf = Unpooled.buffer();
		try {
			RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(byteBuf, RegistryAccess.EMPTY);
			PacketRecipeTransferResult.STREAM_CODEC.encode(buffer, packet);
			assertArrayEquals(new byte[]{42, (byte) resultByte}, ByteBufUtil.getBytes(byteBuf));

			PacketRecipeTransferResult decoded = PacketRecipeTransferResult.STREAM_CODEC.decode(buffer);
			assertEquals(packet.transferId, decoded.transferId);
			assertEquals(packet.result, decoded.result);
			assertFalse(buffer.isReadable());
		} finally {
			byteBuf.release();
		}
	}
}
