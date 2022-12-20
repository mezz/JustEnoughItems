package mezz.jei.fabric.platform;

import mezz.jei.common.platform.IPlatformRegistry;
import net.fabricmc.fabric.mixin.registry.sync.RegistryAccessor;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;
import java.util.stream.Stream;

public class RegistryWrapper<T> implements IPlatformRegistry<T> {
    public static <T> IPlatformRegistry<T> getRegistry(ResourceKey<? extends Registry<T>> key) {
        WritableRegistry<WritableRegistry<?>> rootRegistry = RegistryAccessor.getROOT();
        WritableRegistry<?> registry = rootRegistry.get(key.location());
        IPlatformRegistry<?> registryWrapper = new RegistryWrapper<>(registry);
        @SuppressWarnings("unchecked")
        IPlatformRegistry<T> castPlatformRegistry = (IPlatformRegistry<T>) registryWrapper;
        return castPlatformRegistry;
    }

    private final WritableRegistry<T> registry;

    private RegistryWrapper(WritableRegistry<T> registry) {
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
        return this.registry.getHolder(id).map(Holder::value);
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
