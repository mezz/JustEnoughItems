package mezz.jei.forge.platform;

import mezz.jei.common.platform.IPlatformRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.registries.RegistryManager;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Optional;

public class ForgeRegistryWrapper<T extends IForgeRegistryEntry<T>> implements IPlatformRegistry<T> {
    public static <T, V extends IForgeRegistryEntry<V>> IPlatformRegistry<T> getRegistry(ResourceKey<? extends Registry<T>> key) {
        ForgeRegistry<V> registry = RegistryManager.ACTIVE.getRegistry(key.location());
        IPlatformRegistry<V> registryWrapper = new ForgeRegistryWrapper<>(registry);
        @SuppressWarnings("unchecked")
        IPlatformRegistry<T> castRegistry = (IPlatformRegistry<T>) registryWrapper;
        return castRegistry;
    }

    private final ForgeRegistry<T> forgeRegistry;

    private ForgeRegistryWrapper(ForgeRegistry<T> forgeRegistry) {
        this.forgeRegistry = forgeRegistry;
    }

    @Override
    public Collection<T> getValues() {
        return this.forgeRegistry.getValues();
    }

    @Nullable
    @Override
    public T getValue(ResourceLocation resourceLocation) {
        return this.forgeRegistry.getValue(resourceLocation);
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
}
