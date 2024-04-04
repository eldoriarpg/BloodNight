package de.eldoria.bloodnight.command.util;

import de.eldoria.eldoutilities.localization.MessageComposer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import static de.eldoria.eldoutilities.localization.ILocalizer.escape;

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
        return (int) Math.floor(Math.max(collection.size() - 1, 0) / (double) size);
    }


    public static String getPageFooter(int page, int pageMax, String pageCommand) {
        MessageComposer builder = MessageComposer.create();

        builder.text("<yellow>=====<| ");

        if (page != 0) {
            builder.text("<click:run_command:'%s'><field><<< </click>",
                    pageCommand.replace("{page}", String.valueOf(page - 1)));
        } else {
            builder.text("<inactive><<< ");
        }

        builder.text("<yellow>%s/%s", page + 1, pageMax + 1);

        if (page != pageMax) {
            builder.text("<click:run_command:'%s'><field> >>>",
                    pageCommand.replace("{page}", String.valueOf(page + 1)));
        } else {
            builder.text("<inactive> >>>");
        }


        builder.text("<yellow> |>=====");
        return builder.build();
    }

    public static String getFooter() {
        return "<yellow>=====<|        |>=====";
    }

    public static <T> Optional<Integer> findPage(Collection<T> content, int pageSize, Predicate<T> predicate) {
        Iterator<T> iterator = content.iterator();
        int page = 0;
        while (iterator.hasNext()) {
            for (int i = 0; i < pageSize; i++) {
                if (!iterator.hasNext()) break;
                T next = iterator.next();
                if (predicate.test(next)) return Optional.of(page);
            }
            page++;
        }
        return Optional.empty();
    }

    public static <T> String getPage(Collection<T> content, int page, Function<T, String> mapping, String title, String pageCommand) {
        return getPage(content, page, 18, 1, mapping, title, pageCommand);
    }

    public static <T> String getPage(Collection<T> content, int page, int elementsPerPage, int elementSize, Function<T, String> mapping, String title, String pageCommand) {
        Collection<T> elements = getSlice(content, page, elementsPerPage);
        var builder = MessageComposer.create();
        for (int i = elements.size() * elementSize; i < 18; i++) {
            builder.newLine();
        }

        builder.text("<yellow>=====<| <field>%s<yellow> |>=====", escape(title));
        builder.text(elements.stream().map(mapping).toList());

        return builder.newLine().text(getPageFooter(page, CommandUtil.pageCount(content, elementsPerPage), pageCommand)).build();
    }

    public static String getHeader(String title) {
        return "<yellow>=====<| <field>%s <yellow>|>=====".formatted(escape(title));
    }

    public static String getBooleanField(boolean currValue, String cmd, String field, String postive, String negative) {
        return "<field>%s: <click:run_command:'%s'><%s>%s</click> <click:run_command:'%s'><%s>%s</click>".formatted(
                escape(field),
                cmd.replace("{bool}", "true"), currValue ? "active" : "inactive", escape(postive),
                cmd.replace("{bool}", "false"), !currValue ? "remove" : "inactive", escape(negative));
    }

    public static String changeButton(String command) {
        return changeButton(command, "action.change", "change");
    }
    public static String changeButtonWithInput(String command) {
        return changeButtonWithInput(command, "action.change", "change");
    }

    public static String changeButton(String command, String label, String color) {
        return "<click:run_command:'%s'><%s>[%s]</click>".formatted(command, color, escape(label));
    }
    public static String changeButtonWithInput(String command, String label, String color) {
        return "<click:suggest_command:'%s'><%s>[%s]</click>".formatted(command, color, escape(label));
    }

    public static String changeableValueWithInput(String field, Object value, String command) {
        return "%s %s".formatted(value(field, value), changeButtonWithInput(command));
    }

    public static String value(String field, Object value) {
        return "<field>%s: <gold>%s".formatted(escape(field), value);
    }

    public static String getToggleField(boolean currValue, String cmd, String field) {
        String newCmd = cmd.replace("{bool}", String.valueOf(!currValue));
        return "<click:run_command:'%s'><%s>[%s]".formatted(newCmd, currValue ? "green" : "dark_gray", escape(field));
    }
}
