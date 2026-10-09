package com.tobethebest.regex.match;

import com.tobethebest.regex.RegexMatcher;
import com.tobethebest.regex.SearchResult;
import com.tobethebest.regex.TestedRegexMatcher;
import com.tobethebest.regex.ast.ASTVisualizer;
import com.tobethebest.regex.ast.LengthMeasurer;
import com.tobethebest.regex.ast.RegexParser;
import com.tobethebest.regex.ast.TestedRegexParser;
import com.tobethebest.regex.ast.exp.RegexExp;
import com.tobethebest.regex.ast.exp.RepeatExp;
import com.tobethebest.regex.ast.exp.UnionExp;
import com.tobethebest.regex.match.matcher.ChainMatcher;
import com.tobethebest.regex.match.matcher.RepeatMatcher;
import com.tobethebest.regex.match.matcher.UnionMatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.converter.ArgumentConversionException;
import org.junit.jupiter.params.converter.ConvertWith;
import org.junit.jupiter.params.converter.SimpleArgumentConverter;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@Suite
@SelectClasses({
        TestGroupFrontAST.class,
        TestGroupIdCollect.class,
        TestGroupAndReference.class,
})
public class RegexGroupTest {}

class TestGroupFrontAST{
    RegexParser regexParser;

    @BeforeEach
    public void setUp() {
        this.regexParser = new TestedRegexParser();
    }

    // 有部分存在在RegexParserTest中，后续更改需要留意

    @DisplayName("测试捕获组与引用解析-1")
    @Test
    public void testGroupAndRef1() {
        String regex = "(ab)+\\1";
        RegexExp regexExp = regexParser.parse(regex);
        String tree = ASTVisualizer.getASTString(regexExp);
        System.out.printf("<========================\n%s========================>\n", tree);
        assertEquals("[Concat]\n" +
                        "    ├──[Repeat:{1,}]\n" +
                        "    │   └──[Group:1]\n" +
                        "    │       └──[Concat]\n" +
                        "    │           ├──[Char:a]\n" +
                        "    │           └──[Char:b]\n" +
                        "    └──[GroupRef:1]\n"
                , tree);
    }

    @DisplayName("测试捕获组与引用解析-2")
    @Test
    public void testGroupAndRef2() {
        String regex = "(ab)+\\1c";
        RegexExp regexExp = regexParser.parse(regex);
        String tree = ASTVisualizer.getASTString(regexExp);
        System.out.printf("<========================\n%s========================>\n", tree);
        assertEquals("[Concat]\n" +
                        "    ├──[Repeat:{1,}]\n" +
                        "    │   └──[Group:1]\n" +
                        "    │       └──[Concat]\n" +
                        "    │           ├──[Char:a]\n" +
                        "    │           └──[Char:b]\n" +
                        "    └──[Concat]\n" +
                        "        ├──[GroupRef:1]\n" +
                        "        └──[Char:c]\n"
                , tree);
    }

    @DisplayName("测试捕获组与引用解析-3")
    @Test
    public void testGroupAndRef3() {
        String regex = "((ab)+)\\1c\\2";
        RegexExp regexExp = regexParser.parse(regex);
        String tree = ASTVisualizer.getASTString(regexExp);
        System.out.printf("<========================\n%s========================>\n", tree);
        assertEquals("[Concat]\n" +
                        "    ├──[Group:1]\n" +
                        "    │   └──[Repeat:{1,}]\n" +
                        "    │       └──[Group:2]\n" +
                        "    │           └──[Concat]\n" +
                        "    │               ├──[Char:a]\n" +
                        "    │               └──[Char:b]\n" +
                        "    └──[Concat]\n" +
                        "        ├──[GroupRef:1]\n" +
                        "        └──[Concat]\n" +
                        "            ├──[Char:c]\n" +
                        "            └──[GroupRef:2]\n"
                , tree);
    }

