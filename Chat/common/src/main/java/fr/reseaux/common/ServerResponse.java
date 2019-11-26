package fr.reseaux.common;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServerResponse implements Serializable {

    private boolean success;

    private String content;

    public ServerResponse(boolean success, String content) {
        this.success = success;
        this.content = content;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getRequestAttribute(String attributeName) {

        Pattern attributePattern = Pattern.compile(".*-" + attributeName + ":\\{(.*?)}.*");
        Matcher attributeMatcher = attributePattern.matcher(this.content);
        if (attributeMatcher.matches()) {
            return attributeMatcher.group(1);
        }
        return null;
    }
}
