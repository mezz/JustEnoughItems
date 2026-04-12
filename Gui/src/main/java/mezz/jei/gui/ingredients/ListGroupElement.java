package mezz.jei.gui.ingredients;

import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.common.ingredients.group.IngredientGroupInfo;

import java.util.ArrayList;
import java.util.List;

public final class ListGroupElement implements IListElement {
	private final IngredientGroupInfo groupInfo;
	private final int createdIndex;
	private final List<IListElement> members = new ArrayList<>();

	public ListGroupElement(IngredientGroupInfo groupInfo) {
		this.groupInfo = groupInfo;
		this.createdIndex = ListElementInfo.elementCount++;
	}

	public ListGroupElement(ListGroupElement other) {
		this.groupInfo = other.groupInfo;
		this.createdIndex = other.createdIndex;
		this.members.addAll(other.members);
	}

	public IngredientGroupInfo getGroupInfo() {
		return groupInfo;
	}

	public void addMember(IListElement element) {
		members.add(element);
	}

	public List<IListElement> getMembers() {
		return members;
	}

	@Override
	public boolean isGroup() {
		return true;
	}

	@Override
	public ITypedIngredient<?> getTypedIngredient() {
		return members.getFirst().getTypedIngredient();
	}

	@Override
	public int getSortedIndex() {
		return createdIndex;
	}

	@Override
	public void setSortedIndex(int sortIndex) {
		// Group sort index derived from members, no-op
	}

	@Override
	public int getCreatedIndex() {
		return createdIndex;
	}

	@Override
	public boolean isVisible() {
		return members.isEmpty() || members.stream().anyMatch(IListElement::isVisible);
	}

	@Override
	public void setVisible(boolean visible) {
	}
}
