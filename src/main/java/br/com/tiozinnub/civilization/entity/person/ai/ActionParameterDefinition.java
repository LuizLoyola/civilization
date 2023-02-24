package br.com.tiozinnub.civilization.entity.person.ai;

public class ActionParameterDefinition<T> {
    private final String name;
    private final Class<T> type;
    private T defaultValue = null;
    private boolean defaultValueIsNull = false;

    public ActionParameterDefinition(String name, Class<T> type) {
        this.name = name;
        this.type = type;
    }

    public boolean isRequired() {
        return this.defaultValue == null && !this.defaultValueIsNull;
    }

    public ActionParameterDefinition<T> defaultValue(T value) {
        this.defaultValue = value;
        if (value == null) this.defaultValueIsNull = true;
        return this;
    }
}
