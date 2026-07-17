package com.wly.regex.match;

public class StringMatcher implements Matcher{
    public String string;

    public StringMatcher(String string){
        this.string = string;
    }

    @Override
    public String toString() {
        return String.format("[String:%s]",this.string);
    }
}
