package mezz.jei.fabric.platform;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.datafixers.util.Either;
import mezz.jei.common.input.MouseButtonEventData;
import mezz.jei.common.input.keys.IJeiKeyMappingCategoryBuilder;
import mezz.jei.common.platform.IPlatformInputHelper;
import mezz.jei.fabric.input.FabricJeiKeyMappingCategoryBuilder;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.input.KeyEvent;

public class InputHelper implements IPlatformInputHelper {
	@Override
	public boolean isActiveAndMatches(KeyMapping keyMapping, InputConstants.Key key, Either<MouseButtonEventData, KeyEvent> event) {
		if (keyMapping.isUnbound()) {
			return false;
		}
		return event.map(e -> keyMapping.matchesMouse(e.event()), keyMapping::matches);
	}

	@Override
	public IJeiKeyMappingCategoryBuilder createKeyMappingCategoryBuilder(KeyMapping.Category category) {
		return new FabricJeiKeyMappingCategoryBuilder(category);
	}
}
