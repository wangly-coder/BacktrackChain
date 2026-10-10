package com.tobethebest.regex.match;

import com.tobethebest.regex.RegexMatcher;
import com.tobethebest.regex.SearchResult;
import com.tobethebest.regex.TestedRegexMatcher;
import com.tobethebest.regex.ast.ASTVisualizer;
import com.tobethebest.regex.ast.RegexParser;
import com.tobethebest.regex.ast.TestedRegexParser;
import com.tobethebest.regex.ast.exp.LookaroundExp;
import com.tobethebest.regex.ast.exp.RegexExp;
import com.tobethebest.regex.match.matcher.ChainMatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@Suite
@SelectClasses(value = {
        TestLookaroundFrontAndBuild.class,
        TestLookaroundMatcher.class,
        TestBoundaryMatcher.class,
})
public class AssertionTest {
    public static final HashMap<LookaroundExp.LookaroundType,String> lookTypeToTypeStrMap = new HashMap<>();
    static {
        lookTypeToTypeStrMap.put(LookaroundExp.LookaroundType.RIGHT_POSITIVE,"(?=)");
        lookTypeToTypeStrMap.put(LookaroundExp.LookaroundType.RIGHT_NEGATIVE,"(?!)");
        lookTypeToTypeStrMap.put(LookaroundExp.LookaroundType.LEFT_POSITIVE,"(?<=)");
        lookTypeToTypeStrMap.put(LookaroundExp.LookaroundType.LEFT_NEGATIVE,"(?<!)");
    }

    public static String getLookTypeStr(LookaroundExp.LookaroundType lookaroundType){
        Optional<String> typeStr = Optional.ofNullable(lookTypeToTypeStrMap.get(lookaroundType));
        return typeStr.orElseThrow(() -> new RuntimeException("不应该出现的错误。未知的环视类型%s " + lookaroundType));
    }
}

class TestLookaroundFrontAndBuild {
    RegexParser regexParser;

    @BeforeEach
    public void setUp() {
        this.regexParser = new TestedRegexParser();
    }

    @DisplayName("测试环视嵌套与其它语句结合")
    @Test
    public void test_nest_and_combineWith_other_sentences(){
        String regex = "a(?=bc(?<!(1|2)))+d";
        RegexExp regexExp = regexParser.parse(regex);
        String tree = ASTVisualizer.getASTString(regexExp);
        System.out.printf("<========================\n%s========================>\n", tree);
        assertEquals("[Concat]\n" +
                        "    ├──[Char:a]\n" +
                        "    └──[Concat]\n" +
                        "        ├──[Lookaround:(?=)]\n" +
                        "        │   └──[Concat]\n" +
                        "        │       ├──[Char:b]\n" +
                        "        │       └──[Concat]\n" +
                        "        │           ├──[Char:c]\n" +
                        "        │           └──[Lookaround:(?<!)]\n" +
                        "        │               └──[Group:1]\n" +
                        "        │                   └──[Union]\n" +
                        "        │                       ├──[Char:1]\n" +
                        "        │                       └──[Char:2]\n" +
                        "        └──[Char:d]\n", tree);
    }

    @DisplayName("测试环视表达式外部量词表达式的优化-+的优化")
    @Test
    public void test_lookaround_withRepeatPlus_optimization(){
        String regex = "(a(?=b)+)+";
        RegexExp regexExp = regexParser.parse(regex);
        String tree = ASTVisualizer.getASTString(regexExp);
        System.out.printf("<========================\n%s========================>\n", tree);
        assertEquals(    "[Repeat:{1,}]\n" +
                        "    └──[Group:1]\n" +
                        "        └──[Concat]\n" +
                        "            ├──[Char:a]\n" +
                        "            └──[Lookaround:(?=)]\n" +
                        "                └──[Char:b]\n", tree);
    }

