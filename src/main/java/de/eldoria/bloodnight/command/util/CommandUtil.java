package de.eldoria.bloodnight.command.util;

import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

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
        return (int) Math.floor(collection.size() / (double) size);
    }

    public static TextComponent getPageFooter(int page, int pageMax, String pageCommand) {
        TextComponent.Builder builder = TextComponent.builder();
        builder.append("=====<| ").color(KyoriColors.YELLOW);

        if (page != 0) {
            builder.append(
                    TextComponent.builder("<<< ")
                            .clickEvent(
                                    ClickEvent.runCommand(pageCommand.replace("{page}", String.valueOf(page - 1)))
                            )).color(KyoriColors.AQUA);
        } else {
            builder.append(
                    TextComponent.builder("<<< ").color(KyoriColors.GRAY));
        }

        builder.append((page + 1) + "/" + (pageMax + 1)).color(KyoriColors.YELLOW);

        if (page != pageMax) {
            builder.append(
                    TextComponent.builder(" >>>")
                            .clickEvent(
                                    ClickEvent.runCommand(pageCommand.replace("{page}", String.valueOf(page + 1)))
                            )).color(KyoriColors.AQUA);
        } else {
            builder.append(
                    TextComponent.builder(" >>>").color(KyoriColors.GRAY));
        }

        builder.append(" |>=====").color(KyoriColors.YELLOW);
        return builder.build();
    }

    public static <T> TextComponent getPage(Collection<T> content, int page, Function<T, TextComponent> mapping, String title, String pageCommand) {
        return getPage(content, page, 18, 1, mapping, title, pageCommand);
    }

    public static <T> TextComponent getPage(Collection<T> content, int page, int elementsPerPage, int elementSize, Function<T, TextComponent> mapping, String title, String pageCommand) {
        Collection<T> elements = getSlice(content, page, elementsPerPage);
        TextComponent.Builder builder = TextComponent.builder();
        for (int i = elements.size() * elementSize; i < 18; i++) {
            builder.append(TextComponent.newline());

        }

        builder.append(TextComponent.builder("=====<| ").color(KyoriColors.YELLOW))
                .append(TextComponent.builder(title).color(KyoriColors.AQUA))
                .append(TextComponent.builder(" |>=====").color(KyoriColors.YELLOW));
        for (T t : elements) {
            builder.append(TextComponent.newline())
                    .append(mapping.apply(t));
        }

        return builder.append(TextComponent.newline())
                .append(
                        CommandUtil.getPageFooter(page,
                                CommandUtil.pageCount(content, elementsPerPage),
                                pageCommand)).build();
    }

    public static TextComponent getHeader(String title) {
        return TextComponent.builder()
                .append(TextComponent.builder("=====<| ").color(KyoriColors.YELLOW))
                .append(TextComponent.builder(title).color(KyoriColors.AQUA))
                .append(TextComponent.builder(" |>=====").color(KyoriColors.YELLOW)).build();
    }

    public static TextComponent getBooleanField(boolean currValue, String cmd, String field, String postive, String negative) {
        return TextComponent.builder()
                .append(TextComponent.builder(field + ": ", KyoriColors.AQUA))
                .append(
                        TextComponent.builder(postive,
                                currValue ? KyoriColors.GREEN : KyoriColors.DARK_GRAY)
                                .clickEvent(
                                        ClickEvent.runCommand(cmd.replace("{bool}", "true")))
                )
                .append(" ")
                .append(
                        TextComponent.builder(negative,
                                !currValue ? KyoriColors.RED : KyoriColors.DARK_GRAY)
                                .clickEvent(
                                        ClickEvent.runCommand(cmd.replace("{bool}", "false"))))
                .build();

    }
}
