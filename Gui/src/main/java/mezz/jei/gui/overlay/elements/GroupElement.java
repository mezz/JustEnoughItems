package mezz.jei.gui.overlay.elements;

import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IJeiKeyMapping;
import mezz.jei.api.runtime.IRecipesGui;
import mezz.jei.common.Internal;
import mezz.jei.common.gui.JeiTooltip;
import mezz.jei.common.ingredients.group.IngredientGroupInfo;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.gui.bookmarks.IBookmark;
import mezz.jei.gui.config.GroupExpandStateConfig;
import mezz.jei.gui.ingredients.ListGroupElement;
import mezz.jei.gui.input.UserInput;
import mezz.jei.gui.overlay.IngredientGridTooltipHelper;
import mezz.jei.gui.util.FocusUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class GroupElement implements IElement {
	private final ListGroupElement groupElement;
	private final List<IElement> memberElements;
	private final Runnable onExpandedChange;
    private final GroupExpandStateConfig groupStateConfig;
	private final GroupElementOverlay overlay;

	public GroupElement(
            ListGroupElement groupElement,
            Runnable onExpandedChange,
            GroupExpandStateConfig groupStateConfig,
            GroupElementOverlay overlay
    ) {
		this.groupElement = groupElement;
		this.memberElements = groupElement.getMembers()
				.stream()
				.<IElement>map(element -> new IngredientElement<>(element.getTypedIngredient()))
				.toList();
		this.onExpandedChange = onExpandedChange;
        this.groupStateConfig = groupStateConfig;
		this.overlay = overlay;
	}

	@Override
	public ITypedIngredient<?> getTypedIngredient() {
		return memberElements.getFirst().getTypedIngredient();
	}

	@Override
	public Optional<IBookmark> getBookmark() {
		return memberElements.getFirst().getBookmark();
	}

	@Override
	public @Nullable IElementOverlay createRenderOverlay() {
		return overlay;
	}

	@Override
	public void getTooltip(JeiTooltip tooltip, IngredientGridTooltipHelper tooltipHelper) {
		if (memberElements.size() <= 1) {
			memberElements.getFirst().getTooltip(tooltip, tooltipHelper);
			tooltip.add(Component.empty());
			tooltip.add(groupElement.getGroupInfo().getName().copy().withStyle(ChatFormatting.WHITE));
			String modName = tooltipHelper.getModIdHelper().getFormattedModNameForModId(groupElement.getGroupInfo().id().getNamespace());
			MutableComponent addedBy = Component.translatable("jei.group.added_by", modName);
			tooltip.add(addedBy.withStyle(ChatFormatting.GRAY));
			return;
		}
		tooltip.add(groupElement.getGroupInfo().getName().copy().withStyle(ChatFormatting.WHITE));
		IInternalKeyMappings keyMappings = Internal.getKeyMappings();
		IJeiKeyMapping groupAction = keyMappings.getGroupAction();
		tooltip.addKeyUsageComponent("jei.group.expand", groupAction);
		tooltip.add(new GroupElementTooltipComponent(memberElements));
		String modName = tooltipHelper.getModIdHelper().getFormattedModNameForModId(groupElement.getGroupInfo().id().getNamespace());
		MutableComponent addedBy = Component.translatable("jei.group.added_by", modName);
		tooltip.add(addedBy.withStyle(ChatFormatting.GRAY));
	}

	@Override
	public boolean handleClick(UserInput input, IInternalKeyMappings keyBindings) {
		if (input.is(keyBindings.getGroupAction())) {
			if (input.isSimulate()) {
				return true;
			}
            IngredientGroupInfo groupInfo = groupElement.getGroupInfo();
			groupStateConfig.setExpanded(groupInfo.id(), !groupStateConfig.isExpanded(groupInfo));
			onExpandedChange.run();
			return true;
		}
		return false;
	}

	@Override
	public boolean isVisible() {
		return true;
	}

	@Override
	public void show(IRecipesGui recipesGui, FocusUtil focusUtil, List<RecipeIngredientRole> roles) {
	}
}
