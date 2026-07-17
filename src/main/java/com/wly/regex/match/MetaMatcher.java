package com.wly.regex.match;

public class MetaMatcher implements Matcher{
    public String metaValue;

    public MetaMatcher(String metaValue) {
        this.metaValue = metaValue;
    }

    @Override
    public String toString() {
        return String.format("[Meta:%s]", this.metaValue);
    }

}
