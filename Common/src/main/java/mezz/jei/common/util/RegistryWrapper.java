package mezz.jei.common.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;
import java.util.stream.Stream;

public class RegistryWrapper<T> {
	public static <T> RegistryWrapper<T> getRegistry(ResourceKey<? extends Registry<T>> key) {
		Minecraft minecraft = Minecraft.getInstance();
		ClientLevel level = minecraft.level;
		if (level == null) {
			throw new IllegalStateException("Could not get registry, registry access is unavailable because the level is currently null");
		}
		RegistryAccess registryAccess = level.registryAccess();
		Registry<T> registry = registryAccess.registryOrThrow(key);
		return new RegistryWrapper<>(registry);
	}

	private final Registry<T> registry;

	private RegistryWrapper(Registry<T> registry) {
		this.registry = registry;
	}

	public Stream<T> getValues() {
		return this.registry.stream();
	}

	public Stream<Holder.Reference<T>> getHolderStream() {
		return this.registry.holders();
	}

	public boolean contains(T entry) {
		return this.registry.getKey(entry) != null;
	}

	public Optional<ResourceLocation> getRegistryName(T entry) {
		return this.registry.getResourceKey(entry)
			.map(ResourceKey::location);
	}
}
