package mezz.jei.fabric.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import mezz.jei.fabric.input.ContextAwareKeyMapping;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Map;

@Mixin(KeyMapping.class)
public class KeyMappingMixin {
	@Shadow
	@Final
	private static Map<String, KeyMapping> ALL;

	@WrapOperation(
		method = {
			"click(Lcom/mojang/blaze3d/platform/InputConstants$Key;)V",
			"set(Lcom/mojang/blaze3d/platform/InputConstants$Key;Z)V"
		},
		at = @At(
			value = "INVOKE",
			target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;"
		)
	)
	private static @Nullable Object getActiveKeyMapping(Map<?, ?> mappings, Object key, Operation<Object> original) {
		Object mapping = original.call(mappings, key);
		if (!(mapping instanceof ContextAwareKeyMapping contextAware) || contextAware.isContextActive()) {
			return mapping;
		}

		return ALL.values().stream()
			.filter(candidate -> KeyBindingHelper.getBoundKeyOf(candidate).equals(key))
			.filter(candidate -> !(candidate instanceof ContextAwareKeyMapping candidateContext) || candidateContext.isContextActive())
			.findFirst()
			.orElse(null);
	}
}
