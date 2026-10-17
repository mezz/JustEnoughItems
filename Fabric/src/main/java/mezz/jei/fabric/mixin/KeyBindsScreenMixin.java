package mezz.jei.fabric.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.fabric.input.FabricKeyMapping;
import mezz.jei.fabric.input.ContextAwareKeyMapping;
import mezz.jei.fabric.input.FabricKeyModifiers;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.client.gui.screens.options.controls.KeyBindsList;
import net.minecraft.client.gui.screens.options.controls.KeyBindsScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(KeyBindsScreen.class)
public abstract class KeyBindsScreenMixin extends OptionsSubScreen {
	@Shadow
	public @Nullable KeyMapping selectedKey;
	@Shadow
	public long lastKeySelection;
	@Shadow
	private KeyBindsList keyBindsList;
	@Unique
	private InputConstants.@Nullable Key jei$pendingModifier;

	protected KeyBindsScreenMixin(Screen lastScreen, Options options, Component title) {
		super(lastScreen, options, title);
	}

	@Inject(
		method = "keyPressed",
		at = @At("HEAD"),
		cancellable = true
	)
	private void bindJeiKeyboard(KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
		if (selectedKey instanceof FabricKeyMapping mapping) {
			InputConstants.Key key = InputConstants.getKey(event);
			if (event.isEscape()) {
				key = InputConstants.UNKNOWN;
			}
			int modifier = FabricKeyModifiers.forKey(event.key());
			mapping.setKeyAndModifiers(key, event.modifiers() & ~modifier);
			if (modifier != 0) {
				// Record a standalone modifier while waiting for a possible shortcut key.
				jei$pendingModifier = key;
				keyBindsList.resetMappingAndUpdateButtons();
			} else {
				jei$finishBinding();
			}
			cir.setReturnValue(true);
		}
	}

	@Override
	public boolean keyReleased(KeyEvent event) {
		if (selectedKey instanceof FabricKeyMapping && InputConstants.getKey(event).equals(jei$pendingModifier)) {
			jei$finishBinding();
			return true;
		}
		return super.keyReleased(event);
	}

	@Inject(
		method = "mouseClicked",
		at = @At("HEAD")
	)
	private void bindJeiMouse(MouseButtonEvent event, boolean doubleClick, CallbackInfoReturnable<Boolean> cir) {
		if (selectedKey instanceof FabricKeyMapping mapping) {
			mapping.setModifiers(event.modifiers());
			jei$pendingModifier = null;
		}
	}

	@WrapOperation(method = "lambda$addFooter$0", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;setKey(Lcom/mojang/blaze3d/platform/InputConstants$Key;)V"))
	private void resetJeiModifiers(KeyMapping mapping, InputConstants.Key key, Operation<Void> original) {
		original.call(mapping, key);
		if (mapping instanceof ContextAwareKeyMapping jeiMapping) {
			jeiMapping.resetModifiersToDefault();
		}
	}

	@Unique
	private void jei$finishBinding() {
		selectedKey = null;
		jei$pendingModifier = null;
		lastKeySelection = Util.getMillis();
		keyBindsList.resetMappingAndUpdateButtons();
	}
}