    @DisplayName("测试捕获组与引用解析-异常测试")
    @Test
    public void testGroupAndRef_Exception() {
        String regex = "((ab)+)\\1c\\3";
        Exception e = assertThrows(RuntimeException.class, () -> regexParser.parse(regex));
        assertEquals("引用编号为3的捕获组不存在", e.getMessage());
    }


    @DisplayName("测试LengthMeasurer是否正确-捕获组与引用")
    @ParameterizedTest
    @CsvSource(delimiterString = "=",value = {
            "(ab)+\\1=4=-1",
            "((ab)+)\\1c\\2=7=-1",
            "(ab)+1(2*)\\1c\\2=6=-1"
    })
    public void test_LengthMeasurer_accuracy_group_reference(String regex, int minLength,int maxLength){
        this.regexParser.closeNestCheck();
        RegexExp regexExp = this.regexParser.parse(regex);
        LengthMeasurer.LengthInfo lengthInfo = LengthMeasurer.measureLength(regexExp);
        System.out.println("<======================对比MinLength=========================>");
        assertEquals(minLength,lengthInfo.minLength);
        System.out.println("<======================对比MaxLength=========================>");
        assertEquals(maxLength,lengthInfo.maxLength);
    }

    @DisplayName("测试TestedMatcherChainBuilder构建GroupMatcher和GroupRefMatcher-1")
    @Test
    public void test_TestedMatcherChainBuilder_GroupMatcher_1() {
        String regex = "((ab)+)c\\1";
        RegexExp regexExp = regexParser.parse(regex);
        ChainMatcher chainMatcher = TestedMatcherChainBuilder.build(regexExp);
        String matcherString = MatcherChainPrinter.printChain(chainMatcher);
        System.out.printf("<========================\n%s\n========================>\n", matcherString);
        assertEquals("[GS:1] -> [RS:[Name:1-(ab)+,Pre:null]]" +
                " -> [GS:2] -> [String:ab] -> [GE:2] -> [RE:1]" +
                " -> [GE:1] -> [String:c] -> [GRef:1]",matcherString);
    }

    @DisplayName("测试TestedMatcherChainBuilder构建GroupMatcher和GroupRefMatcher-2")
    @Test
    public void test_TestedMatcherChainBuilder_GroupMatcher_2() {
        String regex = "(a|(b)|c)\\1";
        RegexExp regexExp = regexParser.parse(regex);
        ChainMatcher chainMatcher = TestedMatcherChainBuilder.build(regexExp);
        String matcherString = MatcherChainPrinter.printChain(chainMatcher);
        System.out.printf("<========================\n%s\n========================>\n", matcherString);
        assertEquals("[GS:1] -> [Union:[Pre:null,Chains:[" +
                "[String:a] -> [GE:1] -> [GRef:1] / [GS:2] -> [String:b] -> [GE:2] -> [GE:1] -> [GRef:1] " +
                "/ [String:c] -> [GE:1] -> [GRef:1]]" +
                "]] -> [GE:1] -> [GRef:1]",matcherString);
    }

    // 来自于TestedMatcherChainBuilderAndPrinterTest中，有别的带Group的也在其中，后续更改需要留意
    @DisplayName("测试TestedMatcherChainBuilder构建GroupMatcher和GroupRefMatcher-3")
    @Test
    public void test_TestedMatcherChainBuilder_GroupMatcher_3() {
        String regexString = "(a(b|c)d){1,2}";
        RegexExp regexExp = regexParser.parse(regexString);
        ChainMatcher chainMatcher = TestedMatcherChainBuilder.build(regexExp);
        String matcherChainString = MatcherChainPrinter.printChain(chainMatcher);
        System.out.printf("<========================\n%s\n========================>\n", matcherChainString);
        assertEquals("[RS:[Name:1-(a(b|c)d){1,2},Pre:null]]" +
                " -> [GS:1] -> [String:a] -> [GS:2] -> [Union:[Pre:1,Chains:[" +
                "[String:b] -> [GE:2] -> [String:d] -> [GE:1] -> [RE:1]" +
                " / [String:c] -> [GE:2] -> [String:d] -> [GE:1] -> [RE:1]" +
                "]]]" +
                " -> [GE:2] -> [String:d] -> [GE:1] -> [RE:1]",matcherChainString);
    }

