package mezz.jei.forge.platform;

import mezz.jei.common.platform.IPlatformRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.RegistryManager;

import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class RegistryWrapper<T> implements IPlatformRegistry<T> {
    public static <T, V> IPlatformRegistry<T> getRegistry(ResourceKey<? extends Registry<T>> key) {
        ForgeRegistry<V> registry = RegistryManager.ACTIVE.getRegistry(key.location());
        IPlatformRegistry<V> registryWrapper = new RegistryWrapper<>(registry);
        @SuppressWarnings("unchecked")
        IPlatformRegistry<T> castRegistry = (IPlatformRegistry<T>) registryWrapper;
        return castRegistry;
    }

    private final ForgeRegistry<T> forgeRegistry;

    private RegistryWrapper(ForgeRegistry<T> forgeRegistry) {
        this.forgeRegistry = forgeRegistry;
    }

    @Override
    public Stream<T> getValues() {
        return StreamSupport.stream(this.forgeRegistry.spliterator(), false);
    }

    @Override
    public Optional<T> getValue(ResourceLocation resourceLocation) {
        T value = this.forgeRegistry.getValue(resourceLocation);
        return Optional.ofNullable(value);
    }

    @Override
    public int getId(T entry) {
        return this.forgeRegistry.getID(entry);
    }

    @Override
    public Optional<T> getValue(int id) {
        T value = this.forgeRegistry.getValue(id);
        return Optional.ofNullable(value);
    }

    @Override
    public boolean contains(T entry) {
        return this.forgeRegistry.containsValue(entry);
    }

    @Override
    public Optional<ResourceLocation> getRegistryName(T entry) {
        return this.forgeRegistry.getResourceKey(entry)
            .map(ResourceKey::location);
    }
}
