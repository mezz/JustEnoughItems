package mezz.jei.common.platform;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public interface IPlatformRegistry<T> {
    Collection<T> getValues();

    @Nullable
    T getValue(ResourceLocation resourceLocation);

    int getId(T entry);

    T getValue(int id);

    boolean contains(T entry);
}
