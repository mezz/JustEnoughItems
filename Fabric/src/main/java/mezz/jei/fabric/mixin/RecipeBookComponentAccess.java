package mezz.jei.fabric.mixin;

import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeBookTabButton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(RecipeBookComponent.class)
public interface RecipeBookComponentAccess {
    @Accessor
    int getWidth();

    @Accessor
    int getHeight();

    @Accessor
    int getXOffset();

    @Accessor
    List<RecipeBookTabButton> getTabButtons();
}
