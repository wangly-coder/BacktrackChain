package com.wly.regex.match;

import java.util.List;

/**
 * 匹配器链打印器
 */
public class MatcherChainPrinter {

    public static final String ARROW_DELIM = " -> ";
    public static final String COLLECT_DELIM = ",";
    public static final String UNION_DELIM = " / ";

    public static String printChain(MatcherWrapper head,String delim){
        String delimiter = delim == null ? ARROW_DELIM : delim;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(head.toString());
        while(head.next != null) {
            head = head.next;
            stringBuilder.append(delimiter).append(head.toString());
        }
        return stringBuilder.toString();
    }

    public static String printChain(MatcherWrapper head){
        return MatcherChainPrinter.printChain(head, ARROW_DELIM);
    }

    public static String printCollection(List<Matcher> collection){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[");
        for(Matcher matcher : collection) stringBuilder.append(matcher.toString()).append(COLLECT_DELIM);
        stringBuilder.setCharAt(stringBuilder.length() - 1, ']');
        return stringBuilder.toString();
    }

    public static String printUnionChains(List<MatcherWrapper> unionChains){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[");
        for(MatcherWrapper chain : unionChains) stringBuilder.append(MatcherChainPrinter.printChain(chain)).append(UNION_DELIM);
        stringBuilder.setLength(stringBuilder.length()-UNION_DELIM.length());
        stringBuilder.append("]");
        return stringBuilder.toString();
    }
}
