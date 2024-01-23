package mezz.jei.neoforge.platform;

import mezz.jei.common.platform.IPlatformRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;
import java.util.stream.Stream;

public class RegistryWrapper<T> implements IPlatformRegistry<T> {
    public static <T> IPlatformRegistry<T> getRegistry(ResourceKey<? extends Registry<T>> key) {
        Registry<? extends Registry<?>> rootRegistry = BuiltInRegistries.REGISTRY;
        Registry<?> registry = rootRegistry.get(key.location());
        if (registry == null) {
            throw new NullPointerException("Could not find registry for key: " + key);
        }
        IPlatformRegistry<?> registryWrapper = new RegistryWrapper<>(registry);
        @SuppressWarnings("unchecked")
        IPlatformRegistry<T> castPlatformRegistry = (IPlatformRegistry<T>) registryWrapper;
        return castPlatformRegistry;
    }

    private final Registry<T> registry;

    private RegistryWrapper(Registry<T> registry) {
        this.registry = registry;
    }

    @Override
    public Stream<T> getValues() {
        return this.registry.stream();
    }

    @Override
    public Optional<T> getValue(ResourceLocation resourceLocation) {
        T t = this.registry.get(resourceLocation);
        return Optional.ofNullable(t);
    }

    @Override
    public int getId(T entry) {
        return this.registry.getId(entry);
    }

    @Override
    public Optional<T> getValue(int id) {
        return this.registry.getHolder(id)
                .map(Holder::value);
    }

    @Override
    public boolean contains(T entry) {
        return this.registry.getKey(entry) != null;
    }

    @Override
    public Optional<ResourceLocation> getRegistryName(T entry) {
        return this.registry.getResourceKey(entry)
                .map(ResourceKey::location);
    }
}