    @DisplayName("测试非捕获组")
    @ParameterizedTest
    @CsvSource(delimiterString = "=",value = {
            "(?:a(b|c)d)=a(b|c)d",
            "(a|(?:b)|c)\\1=(a|b|c)\\1",
            "(ab)+1(?:2*)\1c=(ab)+12*\1c"
    })
    public void test_nonCaptureGroup(String regex, String simplyRegx){
        RegexExp regexExp = this.regexParser.parse(regex);
        String tree = ASTVisualizer.getASTString(regexExp);
        RegexExp simplyRegexExp = this.regexParser.parse(simplyRegx);
        String simplyTree = ASTVisualizer.getASTString(simplyRegexExp);
        assertEquals(tree,simplyTree);
    }

    @DisplayName("测试具名捕获组、名字引用以及名字引用下\\k转义成普通字符k的兼容性")
    @ParameterizedTest
    @CsvSource(delimiterString = "=",value = {
            "(?<group1>a(?<group2>b|c)d)\\k<group1>\\k<group2>=(a(b|c)d)\\1\\2",
            "(?<group1>ab)+1(?<group2>2*)\\k<group2>\\k<group1>c=(ab)+1(2*)\\2\\1c",
            "(?<group1>a(?<group2>b|c)d)\\k<group1>\\k=(a(b|c)d)\\1k",
            "(?<group1>ab)+1(?<group2>2*)\\k\\k<group1>c=(ab)+1(2*)k\\1c",
    })
    public void test_namedCaptureGroup_and_reference(String regex, String namedGroupRegex){
        RegexExp regexExp = this.regexParser.parse(regex);
        String tree = ASTVisualizer.getASTString(regexExp);
        RegexExp simplyRegexExp = this.regexParser.parse(namedGroupRegex);
        String simplyTree = ASTVisualizer.getASTString(simplyRegexExp);
        assertEquals(tree,simplyTree);
    }

    @DisplayName("测试具名捕获组和名字引用的异常处理")
    @ParameterizedTest
    @CsvSource(delimiterString = "=",value = {
            "(?<123>a(b|c)d)=捕获组名称123不能以数字开头，开头只能是字母大小写",
            "(?<group1>a(?<group1>b|c)d)=重复的捕获组名称group1",
            "(?<group1>ab)+1(?<group2>2*)\\k<group2>\\k<group3>c=具名捕获组引用，引用的组名group3不存在",
    })
    public void test_namedCaptureGroup_exceptionHandle(String regex, String eMessage){
        Exception exception = assertThrows(RuntimeException.class,() -> this.regexParser.parse(regex));
        assertEquals(eMessage,exception.getMessage());
    }
}

class TestGroupIdCollect extends TestedMatcherChainBuilder{
    RegexParser regexParser;

    private static final TestGroupIdCollect INSTANCE = new TestGroupIdCollect();

    @BeforeEach
    public void setUp() {
        this.regexParser = new TestedRegexParser();
    }

    protected static class GroupIdCollectTestContext extends TestedMatcherChainBuilder.MatcherBuilderContext{
        public Map<String,List<?>> groupIdValidatorMap;
        public GroupIdCollectTestContext(Map<String, List<?>> groupIdValidatorMap){
            this.groupIdValidatorMap = groupIdValidatorMap;
        }
    }

    public static ChainMatcher build(RegexExp regexExp,Map<String,List<?>> groupIdValidatorMap){
        TestedMatcherChainBuilder.clear();
        System.out.println("使用GroupId收集测试环境");
        return regexExp.accept(INSTANCE,new GroupIdCollectTestContext(groupIdValidatorMap));
    }

