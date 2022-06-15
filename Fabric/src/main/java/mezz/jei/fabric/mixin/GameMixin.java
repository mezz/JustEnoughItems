package mezz.jei.fabric.mixin;

import mezz.jei.fabric.events.JeiLifecycleEvents;
import net.minecraft.client.Game;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Game.class)
public class GameMixin {
    @Inject(method = "onStartGameSession()V", at = @At("RETURN"))
    public void onStartGameSession(CallbackInfo ci) {
        JeiLifecycleEvents.GAME_START.invoker().run();
    }

    @Inject(method = "onLeaveGameSession", at = @At("RETURN"))
    public void onLeaveGameSession(CallbackInfo ci) {
        JeiLifecycleEvents.GAME_STOP.invoker().run();
    }
}
