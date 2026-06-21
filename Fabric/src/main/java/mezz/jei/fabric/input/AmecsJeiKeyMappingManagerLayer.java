package mezz.jei.fabric.input;

import com.mojang.blaze3d.platform.InputConstants;
import de.siphalor.amecs.key_modifiers.impl.AmecsKeyMappingManagerLayer;
import net.minecraft.client.KeyMapping;

import java.util.stream.Stream;

@SuppressWarnings("UnstableApiUsage")
public class AmecsJeiKeyMappingManagerLayer extends AmecsKeyMappingManagerLayer {
	@Override
	public boolean accepts(KeyMapping keyMapping) {
		return keyMapping instanceof AmecsKeyMappingWithContext;
	}

	@Override
	public Stream<KeyMapping> getAllMappings() {
		return super.getAllMappings()
			.filter(AmecsJeiKeyMappingManagerLayer::isActive);
	}

	@Override
	public Stream<KeyMapping> getMappingsForInput(InputConstants.Key input) {
		return super.getMappingsForInput(input)
			.filter(AmecsJeiKeyMappingManagerLayer::isActive);
	}

	private static boolean isActive(KeyMapping keyMapping) {
		return keyMapping instanceof AmecsKeyMappingWithContext jeiKeyMapping &&
			jeiKeyMapping.isContextActive();
	}
}