    @Override
    public ChainMatcher visit(UnionExp unionExp, MatcherBuilderContext context) {
        UnionMatcher unionMatcher = (UnionMatcher) super.visit(unionExp, context);
        int id = TestedMatcherChainBuilder.matcherToIdMap.get(unionMatcher);
        String protoStr = TestedMatcherChainBuilder.matcherToProtoStrMap.get(unionMatcher);
        String name = String.format("%d-%s",id,protoStr);
        // 校验
        GroupIdCollectTestContext groupIdCollectTestContext = (GroupIdCollectTestContext) context;
        List<List<Integer>> unionGroupIds = (List<List<Integer>>) groupIdCollectTestContext.groupIdValidatorMap.get(name);
        // 断言
        assertEquals(unionGroupIds,unionMatcher.unionChainGroupIdList);
        return unionMatcher;
    }

    @Override
    public ChainMatcher visit(RepeatExp repeatExp, MatcherBuilderContext context) {
        RepeatMatcher.RepeatStartMatcher repeatMatcher = (RepeatMatcher.RepeatStartMatcher) super.visit(repeatExp, context);
        RepeatMatcher.RepeatEndMatcher repeatEndMatcher = repeatMatcher.repeatEndMatcher;
        int id = TestedMatcherChainBuilder.matcherToIdMap.get(repeatEndMatcher);
        String protoStr = TestedMatcherChainBuilder.matcherToProtoStrMap.get(repeatEndMatcher);
        String name = String.format("%d-%s",id,protoStr);
        // 校验
        GroupIdCollectTestContext groupIdCollectTestContext = (GroupIdCollectTestContext) context;
        List<Integer> repeatGroupIds = (List<Integer>) groupIdCollectTestContext.groupIdValidatorMap.get(name);
        // 断言
        assertEquals(repeatGroupIds,repeatEndMatcher.groupIdList);
        return repeatMatcher;
    }

    private static class StringTogroupIdValidatorMap extends SimpleArgumentConverter{

        @Override
        protected Object convert(Object o, Class<?> aClass) throws ArgumentConversionException {
            String convertedStr = (String) o;
            // 划分多对键值对
            String[] pairs = convertedStr.split("(?<=>),(?=<)");
            String key;
            String valueStr;
            Map<String,List<Object>> map = new HashMap<>();
            // 解析key和value
            for(String pair:pairs){
                int splitIndex = pair.indexOf(',');
                key = pair.substring(1,splitIndex);
                valueStr = pair.substring(splitIndex+1,pair.length()-1);
                map.put(key,this.parseList(valueStr));
            }
            return map;
        }

        private List<Object> parseList(String listStr){
            String valueStr = listStr.substring(1,listStr.length()-1);
            List<Object> result = new ArrayList<>();
            // 双层list
            if(valueStr.startsWith("[")){
                String[] valueListStr = valueStr.split("(?<=]),(?=\\[)");
                for(String value:valueListStr){
                    result.add(this.parseList(value));
                }
            }else{
                String[] valueIntegerStr = valueStr.split(",");
                if(valueIntegerStr.length == 1 && valueIntegerStr[0].isEmpty()) return new ArrayList<>();
                for (String valueInteger:valueIntegerStr){
                    result.add(Integer.parseInt(valueInteger));
                }
            }
            return result;
        }
    }

    @DisplayName("校验groupId收集器代码逻辑是否正确")
    @ParameterizedTest
    @CsvSource(delimiterString = "=",value = {
            "((a)|(b))|(c)=<1-((a)|(b))|(c),[[1,2,3],[4]]>,<2-(a)|(b),[[2],[3]]>",
            "((a)b(c))+=<1-((a)b(c))+,[1,2,3]>",
            "((a)(1(2)3)+(b))+=<1-((a)(1(2)3)+(b))+,[1,2,3,4,5]>,<2-(1(2)3)+,[3,4]>",
            "(ab(1|(2)|3)c)+=<1-(ab(1|(2)|3)c)+,[1,2,3]>,<1-1|(2)|3,[[],[3],[]]>",
    })
    public void test_GroupIdCollectTest_NotNest(String regexString, @ConvertWith(StringTogroupIdValidatorMap.class) Map<String, List<?>> groupIdValidatorMap){
        RegexExp regexExp = this.regexParser.parse(regexString);
        TestGroupIdCollect.build(regexExp,groupIdValidatorMap);
    }
}

