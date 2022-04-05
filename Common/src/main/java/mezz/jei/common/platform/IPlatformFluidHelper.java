package mezz.jei.common.platform;

import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientTypeWithSubtypes;
import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;
import net.minecraft.world.level.material.Fluid;

public interface IPlatformFluidHelper<T> {
    IIngredientTypeWithSubtypes<Fluid, T> getFluidIngredientType();

    IIngredientSubtypeInterpreter<T> getAllNbtSubtypeInterpreter();

    IIngredientRenderer<T> createRenderer(int capacityMb, boolean showCapacity, int width, int height);
}
