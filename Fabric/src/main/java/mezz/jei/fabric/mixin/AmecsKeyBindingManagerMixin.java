package mezz.jei.fabric.mixin;

import mezz.jei.fabric.input.ContextAwareKeyMapping;
import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;
import java.util.stream.Stream;

@Pseudo
@Mixin(targets = "de.siphalor.amecs.impl.KeyBindingManager", remap = false)
public class AmecsKeyBindingManagerMixin {
	@Redirect(
		method = "getMatchingKeyBindings",
		at = @At(
			value = "INVOKE",
			target = "Ljava/util/List;stream()Ljava/util/stream/Stream;"
		),
		require = 3
	)
	private static Stream<KeyMapping> filterInactiveJeiMappings(List<KeyMapping> mappings) {
		return mappings.stream()
			.filter(mapping -> !(mapping instanceof ContextAwareKeyMapping contextAware) || contextAware.isContextActive());
	}
}
