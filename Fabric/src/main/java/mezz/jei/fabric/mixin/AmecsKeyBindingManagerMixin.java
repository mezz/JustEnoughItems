package mezz.jei.fabric.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import mezz.jei.fabric.input.ContextAwareKeyMapping;
import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Collection;
import java.util.stream.Stream;

@Pseudo
@Mixin(targets = "de.siphalor.amecs.key_modifiers.impl.AmecsKeyMappingManagerLayer", remap = false)
public class AmecsKeyBindingManagerMixin {
	@WrapOperation(
		method = "getMappingsForInput",
		at = @At(
			value = "INVOKE",
			target = "Ljava/util/Collection;stream()Ljava/util/stream/Stream;"
		),
		require = 2
	)
	private static Stream<KeyMapping> filterInactiveJeiMappings(Collection<KeyMapping> mappings, Operation<Stream<KeyMapping>> original) {
		return original.call(mappings)
			.filter(mapping -> !(mapping instanceof ContextAwareKeyMapping contextAware) || contextAware.isContextActive());
	}
}
