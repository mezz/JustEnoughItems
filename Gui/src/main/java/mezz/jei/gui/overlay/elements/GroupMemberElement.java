package mezz.jei.gui.overlay.elements;

import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IJeiKeyMapping;
import mezz.jei.api.runtime.IRecipesGui;
import mezz.jei.common.Internal;
import mezz.jei.common.gui.JeiTooltip;
import mezz.jei.common.ingredients.group.IngredientGroupInfo;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.gui.bookmarks.IBookmark;
import mezz.jei.gui.config.GroupExpandStateConfig;
import mezz.jei.gui.input.UserInput;
import mezz.jei.gui.overlay.IngredientGridTooltipHelper;
import mezz.jei.gui.util.FocusUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class GroupMemberElement<T> implements IElement {
	private final ITypedIngredient<T> ingredient;
	private final IngredientGroupInfo groupInfo;
	private final Runnable onCollapse;
	private final GroupExpandStateConfig groupStateConfig;
	private final GroupElementOverlay overlay;

	public GroupMemberElement(ITypedIngredient<T> ingredient, IngredientGroupInfo groupInfo, Runnable onCollapse, GroupExpandStateConfig groupStateConfig, GroupElementOverlay overlay) {
		this.ingredient = ingredient;
		this.groupInfo = groupInfo;
		this.onCollapse = onCollapse;
		this.groupStateConfig = groupStateConfig;
		this.overlay = overlay;
	}

	@Override
	public ITypedIngredient<T> getTypedIngredient() {
		return ingredient;
	}

	@Override
	public Optional<IBookmark> getBookmark() {
		return Optional.empty();
	}

	@Override
	public @Nullable IElementOverlay createRenderOverlay() {
		return overlay;
	}

	@Override
	public void show(IRecipesGui recipesGui, FocusUtil focusUtil, List<RecipeIngredientRole> roles) {
		List<IFocus<?>> focuses = focusUtil.createFocuses(ingredient, roles);
		recipesGui.show(focuses);
	}

	@Override
	public void getTooltip(JeiTooltip tooltip, IngredientGridTooltipHelper tooltipHelper) {
		tooltipHelper.getIngredientTooltip(tooltip, ingredient);
		tooltip.add(Component.empty());
		tooltip.add(groupInfo.getName().copy().withStyle(ChatFormatting.WHITE));
		IInternalKeyMappings keyMappings = Internal.getKeyMappings();
		IJeiKeyMapping groupAction = keyMappings.getGroupAction();
		tooltip.addKeyUsageComponent("jei.group.collapse", groupAction);
		String modName = tooltipHelper.getModIdHelper().getFormattedModNameForModId(groupInfo.id().getNamespace());
		MutableComponent addedBy = Component.translatable("jei.group.added_by", modName);
		tooltip.add(addedBy.withStyle(ChatFormatting.GRAY));
	}

	@Override
	public boolean handleClick(UserInput input, IInternalKeyMappings keyBindings) {
		if (input.is(keyBindings.getGroupAction())) {
			if (input.isSimulate()) {
				return true;
			}
			groupStateConfig.setExpanded(groupInfo.id(), !groupStateConfig.isExpanded(groupInfo));
			onCollapse.run();
			return true;
		}
		return false;
	}

	@Override
	public boolean isVisible() {
		return true;
	}
}
