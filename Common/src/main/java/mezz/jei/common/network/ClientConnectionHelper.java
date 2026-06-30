package mezz.jei.common.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

public final class ClientConnectionHelper {
	private static final String UNKNOWN_SERVER_BRAND = "unknown";

	private ClientConnectionHelper() {
	}

	public static String getServerBrand() {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null) {
			return UNKNOWN_SERVER_BRAND;
		}
		String serverBrand = player.getServerBrand();
		if (serverBrand == null || serverBrand.isBlank()) {
			return UNKNOWN_SERVER_BRAND;
		}
		return serverBrand;
	}

	public static boolean hasServerBrand(String expectedBrand) {
		for (String serverBrand : getServerBrand().split(",")) {
			if (serverBrand.trim().equalsIgnoreCase(expectedBrand)) {
				return true;
			}
		}
		return false;
	}
}
