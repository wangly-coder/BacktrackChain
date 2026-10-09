package com.tobethebest.regex;

import com.tobethebest.regex.ast.TestedRegexParser;
import com.tobethebest.regex.match.TestedMatchContext;

public class TestedRegexMatcher extends RegexMatcher{

    public TestedRegexMatcher(String regexString, boolean isCloseCheck) {
        TestedRegexParser testedRegexParser = new TestedRegexParser(regexString);
        if (isCloseCheck) testedRegexParser.closeNestCheck();
        this.matchContext = new TestedMatchContext(testedRegexParser);
    }

    public TestedRegexMatcher(String regexString) {
        this(regexString,false);
    }
}