class TestGroupAndReference{

    @DisplayName("测试捕获组与引用-match()方法测试")
    @ParameterizedTest
    @CsvSource(delimiterString = "=",value ={
            "^(\\w+)\\s+(\\w+)\\s+\\2\\s+\\1$=love you you love=true",
            "(\\d\\d\\d)-(\\d\\d\\d)-\\2-\\1=123-456-456-123=true",
            "(\\w+)\\s+\\1-\\1=repeat repeat-repeat=true",
            "(\\w\\w\\w)\\s+\\1$=abcabc=false",
            "(\\d\\d)(\\d\\d)\\1\\2\\1=1234123412=true",
            "^(\\d)\\1(\\d)\\2\\1\\2(\\d)\\3$=11221233=true",
            "^(\\d\\d)(\\d\\d)\\2\\1\\2\\1=123434123412=true",
            "(\\d\\d)-(\\d\\d)-(\\d\\d)\\3-\\2-\\1=11-22-33-33-22-11=false",
            "^(\\w+)\\s+\\1\\1$=go gogo=true",
            "^(\\w)\\w+\\s+\\w+\\s+\\1$=abc def g=false",
            "((ab|cd)\\d)\\1=ab1ab1=true",
            "(\\d{2,3})-\\1=123-123=true",
            "^(cat|dog)s?\\s\\1$=cats cat=true",
            "^(\\w+)\\s\\1$=hello hello=true",
            "(\\w+)\\s(\\w+)\\s\\2\\s\\1=hello world world hello=true",
            "(\\d{2,4})-(\\d{2,4})-\\1=1234-4567-123=false",
            "((ab|cd){2})\\1=abcdabc=false",
            "(\\d+)-(\\w+)-\\1-\\2$=123-ab-123-abc=false",
            "(\\d{3})\\s(\\d{3})\\s\\1\\2=123 456 123456=true",
            "(\\d{2})\\1(\\d{2})\\2=11112222=true",
    })
    public void test_group_reference_match(String regex,String str,boolean expected){
        RegexMatcher regexMatcher = new TestedRegexMatcher(regex);
        assertEquals(expected,regexMatcher.match(str));
    }

    protected static class StringToGroupStrsListConvertor extends SimpleArgumentConverter{

        @Override
        protected Object convert(Object o, Class<?> aClass) throws ArgumentConversionException {
            String convertedStr = (String) o;
            return this.parseList(convertedStr);
        }

        public List<List<String>> parseList(String listStr){
            List<List<String>> result = new ArrayList<>();
            String contentStr = listStr.substring(1,listStr.length()-1);
            if(contentStr.isEmpty()) return result;
            String[] groupStrs = contentStr.split("(?<=]),(?=\\[)");
            for(String groupStr:groupStrs){
                String groupStrContent = groupStr.substring(1,groupStr.length()-1);
                List<String> groupStrList = new ArrayList<>(Arrays.asList(groupStrContent.split(",")));
                result.add(groupStrList);
            }
            return result;
        }
    }

