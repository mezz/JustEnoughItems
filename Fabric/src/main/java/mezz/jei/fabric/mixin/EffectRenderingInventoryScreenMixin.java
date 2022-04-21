package mezz.jei.fabric.mixin;

import mezz.jei.api.runtime.IIngredientListOverlay;
import mezz.jei.common.Internal;
import mezz.jei.common.runtime.JeiRuntime;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(EffectRenderingInventoryScreen.class)
public abstract class EffectRenderingInventoryScreenMixin extends AbstractContainerScreen<AbstractContainerMenu> {
    public EffectRenderingInventoryScreenMixin(AbstractContainerMenu menu, Inventory inventory, Component component) {
        super(menu, inventory, component);
    }

    @ModifyVariable(
        method = "renderEffects(Lcom/mojang/blaze3d/vertex/PoseStack;II)V",
        index = 7,
        name = "bl",
        at = @At("STORE")
    )
    public boolean modifyHasRoom(boolean bl) {
        boolean ingredientListDisplayed = Internal.getRuntime()
            .map(JeiRuntime::getIngredientListOverlay)
            .map(IIngredientListOverlay::isListDisplayed)
            .orElse(false);

        if (ingredientListDisplayed) {
            // make the potion effects think that there is not enough room,
            // so they render in compact mode.
            return false;
        }
        return bl;
    }
}
