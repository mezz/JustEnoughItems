package mezz.jei.vote;

import com.google.common.base.MoreObjects;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.ingredients.IIngredientType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.ITextComponent;

import java.util.List;
import java.util.function.Supplier;

public class GoVoteIngredient {
	public static final IIngredientType<GoVoteIngredient> TYPE = () -> GoVoteIngredient.class;

	private final String displayName;
	private final List<ITextComponent> tooltip;
	private final Supplier<IDrawable> icon;
	private final String uid;
	private final String url;

	public GoVoteIngredient(Supplier<IDrawable> icon, String uid, String displayName, List<ITextComponent> tooltip, String url) {
		this.icon = icon;
		this.uid = uid;
		this.displayName = displayName;
		this.tooltip = tooltip;
		this.url = url;
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getUid() {
		return uid;
	}

	public IDrawable getIcon() {
		return icon.get();
	}

	public List<ITextComponent> getTooltip() {
		return tooltip;
	}

	@SuppressWarnings("ConstantConditions")
	public void onClick() {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft != null) {
			Screen currentScreen = minecraft.currentScreen;
			if (currentScreen != null) {
				GoVoteHandler.displayOpenLinkScreen(url, minecraft, currentScreen);
			}
		}
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("displayName", displayName)
			.add("tooltip", tooltip)
			.add("icon", icon)
			.add("uid", uid)
			.toString();
	}
}
