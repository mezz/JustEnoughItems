package mezz.jei.common.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.repository.PackRepository;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public final class DarkModeResourcePack {
	public static final String RESOURCE_PACK_NAME = "dark_mode";
	public static final String RESOURCE_PACK_PATH = "resourcepacks/" + RESOURCE_PACK_NAME;
	public static final String FABRIC_PACK_ID = "jei:dark_mode";
	public static final String NEOFORGE_PACK_ID = "mod/jei:resourcepacks/dark_mode";

	private static final List<Consumer<Boolean>> listeners = new CopyOnWriteArrayList<>();

	private DarkModeResourcePack() {

	}

	public static boolean isEnabled() {
		PackRepository repository = Minecraft.getInstance().getResourcePackRepository();
		String packId = getPackId(repository);
		return repository.getSelectedIds().contains(packId);
	}

	public static void setEnabled(boolean enabled) {
		Minecraft minecraft = Minecraft.getInstance();
		PackRepository repository = minecraft.getResourcePackRepository();
		String packId = getPackId(repository);
		boolean changed;
		if (enabled) {
			changed = repository.addPack(packId);
		} else {
			changed = repository.removePack(packId);
		}
		if (changed) {
			minecraft.options.updateResourcePacks(repository);
			listeners.forEach(listener -> listener.accept(enabled));
		}
	}

	public static Runnable addListener(Consumer<Boolean> listener) {
		listeners.add(listener);
		return () -> listeners.remove(listener);
	}

	private static String getPackId(PackRepository repository) {
		for (String packId : List.of(FABRIC_PACK_ID, NEOFORGE_PACK_ID)) {
			if (repository.getPack(packId) != null) {
				return packId;
			}
		}
		throw new IllegalStateException("JEI's dark mode resource pack is not registered");
	}
}