    @DisplayName("测试环视表达式外部量词表达式的优化-*的优化")
    @Test
    public void test_lookaround_withRepeatStar_optimization(){
        String regex = "(a(?=b)*)+";
        RegexExp regexExp = regexParser.parse(regex);
        String tree = ASTVisualizer.getASTString(regexExp);
        System.out.printf("<========================\n%s========================>\n", tree);
        assertEquals(    "[Repeat:{1,}]\n" +
                "    └──[Group:1]\n" +
                "        └──[Concat]\n" +
                "            ├──[Char:a]\n" +
                "            └──[Repeat:{0,1}]\n"+
                "                └──[Lookaround:(?=)]\n" +
                "                    └──[Char:b]\n", tree);
    }

    @DisplayName("测试环视表达式构建成环视匹配器")
    @Test
    public void test_build_lookaroundMatcher(){
        String regexString = "a\\w|b(?=c(1))|\\dd";
        RegexExp regexExp = this.regexParser.parse(regexString);
        ChainMatcher chainMatcher = TestedMatcherChainBuilder.build(regexExp);
        String matcherString = MatcherChainPrinter.printChain(chainMatcher);
        System.out.printf("<========================\n%s\n========================>\n", matcherString);
        assertEquals("[Union:[Pre:null,Chains:[" +
                "[String:a] -> [Meta:\\w] / [String:b]" +
                " -> [LM:[Type:(?=),Chains:{[String:c] -> [GS:1] -> [String:1] -> [GE:1]}]] / [Meta:\\d] -> [String:d]" +
                "]]]", matcherString);
    }

    @DisplayName("测试环视表达式构建成环视匹配器的内部是否独立，即没有引用外部的RM")
    @Test
    public void test_build_lookaroundMatcher_innerIndependent(){
        String regexString = "(a(?=b+)c)+";
        RegexExp regexExp = this.regexParser.parse(regexString);
        ChainMatcher chainMatcher = TestedMatcherChainBuilder.build(regexExp);
        String matcherString = MatcherChainPrinter.printChain(chainMatcher);
        System.out.printf("<========================\n%s\n========================>\n", matcherString);
        assertEquals("[RS:[Name:1-(a(?=b+)c)+,Pre:null]] -> [GS:1] -> [String:a]" +
                " -> [LM:[Type:(?=),Chains:{[RS:[Name:2-b+,Pre:null]] -> [String:b] -> [RE:2]}]]" +
                " -> [String:c] -> [GE:1] -> [RE:1]", matcherString);
    }

    private static Stream<Arguments> LeftInnerInfiniteLengthProvider(){
        return Stream.of(
                Arguments.of("(b+|d)(?<!\\1{2})y",
                        "左向断言内部表达式最大长度不能是无穷大"),
                Arguments.of("((a+|bb)c)(?<!\\1\\d)x",
                        "左向断言内部表达式最大长度不能是无穷大")
        );
    }

    @DisplayName("测试左向断言内部无穷大长度报错，无法构建")
    @ParameterizedTest
    @MethodSource("LeftInnerInfiniteLengthProvider")
    public void test_leftInnerInfiniteLength(String regex,String errorMsg){
        assertThrows(RuntimeException.class, () -> new TestedRegexMatcher(regex),errorMsg);
    }
}

class TestLookaroundMatcher{

    private static Stream<Arguments> rightPositiveProvider() {
        return Stream.of(
                // 1. 匹配后面跟着"元"的数字，捕获数字，量词+
                // "价格100元" 中匹配 "100"，"价格100美元" 中不匹配
                Arguments.of("(\\d+)(?=元)", "价格100元，重10斤", true),
                // 2. 选择表达式+量词：匹配后跟 .com 或 .cn 的域名部分
                Arguments.of("(www\\.(google|baidu))(?=\\.com|\\.cn)",
                        "访问 www.google.com 或 www.baidu.cn", true),
                // 3. 密码规则：至少包含一个数字，匹配整个单词
                Arguments.of("(?=\\w*\\d)\\w{8,}",
                        "abc12345 ok", true),
                // 4. 捕获单位前的金额，量词*与选择
                Arguments.of("(\\d+(\\.\\d+)?)(?=(元|人民币))",
                        "这件衣服300.5元", true),
                // 5. 负例：断言不满足
                // 要求"foo"后必须跟"bar"，但实际跟的是"baz"
                Arguments.of("(foo)(?=bar)", "hello foobaz world", false)
        );
    }

