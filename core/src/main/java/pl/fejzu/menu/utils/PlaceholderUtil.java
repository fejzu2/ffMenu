package pl.fejzu.menu.utils;

import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@UtilityClass
public class PlaceholderUtil {

    public static String replace(String text, Map<String, Object> placeholders) {
        if (text == null || placeholders == null || placeholders.isEmpty()) {
            return text;
        }

        String result = text;
        for (Map.Entry<String, Object> entry : placeholders.entrySet()) {
            if (entry.getValue() == null) {
                continue;
            }

            String placeholder = "{" + entry.getKey() + "}";
            String value = String.valueOf(entry.getValue());
            result = result.replace(placeholder, value);
        }

        return result;
    }

    public static List<String> replace(List<String> lines, Map<String, Object> placeholders) {
        if (lines == null || placeholders == null || placeholders.isEmpty()) {
            return lines;
        }

        return lines.stream()
                .map(line -> replace(line, placeholders))
                .collect(Collectors.toList());
    }

    @SafeVarargs
    public static Map<String, Object> merge(Map<String, Object>... maps) {
        Map<String, Object> result = new java.util.HashMap<>();
        for (Map<String, Object> map : maps) {
            if (map != null) {
                result.putAll(map);
            }
        }
        return result;
    }
}
