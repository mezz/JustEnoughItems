package mezz.jei.common.platform;

import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientTypeWithSubtypes;
import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.material.Fluid;

public interface IPlatformFluidHelper<T> {
    IIngredientTypeWithSubtypes<Fluid, T> getFluidIngredientType();

    IIngredientSubtypeInterpreter<T> getAllNbtSubtypeInterpreter();

    IIngredientRenderer<T> createRenderer(long capacity, boolean showCapacity, int width, int height);

    TextureAtlasSprite getStillFluidSprite(T ingredient);

    Component getDisplayName(T ingredient);

    int getColor(T ingredient);

    long getAmount(T ingredient);

    long bucketVolume();
}
