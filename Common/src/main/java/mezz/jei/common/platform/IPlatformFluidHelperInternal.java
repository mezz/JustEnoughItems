package mezz.jei.common.platform;

import mezz.jei.api.helpers.IPlatformFluidHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

import java.util.Optional;

public interface IPlatformFluidHelperInternal<T> extends IPlatformFluidHelper<T> {

    IIngredientSubtypeInterpreter<T> getAllNbtSubtypeInterpreter();

    IIngredientRenderer<T> createRenderer(long capacity, boolean showCapacity, int width, int height);

    TextureAtlasSprite getStillFluidSprite(T ingredient);

    Component getDisplayName(T ingredient);

    int getColorTint(T ingredient);

    long getAmount(T ingredient);

    Optional<CompoundTag> getTag(T ingredient);

    T copy(T ingredient);

    T normalize(T ingredient);
}
