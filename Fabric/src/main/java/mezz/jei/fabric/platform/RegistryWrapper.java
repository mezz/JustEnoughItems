package mezz.jei.fabric.platform;

import mezz.jei.common.platform.IPlatformRegistry;
import net.fabricmc.fabric.mixin.registry.sync.AccessorRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.stream.Stream;

public class RegistryWrapper<T> implements IPlatformRegistry<T> {
    public static <T> IPlatformRegistry<T> getRegistry(ResourceKey<? extends Registry<T>> key) {
        WritableRegistry<WritableRegistry<?>> rootRegistry = AccessorRegistry.getROOT();
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

    @Nullable
    @Override
    public T getValue(ResourceLocation resourceLocation) {
        return this.registry.get(resourceLocation);
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
    @Nullable
    public ResourceLocation getRegistryName(T entry) {
        return this.registry.getKey(entry);
    }
}
