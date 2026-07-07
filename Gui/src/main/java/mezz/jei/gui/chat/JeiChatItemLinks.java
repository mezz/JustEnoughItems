package mezz.jei.gui.chat;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.common.Internal;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class JeiChatItemLinks {
	public static final String SHOW_RECIPE_COMMAND = "jei_internal_show";

	private static final Pattern LINK_PATTERN = Pattern.compile(
		"\\[JEI:([a-z0-9_.-]+:[a-z0-9_./-]+)\\|([^\\]]*)]"
	);

	private JeiChatItemLinks() {
	}

	public static String createLinkMarker(ItemStack stack) {
		if (stack.isEmpty()) {
			return "";
		}
		Identifier itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
		String itemName = sanitizeDisplayName(stack.getHoverName().getString());
		return "[JEI:" + itemId + "|" + itemName + "] ";
	}

	public static Component parse(String rawText) {
		Matcher matcher = LINK_PATTERN.matcher(rawText);
		MutableComponent result = Component.empty();
		int lastEnd = 0;

		while (matcher.find()) {
			if (matcher.start() > lastEnd) {
				result.append(Component.literal(rawText.substring(lastEnd, matcher.start())));
			}

			String itemId = matcher.group(1);
			String itemName = matcher.group(2);
			result.append(createLinkComponent(itemId, itemName));

			lastEnd = matcher.end();
		}

		if (lastEnd < rawText.length()) {
			result.append(Component.literal(rawText.substring(lastEnd)));
		}

		return result;
	}

	public static boolean showRecipeForItemId(String itemId) {
		IJeiRuntime runtime = Internal.getJeiRuntime();
		if (runtime == null) {
			return false;
		}

		ItemStack stack = resolveItemStack(itemId);
		if (stack.isEmpty()) {
			return false;
		}

		runtime.getRecipesGui().show(
			runtime.getJeiHelpers().getFocusFactory().createFocus(
				RecipeIngredientRole.OUTPUT,
				VanillaTypes.ITEM_STACK,
				stack
			)
		);
		return true;
	}

	private static ItemStack resolveItemStack(String itemId) {
		Identifier identifier;
		try {
			identifier = Identifier.parse(itemId);
		} catch (Exception e) {
			return ItemStack.EMPTY;
		}

		Item item = BuiltInRegistries.ITEM.get(identifier)
			.map(Holder::value)
			.orElse(Items.AIR);
		return new ItemStack(item);
	}

	private static MutableComponent createLinkComponent(String itemId, String itemName) {
		return Component.literal("[" + itemName + "]")
			.withStyle(style -> style
				.withColor(ChatFormatting.AQUA)
				.withClickEvent(new ClickEvent.RunCommand(SHOW_RECIPE_COMMAND + " " + itemId))
				.withHoverEvent(new HoverEvent.ShowText(Component.translatable("jei.chat.link.show.recipe")))
			);
	}

	private static String sanitizeDisplayName(String name) {
		return name.replace('[', '(').replace(']', ')');
	}
}