    @DisplayName("测试捕获组与引用-search()方法、SearchResult的group()方法以及所有的捕获组内容是否正确")
    @ParameterizedTest
    @CsvSource(delimiterString = "=",value = {
            "^((a+b|cd)+)\\d{2,3}\\1$=aabcdaab123aabcdaab=[[aabcdaab123aabcdaab,aabcdaab,aab]]",
            "(\\d{2,3})(a+b+|c+d+)+\\1=123aabbccdd123=[[123aabbccdd123,123,ccdd]]",
            "((x+y|z{2,3})+)\\1=xyzxyz=[]",
            "^(\\d+)(a{2,3}b+|c+d+)+\\1$=77aaabccdd77=[[77aaabccdd77,77,ccdd]]",
            "((ab|c+d)+)\\1=abcdabcd=[[abcdabcd,abcd,cd]]",
            "^(\\d{2,3})(a+b+|c{2,3})+\\1$=123aabbcc123=[[123aabbcc123,123,cc]]",
            "^((a{2,3}b|cd)+)\\1$=aabcdaabcd=[[aabcdaabcd,aabcd,cd]]",
            "((ab+|c{2,3})+)\\1=abbabb=[[abbabb,abb,abb]]",
            "(\\d+)(a+b+|c+d+)+\\1$=99aabbccdd99=[[99aabbccdd99,99,ccdd]]",
            "((a{2,3}|b+c)+)\\d+\\1$=aabcaabbc123aabcaabbc=[[aabcaabbc123aabcaabbc,aabcaabbc,bbc]]",
            "(\\\\w{2,4}\\d)\\w\\1=abc1dabc1=[]",
            "((ab|cd)+)\\d+\\1=abcdcd123abcdcd=[[abcdcd123abcdcd,abcdcd,cd]]",
            "(\\d{2,4}[a-z])\\d\\1=1234a51234a=[[1234a51234a,1234a]]",
            "((a{2,3}|b+c)+)\\d+\\1=aabcaabbc123aabcaabbc=[[aabcaabbc123aabcaabbc,aabcaabbc,bbc]]",
            "((xy|z)+)\\d+\\1=xyzxyz8xyzxyz=[[xyzxyz8xyzxyz,xyzxyz,z]]",
            "(\\w{3}\\d{2})\\d\\1=abc123abc12=[[abc123abc12,abc12]]",
            "((a+b)+)\\d+\\1=aabaaab4aabaaab=[[aabaaab4aabaaab,aabaaab,aaab]]",
            "(([a-z]+\\d)+)\\w\\1=ab1cd2ab1cd2=[[b1cd2ab1cd2,b1cd2,cd2]]",
            "(\\d+[a-z]{2})\\d\\1=123ab4123ab=[[123ab4123ab,123ab]]",
            "((ab+c)+)\\d+\\1=ababbc3ababbc=[]",
            "((\\d{2,3}[a-z])+)\\d\\1=12a34b512a34b=[[12a34b512a34b,12a34b,34b]]",
            "(([a-z]{2}\\d?)+)\\d+\\1=ab1cd2ef9ab1cd2ef=[[ab1cd2ef9ab1cd2ef,ab1cd2ef,ef]]", // ?
            "((\\w\\d)+)\\w\\1=a1b2c3da1b2c=[]",
            "((a+|b+)+)\\d+\\1=aabbaabb6aabbaabb=[[aabbaabb6aabbaabb,aabbaabb,bb]]",
            "(\\d{2}[a-z]{2}\\d)\\d\\1=12ab3412ab3=[[12ab3412ab3,12ab3]]",
            "(([a-z]+\\d+)+)\\w\\1=abc12def3xabc12def3=[[abc12def3xabc12def3,abc12def3,def3]]",
            "((ab|a+b)+)\\d+\\1=aabab3aabab=[[aabab3aabab,aabab,ab]]",
            "((\\d+[a-z])+)\\d\\1=1a2b3c4d1a2b3=[]",
            "(([a-z]{2,3}\\d)+)\\w\\1=ab1cde2ab1cde=[]",
            "((a{2,3}b|c+d)+)\\d+\\1=aabcccd7aabcccd=[[aabcccd7aabcccd,aabcccd,cccd]]",
            // 三层量词嵌套及以上
            "(([ab]{1,2}|[12]{1,2}){1,3}[AB]){2,3}=ab12Aab12BabA=[[ab12Aab12BabA,abA,ab]]",
            "((([AB]{1,2}[ab]+){1,2}[12]{1,2})[ab]{1,2}){1,3}=Aab12Baab12ab=[[Baab12ab,Baab12ab,Baab12,Baab]]",
            "(([ab]{2,5}|[12]+){2,5}[AB]*){2,3}=ab12Aab12BabA=[[ab12Aab12B,ab12B,12]]",
            "((a+?|b{2,4}c)+d)+=adxbbbcadx=[[ad,ad,a],[bbbcad,bbbcad,a]]",
            "((a{1,3}?|b{2,3})+c)+=abbcxabbcc=[[abbc,abbc,bb],[abbc,abbc,bb]]",
            "((a+?b|c{2,3})+d)+=abdccabdxx=[[abdccabd,ccabd,ab]]",
            // 四层
            "(((a{1,2}?|b{2,3}c)+d)+e)+=adexadbbcade=[[ade,ade,ad,a],[adbbcade,adbbcade,bbcad,a]]",
            "(((a+?b{1,2}|c{2,3})+d)+e)+=abbdeccabbde=[[abbdeccabbde,ccabbde,ccabbd,abb]]",
            "(((a+?|b{2,3}c)+d)+e)+=adexadbbcde=[[ade,ade,ad,a],[adbbcde,adbbcde,bbcd,bbc]]",
            "(((a{1,3}?|b{2,3})+c)+d)+=acdxaccdxx=[[acd,acd,ac,a]]",
            // 五层
            "((((a+?|b{2,3})+c)+d)+e)+=acdeacdeacde=[[acdeacdeacde,acde,acd,ac,a]]",
            "((((a+?b|c)+d)+e)+f)+=abdefabdef=[[abdefabdef,abdef,abde,abd,ab]]",
    })
    public void test_group_reference_search(String regex,String str,@ConvertWith(StringToGroupStrsListConvertor.class) List<List<String>> groupStrs){
        RegexMatcher regexMatcher = new TestedRegexMatcher(regex,true);
        List<List<String>> result = new ArrayList<>();
        SearchResult searchResult = regexMatcher.search(str);
        if(searchResult != null){
            do result.add(searchResult.getGroupStrList());
            // 进行后续查找
            while(searchResult.next());
        }
        assertEquals(groupStrs,result);
    }

