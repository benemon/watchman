package com.redhat.ukiservices.openshift.watchman.model;

public enum ConditionTypeEnum {
    AVAILABLE("Available"), PROGRESSING("Progressing");

    private final String text;


    /**
     * Private constructor for String enum
     *
     * @param text
     */
    private ConditionTypeEnum(final String text) {
        this.text = text;
    }

    /* (non-Javadoc)
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {
        return text;
    }
}
