package mg.itu.util;

import java.util.Map;

public class Session {
    private Map<String, Object> attributes;

    public Session(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public Session() {
    }

    public Map<String, Object> getAllAttributesMap() {
        return attributes;
    }

    public void setAllAttributesMap(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    public void setAttribute(String name, Object value) {
        attributes.put(name, value);
    }

    public void removeAttribute(String name) {
        attributes.remove(name);
    }

    public void modifyAttribute(String name, Object value) {
        if (attributes.containsKey(name)) {
            attributes.put(name, value);
        }
    }

    public void clearAttributes() {
        attributes.clear();
    }
}
