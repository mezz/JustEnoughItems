package mezz.jei.fabric.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(Screen.class)
public interface ScreenAccessor {
    @Invoker
    void callRenderTooltipInternal(PoseStack poseStack, List<ClientTooltipComponent> clientTooltipComponents, int mouseX, int mouseY);
}
