package mezz.jei.gui.ingredients;

import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.config.IIngredientFilterConfig;
import mezz.jei.common.ingredients.group.IngredientGroupInfo;
import mezz.jei.common.util.Translator;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public final class ListGroupElementInfo implements IListElementInfo {

	private final IngredientGroupInfo groupInfo;
	private final IModIdHelper modIdHelper;
	private @Nullable ListGroupElement element;

	public ListGroupElementInfo(IngredientGroupInfo groupInfo, IModIdHelper modIdHelper) {
		this.groupInfo = groupInfo;
		this.modIdHelper = modIdHelper;
	}

	public IngredientGroupInfo getGroupInfo() {
		return groupInfo;
	}

	@Override
	public boolean isGroup() {
		return true;
	}

	@Override
	public List<String> getNames() {
		String groupName = Translator.toLowercaseWithLocale(groupInfo.getName().getString());
		return List.of(groupName);
	}

	@Override
	public String getModNameForSorting() {
		return modIdHelper.getModNameForModId(groupInfo.id().getNamespace());
	}

	@Override
	public List<String> getModNames() {
		return List.of(modIdHelper.getModNameForModId(groupInfo.id().getNamespace()));
	}

	@Override
	public List<String> getModIds() {
		return List.of(groupInfo.id().getNamespace());
	}

	@Override
	public @Unmodifiable Set<String> getTooltipStrings(IIngredientFilterConfig config, IIngredientManager ingredientManager) {
		return Set.of();
	}

	@Override
	public Collection<String> getTagStrings(IIngredientManager ingredientManager) {
		return List.of();
	}

	@Override
	public Stream<Identifier> getTagIds(IIngredientManager ingredientManager) {
		return Stream.empty();
	}

	@Override
	public Iterable<Integer> getColors(IIngredientManager ingredientManager) {
		return List.of();
	}

	@Override
	public @Unmodifiable Collection<String> getCreativeTabsStrings(IIngredientManager ingredientManager) {
		return List.of();
	}

	@Override
	public Identifier getIdentifier() {
		return groupInfo.id();
	}

	@Override
	public ListGroupElement getElement() {
		if (element == null) {
			element = new ListGroupElement(groupInfo);
		}
		return element;
	}

	@Override
	public ITypedIngredient<?> getTypedIngredient() {
		return getElement().getTypedIngredient();
	}

	@Override
	public int getCreatedIndex() {
		return getElement().getCreatedIndex();
	}
}