    @DisplayName("测试右向肯定断言")
    @ParameterizedTest
    @MethodSource("rightPositiveProvider")
    public void test_lookaround_rightPositive(String regex,String str,boolean isMatch){
        RegexMatcher regexMatcher = new TestedRegexMatcher(regex);
        assertEquals(isMatch,regexMatcher.match(str));
    }

    private static Stream<Arguments> rightNegativeProvider() {
        return Stream.of(
                // 1. 匹配后面不跟"元"的数字，捕获数字
                // "100元"排除，"100斤"匹配
                Arguments.of("(\\d+)(?!元)", "大米100斤，白菜100元", true),
                // 2. 文件名不以.tmp结尾的文件，捕获主文件名
                Arguments.of("(\\w+\\.)(?!tmp)\\w+",
                        "读取 report.docx 和 cache.tmp", true),
                // 3. 选择+量词：匹配后面不是.com/.cn的域名部分
                Arguments.of("(www\\.[a-z]+)(?!\\.com|\\.cn)",
                        "访问 www.test.org 和 www.baidu.com", true),
                // 4. 密码规则：捕获整个单词，断言后面不能是特殊字符结尾的干扰
                // 匹配后面不是数字的8位以上单词
                Arguments.of("(\\w{8,})(?!\\d)",
                        "合法密码abc12345出现", true),
                // 5. 负例：所有位置都被否定断言拦截
                // 要求"foo"后面不能跟"bar"，但文本中foo后面跟的就是bar
                Arguments.of("(foo)(?!bar)", "hello foobar world", false)
        );
    }

    @DisplayName("测试右向否定断言")
    @ParameterizedTest
    @MethodSource("rightNegativeProvider")
    public void test_lookaround_rightNegative(String regex,String str,boolean isMatch){
        RegexMatcher regexMatcher = new TestedRegexMatcher(regex);
        assertEquals(isMatch,regexMatcher.match(str));
    }

    private static Stream<Arguments> leftPositiveProvider() {
        return Stream.of(
                // 1. 基本用法：匹配"¥"后面的金额数字，捕获组捕获金额
                //    lookbehind 内的消费不占主匹配位置
                Arguments.of("(?<=¥)\\d+", "商品价格¥1990元整", true),

                // 2. 可变长度后行断言：后面跟任意长度的前缀必须以"测试"开头
                //    （部分引擎不支持可变长度lookbehind，此用例检验引擎能力）
                Arguments.of("(?<=测试：)\\w+",
                        "日志：测试：success，结束", true),

                // 3. lookbehind 内含捕获组，主模式用反向引用引用它：
                //    匹配"基准数字+空白+与基准相同的数字"，验证lookbehind的捕获组对外可见
                Arguments.of("(?<=(\\d) )\\1{2}",
                        "校验 3 333 通过", true),

                // 4. 边界负例：金额前是"$"不是"¥"，lookbehind失败
                //    且文本中间的"1990"前无任何符号，也不应误匹配
                Arguments.of("(?<=¥)\\d+", "price $1990 total 1990", false),

                // 5. 回溯边界负例：lookbehind匹配到了位置，但主模式消费失败且无法回溯补救
                //    要求"ab"后面是数字并消费3位数字，但数字只有2位
                Arguments.of("(?<=ab)\\d{3}", "xxab12yy", false)
        );
    }

    @DisplayName("测试左向肯定断言")
    @ParameterizedTest
    @MethodSource("leftPositiveProvider")
    public void test_lookaround_leftPositive(String regex,String str,boolean isMatch){
        RegexMatcher regexMatcher = new TestedRegexMatcher(regex);
        assertEquals(isMatch,regexMatcher.match(str));
    }

