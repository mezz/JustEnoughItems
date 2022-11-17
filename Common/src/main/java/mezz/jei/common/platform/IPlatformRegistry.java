package mezz.jei.common.platform;

import net.minecraft.resources.ResourceLocation;

import java.util.Optional;
import java.util.stream.Stream;

public interface IPlatformRegistry<T> {
    Stream<T> getValues();

    Optional<T> getValue(ResourceLocation resourceLocation);

    int getId(T entry);

    Optional<T> getValue(int id);

    boolean contains(T entry);

    Optional<ResourceLocation> getRegistryName(T entry);
}
