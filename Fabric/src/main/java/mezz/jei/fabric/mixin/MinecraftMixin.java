package mezz.jei.fabric.mixin;

import mezz.jei.fabric.events.JeiLifecycleEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.main.GameConfig;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Shadow
    @Final
    private ReloadableResourceManager resourceManager;

    @Shadow
    @Final
    private TextureManager textureManager;

    @Inject(
        method = "<init>(Lnet/minecraft/client/main/GameConfig;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/ResourceLoadStateTracker;startReload(Lnet/minecraft/client/ResourceLoadStateTracker$ReloadReason;Ljava/util/List;)V",
            ordinal = 0
        )
    )
    public void beforeInitialResourceReload(GameConfig gameConfig, CallbackInfo ci) {
        JeiLifecycleEvents.REGISTER_RESOURCE_RELOAD_LISTENER.invoker()
                .registerResourceReloadListener(resourceManager, textureManager);
    }

    @Inject(
        method = "clearLevel(Lnet/minecraft/client/gui/screens/Screen;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/GameRenderer;resetData()V",
            ordinal = 0,
            shift = At.Shift.AFTER
        )
    )
    public void clearLevel(Screen screen, CallbackInfo ci) {
        JeiLifecycleEvents.GAME_STOP.invoker().run();
    }
}
