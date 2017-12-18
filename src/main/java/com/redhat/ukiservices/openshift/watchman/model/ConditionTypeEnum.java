package com.redhat.ukiservices.openshift.watchman.model;

public enum ConditionTypeEnum {
    AVAILABLE("Available");

    private final String text;


    /**
     * Private constructor for String enum
     *
     * @param text of enum
     */
    ConditionTypeEnum(final String text) {
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
