package mezz.jei.test.lib;

import net.minecraft.server.Bootstrap;

public final class ForgeTestBootstrap {
	private ForgeTestBootstrap() {

	}

	public static void bootStrap() {
		try {
			Bootstrap.bootStrap();
		} catch (ExceptionInInitializerError error) {
			// Forge's runtime event transformer supplies this constructor, but plain unit tests run without that transformer.
			// Vanilla registry bootstrap has completed before Forge reaches the network initialization that needs it.
			if (!isMissingTransformedNetworkEventConstructor(error)) {
				throw error;
			}
		}
	}

	private static boolean isMissingTransformedNetworkEventConstructor(Throwable throwable) {
		for (Throwable cause = throwable; cause != null; cause = cause.getCause()) {
			if (cause instanceof NoSuchMethodException && cause.getMessage().equals("net.minecraftforge.network.NetworkEvent.<init>()")) {
				return true;
			}
		}
		return false;
	}
}
