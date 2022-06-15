package mezz.jei.fabric.mixin;

import net.minecraft.client.KeyboardHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(KeyboardHandler.class)
public interface KeyboardHandlerAccess {
    @Accessor
    boolean getSendRepeatsToGui();
}
