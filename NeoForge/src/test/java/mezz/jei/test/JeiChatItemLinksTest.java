package mezz.jei.test;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.common.chat.JeiChatItemLinks;
import mezz.jei.common.chat.JeiChatItemLinks.IngredientLink;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JeiChatItemLinksTest {
	@Test
	public void linkMarkersUseIngredientTypeAndUid() {
		// Setup: marker data for an item ingredient that can be resolved by the receiving client.
		IngredientLink ingredientLink = createItemLink("minecraft:diamond");
		String expectedLinkText = createExpectedLinkText(ingredientLink);

		// Operation: create a JEI link marker.
		String marker = createLinkMarker(ingredientLink);
		String linkText = getLinkText(marker);
		Optional<IngredientLink> parsedLink = JeiChatItemLinks.parseCommandArgument(linkText);

		// Assertions: raw ingredient type and UID are visible in the serialized chat text.
		assertEquals(expectedLinkText, linkText);
		assertTrue(linkText.contains(VanillaTypes.ITEM_STACK.getUid()));
		assertTrue(linkText.contains("minecraft:diamond"));
		assertTrue(parsedLink.isPresent());
		IngredientLink link = parsedLink.get();
		assertEquals(VanillaTypes.ITEM_STACK.getUid(), link.ingredientTypeUid());
		assertEquals("minecraft:diamond", link.ingredientUid());
	}

	@Test
	public void fluidLinkMarkersUseReadableTypeAndUid() {
		// Setup: marker data for a fluid ingredient that can be resolved by the receiving client.
		IngredientLink ingredientLink = new IngredientLink(NeoForgeTypes.FLUID_STACK.getUid(), "minecraft:lava");
		String expectedLinkText = "v1:fluid_stack;minecraft:lava";

		// Operation: create a JEI link marker.
		String marker = createLinkMarker(ingredientLink);
		String linkText = getLinkText(marker);
		Optional<IngredientLink> parsedLink = JeiChatItemLinks.parseCommandArgument(linkText);

		// Assertions: the serialized chat text stays readable for clients without JEI.
		assertEquals("[JEI:" + expectedLinkText + "] ", marker);
		assertEquals(expectedLinkText, linkText);
		assertEquals(Optional.of(ingredientLink), parsedLink);
	}

	@Test
	public void validLinkMarkersAreDetected() {
		// Setup: a chat message with a complete JEI link marker.
		String marker = createLinkMarker("minecraft:diamond");
		String rawText = "Look at " + marker.trim();

		// Operation: check for JEI link markers.
		boolean hasLinkMarkers = JeiChatItemLinks.hasLinkMarkers(rawText);

		// Assertions: complete markers are handled by the platform chat handlers.
		assertTrue(hasLinkMarkers);
	}

	@Test
	public void itemIdLinkMarkersAreIgnored() {
		// Setup: a chat message with a raw item id instead of a complete JEI UID link.
		String rawText = "Look at [JEI:minecraft:diamond]";

		// Operation: check for JEI link markers.
		boolean hasLinkMarkers = JeiChatItemLinks.hasLinkMarkers(rawText);

		// Assertions: this feature has no legacy format, so only complete JEI UID links are accepted.
		assertFalse(hasLinkMarkers);
	}

	@Test
	public void lengthPrefixedLinkMarkersAreIgnored() {
		// Setup: a chat message with the older internal marker shape.
		String rawText = "Look at [JEI:v1:type(10)=item_stack;uid(17)=minecraft:diamond]";
		String linkText = getLinkText("[JEI:v1:type(10)=item_stack;uid(17)=minecraft:diamond] ");

		// Operation: check for JEI link markers.
		boolean hasLinkMarkers = JeiChatItemLinks.hasLinkMarkers(rawText);
		Optional<IngredientLink> parsedLink = JeiChatItemLinks.parseCommandArgument(linkText);

		// Assertions: this feature has no legacy format, so old marker shapes are not accepted.
		assertFalse(hasLinkMarkers);
		assertTrue(parsedLink.isEmpty());
	}

	@Test
	public void incompleteLinkMarkersAreIgnored() {
		// Setup: a chat message that mentions a JEI marker prefix but is not a complete link.
		String rawText = "This is not a link: [JEI:";

		// Operation: check for JEI link markers.
		boolean hasLinkMarkers = JeiChatItemLinks.hasLinkMarkers(rawText);

		// Assertions: incomplete markers should stay as normal chat text.
		assertFalse(hasLinkMarkers);
	}

	@Test
	public void linkMarkersAreParsedIntoClickableComponents() {
		// Setup: a chat message with normal text around a JEI link marker.
		String marker = createLinkMarker("minecraft:diamond");
		String rawText = "Look at " + marker.trim() + "!";

		// Operation: parse the raw chat text.
		Component parsed = JeiChatItemLinks.parse(rawText, JeiChatItemLinksTest::getItemName);
		Component link = parsed.getSiblings().get(1);

		// Assertions: the marker text is replaced by a locally-resolved clickable item-name component.
		assertEquals("Look at [Diamond]!", parsed.getString());
		ClickEvent clickEvent = assertInstanceOf(ClickEvent.RunCommand.class, link.getStyle().getClickEvent());
		IngredientLink diamond = createItemLink("minecraft:diamond");
		assertEquals(JeiChatItemLinks.createShowRecipeCommand(diamond), ((ClickEvent.RunCommand) clickEvent).command());

		HoverEvent hoverEvent = assertInstanceOf(HoverEvent.ShowText.class, link.getStyle().getHoverEvent());
		Component hoverText = ((HoverEvent.ShowText) hoverEvent).value();
		assertEquals("Diamond", hoverText.getString());
	}

	@Test
	public void rawUidLinkMarkersCanContainReadableCharacters() {
		// Setup: a raw UID with spaces and symbols that are common in readable text.
		IngredientLink ingredientLink = new IngredientLink("test:ingredient", "uid with spaces ; and symbols");
		String marker = createLinkMarker(ingredientLink);
		String rawText = "Look at " + marker.trim() + "!";

		// Operation: parse the raw chat text.
		Component parsed = JeiChatItemLinks.parse(rawText, link -> {
			if (link.equals(ingredientLink)) {
				return Optional.of("Complex Ingredient");
			}
			return Optional.empty();
		});

		// Assertions: the full raw UID is recovered from the readable link marker.
		assertEquals("Look at [Complex Ingredient]!", parsed.getString());
	}

	@Test
	public void showRecipeCommandsParseIngredientLinks() {
		// Setup: a command generated for a JEI chat link.
		IngredientLink link = createItemLink("minecraft:diamond");
		String command = JeiChatItemLinks.createShowRecipeCommand(link);

		// Operation: parse the command back into an ingredient link.
		Optional<IngredientLink> parsedLink = JeiChatItemLinks.parseShowRecipeCommand(command);

		// Assertions: chat screen input handlers can recover the linked ingredient from the style command.
		assertEquals(Optional.of(link), parsedLink);
	}

	@Test
	public void nonJeiCommandsAreIgnored() {
		// Setup: a non-JEI command from a different clickable chat component.
		String command = "help";

		// Operation: try to parse it as a JEI chat link command.
		Optional<IngredientLink> parsedLink = JeiChatItemLinks.parseShowRecipeCommand(command);

		// Assertions: unrelated chat links should be left to vanilla handling.
		assertTrue(parsedLink.isEmpty());
	}

	@Test
	public void multipleLinkMarkersAreParsedInOrder() {
		// Setup: a chat message with more than one JEI link marker.
		String diamondMarker = createLinkMarker("minecraft:diamond").trim();
		String stickMarker = createLinkMarker("minecraft:stick").trim();
		String rawText = "Try " + diamondMarker + " or " + stickMarker + ".";

		// Operation: parse the raw chat text.
		Component parsed = JeiChatItemLinks.parse(rawText, JeiChatItemLinksTest::getItemName);
		Component firstLink = parsed.getSiblings().get(1);
		Component secondLink = parsed.getSiblings().get(3);

		// Assertions: each marker becomes a separate clickable component with its own item id.
		assertEquals("Try [Diamond] or [Stick].", parsed.getString());
		assertRunCommand(firstLink, "minecraft:diamond");
		assertRunCommand(secondLink, "minecraft:stick");
	}

	@Test
	public void chatMessagesWithoutLinkMarkersAreNotParsed() {
		// Setup: a normal chat message with no JEI link markers.
		Component message = Component.literal("Nothing to see here.");

		// Operation: parse the chat message.
		Optional<Component> parsed = JeiChatItemLinks.parseChatMessage(message);

		// Assertions: platform chat handlers should leave the original message alone.
		assertTrue(parsed.isEmpty());
	}

	@Test
	public void chatMessagesWithLinkMarkersAreParsed() {
		// Setup: a chat message with a JEI link marker.
		String marker = createLinkMarker("minecraft:diamond");
		Component message = Component.literal("Look at " + marker.trim() + "!");

		// Operation: parse the chat message.
		Optional<Component> parsed = JeiChatItemLinks.parseChatMessage(message, JeiChatItemLinksTest::getItemName);

		// Assertions: platform chat handlers can replace the message with the parsed component.
		assertTrue(parsed.isPresent());
		Component parsedMessage = parsed.get();
		assertEquals("Look at [Diamond]!", parsedMessage.getString());
	}

	private static void assertRunCommand(Component component, String itemId) {
		ClickEvent clickEvent = assertInstanceOf(ClickEvent.RunCommand.class, component.getStyle().getClickEvent());
		IngredientLink link = createItemLink(itemId);
		assertEquals(JeiChatItemLinks.createShowRecipeCommand(link), ((ClickEvent.RunCommand) clickEvent).command());
	}

	private static Optional<String> getItemName(IngredientLink link) {
		if (link.equals(createItemLink("minecraft:diamond"))) {
			return Optional.of("Diamond");
		}
		if (link.equals(createItemLink("minecraft:stick"))) {
			return Optional.of("Stick");
		}
		return Optional.empty();
	}

	private static IngredientLink createItemLink(String itemId) {
		return new IngredientLink(VanillaTypes.ITEM_STACK.getUid(), itemId);
	}

	private static String createLinkMarker(String itemId) {
		IngredientLink link = createItemLink(itemId);
		return createLinkMarker(link);
	}

	private static String createLinkMarker(IngredientLink link) {
		String linkText = JeiChatItemLinks.createCommandArgument(link);
		return "[JEI:" + linkText + "] ";
	}

	private static String createExpectedLinkText(IngredientLink link) {
		String ingredientTypeUid = link.ingredientTypeUid();
		String ingredientUid = link.ingredientUid();
		return "v1:" +
			ingredientTypeUid +
			";" +
			ingredientUid;
	}

	private static String getLinkText(String marker) {
		return marker.substring("[JEI:".length(), marker.length() - "] ".length());
	}
}
