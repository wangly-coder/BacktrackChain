package com.tobethebest.regex.ast;

import com.tobethebest.regex.ast.exp.*;

import java.util.HashMap;

public class TestedRegexParser extends RegexParser{

    public static HashMap<RegexExp,String> regexExpToProtoStrMap = new HashMap<>();

    public TestedRegexParser(){}

    public TestedRegexParser(String regexString){
        super(regexString);
    }

    public String getProtoString(int startIndex){
        return this.regexString.substring(startIndex,this.cursor);
    }

    /**
     * 重置解析器
     */

    @Override
    protected RegexExp parseUnionExp() {
        int startIndex = this.cursor;
        RegexExp regexExp = super.parseUnionExp();
        if(regexExp instanceof UnionExp){
            TestedRegexParser.regexExpToProtoStrMap.put(regexExp,this.getProtoString(startIndex));
        }
        return regexExp;
    }

    @Override
    protected RegexExp parseRepeatExp() {
        int startIndex = this.cursor;
        RegexExp regexExp = super.parseRepeatExp();
        if(regexExp instanceof RepeatExp) {
            TestedRegexParser.regexExpToProtoStrMap.put(regexExp,this.getProtoString(startIndex));
        }
        return regexExp;
    }

    @Override
    protected RegexExp parseCharCollectionExp() {
        int startIndex = this.cursor;
        RegexExp regexExp = super.parseCharCollectionExp();
        if(regexExp instanceof CharCollectionExp){
            TestedRegexParser.regexExpToProtoStrMap.put(regexExp,this.getProtoString(startIndex));
        }
        return regexExp;
    }

//    @Override
//    protected RegexExp parseCharGroupExp() {
//        int startIndex = this.cursor;
//        RegexExp regexExp = super.parseCharGroupExp();
//        if(regexExp instanceof LookaroundExp) {
//            TestedRegexParser.regexExpToProtoStrMap.put(regexExp,this.getProtoString(startIndex));
//        }
//        return regexExp;
//    }
}
