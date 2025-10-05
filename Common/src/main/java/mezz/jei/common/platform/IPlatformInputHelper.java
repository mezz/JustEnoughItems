package mezz.jei.common.platform;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.datafixers.util.Either;
import mezz.jei.common.input.MouseButtonEventData;
import mezz.jei.common.input.keys.IJeiKeyMappingCategoryBuilder;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.world.item.TooltipFlag;

public interface IPlatformInputHelper {
	boolean isActiveAndMatches(KeyMapping keyMapping, InputConstants.Key key, Either<MouseButtonEventData, KeyEvent> event);

	IJeiKeyMappingCategoryBuilder createKeyMappingCategoryBuilder(KeyMapping.Category category);

	default TooltipFlag getClientTooltipFlag(TooltipFlag tooltipFlag) {
		return tooltipFlag;
	}
}
