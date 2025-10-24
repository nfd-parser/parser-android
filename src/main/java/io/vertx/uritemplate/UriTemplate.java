package io.vertx.uritemplate;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UriTemplate {
    private final String template;
    private final Pattern pattern;

    public UriTemplate(String template) {
        this.template = template;
        this.pattern = Pattern.compile("\\{([^}]+)\\}");
    }

    public static UriTemplate of(String template) {
        return new UriTemplate(template);
    }

    @Override
    public String toString() {
        return template;
    }

    public String expandToString(Map<String, Object> variables) {
        String result = template;
        Matcher matcher = pattern.matcher(template);
        
        while (matcher.find()) {
            String variableName = matcher.group(1);
            Object value = variables.get(variableName);
            String stringValue = value != null ? value.toString() : "";
            result = result.replace(matcher.group(0), stringValue);
        }
        
        return result;
    }

    public String expandToString(Object... variables) {
        String result = template;
        Matcher matcher = pattern.matcher(template);
        int index = 0;
        
        while (matcher.find() && index < variables.length) {
            Object value = variables[index++];
            String stringValue = value != null ? value.toString() : "";
            result = result.replace(matcher.group(0), stringValue);
        }
        
        return result;
    }
}
