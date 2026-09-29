package mezz.jei.common.platform;

import mezz.jei.common.input.UserInput;
import mezz.jei.common.input.keys.IJeiKeyMappingCategoryBuilder;
import net.minecraft.client.KeyMapping;
import net.minecraft.world.item.TooltipFlag;

public interface IPlatformInputHelper {
	boolean isActiveAndMatches(KeyMapping keyMapping, UserInput input);

	IJeiKeyMappingCategoryBuilder createKeyMappingCategoryBuilder(KeyMapping.Category category);

	default TooltipFlag getClientTooltipFlag(TooltipFlag tooltipFlag) {
		return tooltipFlag;
	}

	default TooltipFlag getSearchTooltipFlag(TooltipFlag tooltipFlag) {
		return tooltipFlag;
	}
}