    @DisplayName("测试捕获组与引用-具名捕获组与名字引用测试")
    @ParameterizedTest
    @CsvSource(delimiterString = "=",value ={
            "^(?<group1>\\d+)(?<group2>a{2,3}b+|c+d+)+\\k<group1>$=77aaabccdd77=[[77,ccdd]]",
            "(?<group1>(?<group2>ab|c+d)+)\\k<group1>=abcdabcd=[[abcd,cd]]",
            "^(?<group1>\\d{2,3})(?<group2>a+b+|c{2,3})+\\k<group1>$=123aabbcc123=[[123,cc]]",
            "^(?<group1>(?<group2>a{2,3}b|cd)+)\\k<group1>$=aabcdaabcd=[[aabcd,cd]]",
            "(?<group1>(?<group2>ab+|c{2,3})+)\\k<group1>=abbabb=[[abb,abb]]",
    })
    public void test_named_group_reference(String regex,String str,@ConvertWith(StringToGroupStrsListConvertor.class) List<List<String>> namedGroupStrs){
        RegexMatcher regexMatcher = new TestedRegexMatcher(regex);
        Set<String> groupNameSet = regexMatcher.getGroupNameSet();
        List<List<String>> result = new ArrayList<>();
        SearchResult searchResult = regexMatcher.search(str);
        if(searchResult != null){
            do {
                // 找到所有的具名捕获组匹配的组匹配串
                List<String> namedGroupStrList = new ArrayList<>();
                groupNameSet.forEach(groupName -> namedGroupStrList.add(searchResult.group(groupName)));
                result.add(namedGroupStrList);
            }
                // 进行后续查找
            while(searchResult.next());
        }
        assertEquals(namedGroupStrs,result);
    }
}