    private static Stream<Arguments> leftNegativeProvider() {
        return Stream.of(
                // 1. 基本用法：匹配前面不是"-"的数字（排除负数，只取正数），捕获数字
                //    "-50"被排除，"80"匹配
                Arguments.of("(?<!-)(\\d+)", "温度从-50升到80度", true),

                // 2. 可变长度否定后行断言：前面不是"用户名："前缀的字母串
                //    （检验引擎对可变长度lookbehind的支持）
                Arguments.of("(?<!用户名：)[a-z]{4}",
                        "字段：用户名：admin，角色：guest", true),

                // 3. 边界情况：文本开头位置，前面没有字符，否定断言天然成立
                //    首字母"p"前面为空，(?<!x)应视为成功
                Arguments.of("(?<!@)\\w+", "plain@name", true),

                // 4. 负例：每个字母前面都是小写字母或@，均被否定断言拦截
                //    "@name"中n前是@，"admin"中每个位置前都有字母
                Arguments.of("(?<![@a-z])[a-z]+", "邮箱是@name或admin", true),

                // 5. 回溯边界负例：否定断言通过的位置上，主模式消费失败
                //    "ab"前不是"-"满足断言，但后面没有3位数字可供消费
                Arguments.of("(?<!-)ab\\d{3}", "xx-ab12yy", false)
        );
    }

    @DisplayName("测试左向否定断言")
    @ParameterizedTest
    @MethodSource("leftNegativeProvider")
    public void test_lookaround_leftNegative(String regex,String str,boolean isMatch){
        RegexMatcher regexMatcher = new TestedRegexMatcher(regex);
        assertEquals(isMatch,regexMatcher.match(str));
    }

    private static Stream<Arguments> backerAndGroupProvider() {
        return Stream.of(
                Arguments.of("(?=(c+|xy)(d))\\1d",
                        "xxcddyy",
                        Arrays.asList(
                                Arrays.asList("cd","c","d")
                        )),
                Arguments.of("(?=(\\d{1,2}|[a-f]+)x)\\1x",
                        "a22xa6x",
                        Arrays.asList(
                                Arrays.asList("22x", "22"),
                                Arrays.asList("6x", "6")
                        )),
                Arguments.of("(a+|b)(?!\\1[a-z])b",
                        "aabz",
                        Arrays.asList(
                                Arrays.asList("aab", "aa")
                        )),
                Arguments.of("((a+|b)c)(?!\\1\\d)x",
                        "acx",
                        Arrays.asList(
                                Arrays.asList("acx", "ac", "a")
                        )),

                Arguments.of("(?<=(\\d{2,3}|ab)cd)\\1e",
                        "xx12cd12eyy34cd34e",
                        Arrays.asList(
                                Arrays.asList("12e", "12"),
                                Arrays.asList("34e", "34")
                        ))
        );
    }


    @DisplayName("测试环视包含回溯器和捕获组")
    @ParameterizedTest
    @MethodSource("backerAndGroupProvider")
    public void test_include_backerAndGroup(String regex, String str,List<List<String>> groupStrsList){
        RegexMatcher regexMatcher = new TestedRegexMatcher(regex,true);
        List<List<String>> result = new ArrayList<>();
        SearchResult searchResult = regexMatcher.search(str);
        if(searchResult != null){
            do result.add(searchResult.getGroupStrList());
                // 进行后续查找
            while(searchResult.next());
        }
        assertEquals(groupStrsList,result);
    }
}

class TestBoundaryMatcher{
    @DisplayName("\\b/\\B边界匹配器的构建")
    @Test
    public void test_build_boundaryMatcher(){
        String regex = "\\ba\\Bc\\b";
        RegexExp regexExp = new TestedRegexParser(regex).parse();
        ChainMatcher chainMatcher = TestedMatcherChainBuilder.build(regexExp);
        String matcherStr = MatcherChainPrinter.printChain(chainMatcher);
        assertEquals("[Boundary:\\b] -> [String:a] -> [Boundary:\\B] -> [String:c] -> [Boundary:\\b]",matcherStr);
    }

    @DisplayName("\\b/\\B边界匹配器匹配逻辑测试")
    @ParameterizedTest
    @CsvSource(delimiterString = "=",value = {
            "\\btest\\b=a test case=true",
            "\\btest\\b=testing=false",
            "\\btest\\b=我爱test你=true",
            "\\Btest\\B=xtesty=true",
            "\\Btest\\B=test=false"
    })

    public void test_boundaryMatcher_match(String regex,String str,boolean isMatch){
        TestedRegexMatcher regexMatcher = new TestedRegexMatcher(regex);
        assertEquals(isMatch,regexMatcher.match(str));
    }
}
