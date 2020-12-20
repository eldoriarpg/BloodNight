package de.eldoria.bloodnight.command.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.OptionalInt;
import java.util.function.Function;
import java.util.function.Predicate;

public final class CommandUtil {
	private CommandUtil() {
	}

	/**
	 * Get a page from a collection. This collection will always be of size 0-size.
	 *
	 * @param collection collection to parse
	 * @param page       page starting from 0
	 * @param size       size of a page
	 * @param <T>        Type of collection
	 *
	 * @return collection with a size beteween 0 and size
	 */
	public static <T> Collection<T> getSlice(Collection<T> collection, int page, int size) {
		List<T> list = new ArrayList<>(collection);
		List<T> slice = new ArrayList<>();
		for (int i = page * size; i < page * size + size && i < collection.size(); i++) {
			slice.add(list.get(i));
		}
		return slice;
	}

	public static int pageCount(Collection<?> collection, int size) {
		return (int) Math.floor(Math.max(collection.size() - 1, 0) / (double) size);
	}


	public static TextComponent getPageFooter(int page, int pageMax, String pageCommand) {
		TextComponent.Builder builder = Component.text();
		builder.append(Component.text("=====<| ", NamedTextColor.YELLOW));

		if (page != 0) {
			builder.append(
					Component.text("<<< ", NamedTextColor.AQUA)
							.clickEvent(
									ClickEvent.runCommand(pageCommand.replace("{page}", String.valueOf(page - 1)))
							));
		} else {
			builder.append(
					Component.text("<<< ", NamedTextColor.GRAY));
		}

		builder.append(Component.text((page + 1) + "/" + (pageMax + 1), NamedTextColor.YELLOW));

		if (page != pageMax) {
			builder.append(
					Component.text(" >>>")
							.clickEvent(
									ClickEvent.runCommand(pageCommand.replace("{page}", String.valueOf(page + 1)))
							)).color(NamedTextColor.AQUA);
		} else {
			builder.append(
					Component.text(" >>>", NamedTextColor.GRAY));
		}

		builder.append(Component.text(" |>=====", NamedTextColor.YELLOW));
		return builder.build();
	}

	public static TextComponent getFooter() {
		return Component.text()
				.append(Component.text("=====<|    ", NamedTextColor.YELLOW))
				.append(Component.text("    |>=====", NamedTextColor.YELLOW))
				.build();
	}

	public static <T> OptionalInt findPage(Collection<T> content, int pageSize, Predicate<T> predicate) {
		Iterator<T> iterator = content.iterator();
		int page = 0;
		while (iterator.hasNext()) {
			for (int i = 0; i < pageSize; i++) {
				if (!iterator.hasNext()) break;
				T next = iterator.next();
				if (predicate.test(next)) return OptionalInt.of(page);
			}
			page++;
		}
		return OptionalInt.empty();
	}

	public static <T> TextComponent getPage(Collection<T> content, int page, Function<T, TextComponent> mapping, String title, String pageCommand) {
		return getPage(content, page, 18, 1, mapping, title, pageCommand);
	}

	public static <T> TextComponent getPage(Collection<T> content, int page, int elementsPerPage, int elementSize, Function<T, TextComponent> mapping, String title, String pageCommand) {
		Collection<T> elements = getSlice(content, page, elementsPerPage);
		TextComponent.Builder builder = Component.text();
		for (int i = elements.size() * elementSize; i < 18; i++) {
			builder.append(Component.newline());

		}

		builder.append(Component.text("=====<| ").color(NamedTextColor.YELLOW))
				.append(Component.text(title).color(NamedTextColor.AQUA))
				.append(Component.text(" |>=====").color(NamedTextColor.YELLOW));
		for (T t : elements) {
			builder.append(Component.newline())
					.append(mapping.apply(t));
		}

		return builder.append(Component.newline())
				.append(
						CommandUtil.getPageFooter(page,
								CommandUtil.pageCount(content, elementsPerPage),
								pageCommand)).build();
	}

	public static TextComponent getHeader(String title) {
		return Component.text()
				.append(Component.text("=====<| ").color(NamedTextColor.YELLOW))
				.append(Component.text(title).color(NamedTextColor.AQUA))
				.append(Component.text(" |>=====").color(NamedTextColor.YELLOW)).build();
	}

	public static TextComponent getBooleanField(boolean currValue, String cmd, String field, String postive, String negative) {
		return Component.text()
				.append(Component.text(field + ": ", NamedTextColor.AQUA))
				.append(
						Component.text(postive,
								currValue ? NamedTextColor.GREEN : NamedTextColor.DARK_GRAY)
								.clickEvent(
										ClickEvent.runCommand(cmd.replace("{bool}", "true")))
				)
				.append(Component.space())
				.append(
						Component.text(negative,
								!currValue ? NamedTextColor.RED : NamedTextColor.DARK_GRAY)
								.clickEvent(
										ClickEvent.runCommand(cmd.replace("{bool}", "false"))))
				.build();
	}

	public static TextComponent getToggleField(boolean currValue, String cmd, String field) {
		String newCmd = cmd.replace("{bool}", String.valueOf(!currValue));
		return Component.text()
				.append(
						Component.text("[" + field + "]",
								currValue ? NamedTextColor.GREEN : NamedTextColor.DARK_GRAY)
								.clickEvent(
										ClickEvent.runCommand(newCmd))
				)
				.build();

	}
}
