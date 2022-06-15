package mezz.jei.fabric.mixin;

import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.UpgradeRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(UpgradeRecipe.class)
public interface UpgradeRecipeAccess {
    @Accessor
    Ingredient getBase();

    @Accessor
    Ingredient getAddition();
}
