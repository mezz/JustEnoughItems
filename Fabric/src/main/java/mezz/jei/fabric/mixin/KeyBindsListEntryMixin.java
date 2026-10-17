package mezz.jei.fabric.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.fabric.input.FabricKeyMapping;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.controls.KeyBindsList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(KeyBindsList.KeyEntry.class)
public class KeyBindsListEntryMixin {
	@WrapOperation(method = "method_19870", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Options;setKey(Lnet/minecraft/client/KeyMapping;Lcom/mojang/blaze3d/platform/InputConstants$Key;)V"))
	private void resetJeiModifiers(Options options, KeyMapping mapping, InputConstants.Key key, Operation<Void> original) {
		original.call(options, mapping, key);
		if (mapping instanceof FabricKeyMapping jeiMapping) {
			jeiMapping.resetModifiersToDefault();
		}
	}
}
