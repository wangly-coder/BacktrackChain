package com.tobethebest.regex.ast;

import com.tobethebest.regex.ast.exp.*;

import java.util.*;

/**
 * 正则表达式解析器
 * 有状态的递归下降解析器
 */
public class RegexParser {

    // 几乎所有的正则转义字符
    public static final String REGEX_ESCAPE_CHAR_SEQUENCE = "^$|*+?{}()[]";

    // []中的转义字符，其他的字符都当作字面量使用
    public static final String COLLECTION_ESCAPE_CHAR_SEQUENCE = "^-[]";

    // 要解析的正则表达式字符串
    protected String regexString;

    // 记录当前指向的字符位置
    protected int cursor;

    // 是否进入了[]解析环境
    protected boolean isInCollection;

    // 是否关闭RepeatExp的嵌套检查
    protected boolean closeNestCheck;

    // 对应^$限定符
    public boolean hasStartLimit;
    public boolean hasEndLimit;

    // 捕获组与引用相关上下文
    public int groupNumber; // 组数量
    public LinkedHashMap<String,Integer> groupNameToIdMap;

    public RegexParser(){}

    public RegexParser(String regexString){
        this.regexString = regexString;
    }

    public RegexParser(String regexString,boolean closeNestCheck){
        this.regexString = regexString;
        this.closeNestCheck = closeNestCheck;
    }

    /**
     * 是否解析结束
     */
    protected boolean isEnd(){
        return this.cursor >= this.regexString.length();
    }

    /**
     * 匹配消耗当前字符并指向下一个字符
     */
    protected boolean matchChar(char target){
        if(this.isEnd() || this.regexString.charAt(cursor) != target) return false;
        this.cursor++;
        return true;
    }

    /**
     * 获取当前字符，并指向下一个字符
     */
    protected char next(){
        if (this.isEnd()) throw new IndexOutOfBoundsException("正则表达式解析结束，无法获取当前字符");
        return this.regexString.charAt(this.cursor++);
    }

    /**
     * 判断给定的target字符串中是否有当前的指针指向的字符
     */
    protected boolean includeChar(String target){
        return !this.isEnd() && target.indexOf(this.regexString.charAt(this.cursor)) > -1;
    }

    // 关闭量词嵌套检查
    public void closeNestCheck(){
        this.closeNestCheck = true;
    }

    /**
     * 解析从当前指针开始的第一个数字
     */
    protected int parseDigit(){
        int initialPos = this.cursor,next;
        while(!this.isEnd() && (next = this.regexString.charAt(this.cursor)) >= 48 && next <= 57){
            this.cursor++;
        }
        return Integer.parseInt(this.regexString.substring(initialPos, this.cursor));
    }

    /**
     * 解析捕获组的名字
     */
    protected String parseGroupName(){
        int curPos = this.cursor;
        while(!this.isEnd() && this.regexString.charAt(this.cursor) != '>'){
            this.cursor++;
        }
        if(curPos == this.cursor) throw new RuntimeException(String.format("捕获组名称不能为空，索引位置为：%d",curPos));
        return this.regexString.substring(curPos, this.cursor);
    }

    /**
     * 重置解析器
     */
    public void reset(){
        this.cursor = 0;
        this.regexString = null;
        this.isInCollection = false;
        this.closeNestCheck = false;
        this.hasStartLimit = false;
        this.hasEndLimit = false;
        this.groupNumber = 0;
        this.groupNameToIdMap = null;
    }

    protected void throwUnMatchExpectedCharException(String headInfo,char expectedChar){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(headInfo).append("预期是").append(expectedChar).append(",");
        String actualInfo = this.isEnd() ? "实际解析早已结束" : String.format("实际位置%d，字符%c",this.cursor,this.next());
        throw new RuntimeException(stringBuilder.append(actualInfo).toString());
    }

    /**
     * 解析字符串化的正则表达式，统一解析方法入口
     * @return AST的正则表达式，对应RegexExp类
     */
    public RegexExp parse() {
        RegexExp regexExp = this.parseUnionExp();
        // 进行校验
        if(!closeNestCheck) NestNumberChecker.check(regexExp);
        else closeNestCheck = false;
        // 进行长度校验
        if(this.cursor != this.regexString.length()) throw new RuntimeException(
                String.format("正则表达式解析错误：未完成字符全部解析而提前结束。最后的位置：%d",this.cursor));
        return regexExp;
    }

    public RegexExp parse(String regexString) {
        this.reset();
        this.regexString = regexString;
        return this.parse();
    }

    public RegexExp parse(String regexString,boolean closeNestCheck) {
        this.reset();
        this.closeNestCheck = closeNestCheck;
        this.regexString = regexString;
        return this.parse();
    }

    protected RegexExp parseUnionExp() {
        RegexExp left,right;
        // 允许空字符串的写法
        if(this.matchChar('|')) {
            left = MetaExp.of("");
            // 只有|这种写法，不被允许，任何|至少一侧有一个非空表达式
            if(this.includeChar("|") || this.isEnd()) throw new RuntimeException("选择表达式中至少一侧有一个非空表达式");
            right = this.parseUnionExp();
            if(right instanceof UnionExp){
                UnionExp unionRightExp = (UnionExp) right;
                if(unionRightExp.hasEmptyString) throw new RuntimeException("选择表达式中最多只允许一个空串匹配");
            }
            UnionExp result = UnionExp.of(left,right);
            result.hasEmptyString = true;
            return result;
        }
        left = this.parseConcatExp();
        if(this.matchChar('|')) {
            // 右侧为空的情况
            boolean isRightEmptyString = false;
            if(this.isEnd() || this.includeChar(")")) {
                right = MetaExp.of("");
                isRightEmptyString = true;
            }
            else right = this.parseUnionExp();
            UnionExp result = UnionExp.of(left,right);
            result.hasEmptyString = isRightEmptyString;
            return result;
        }
        return left;
    }

    protected RegexExp parseConcatExp() {
        RegexExp left = this.parseRepeatExp();
        if (!this.isEnd() && !this.includeChar("|)")) {
            RegexExp right = this.parseConcatExp();
            return ConcatExp.of(left,right);
        }
        return left;
    }

    protected RegexExp parseRepeatExp() {
        RegexExp charCollectionExp = this.parseCharCollectionExp();
        // 开始解析修饰符
        int min, max;
        RepeatExp repeatExp = null;
        if (!this.isEnd()) {
            if (this.matchChar('?')) repeatExp = RepeatExp.of(charCollectionExp,0,1);
            else if (this.matchChar('*')) repeatExp = RepeatExp.of(charCollectionExp,0,-1);
            else if (this.matchChar('+')) repeatExp = RepeatExp.of(charCollectionExp,1,-1);
            else if (this.matchChar('{')) {
                // 一定有一个最小值数字
                min = this.parseDigit();
                // 如果是}就固定数量，提前结束
                if(this.matchChar('}'))
                {
                    if(min == 0) throw new RuntimeException("无效值，单独一个量词不能为0");
                    repeatExp = RepeatExp.of(charCollectionExp,min,min);
                }
                else
                {
                    // 如果不是}，那么一定有一个逗号
                    if(!this.matchChar(',')) this.throwUnMatchExpectedCharException("",',');
                    // 如果是}那么则有最小值，没有最大值
                    if(this.matchChar('}')) repeatExp = RepeatExp.of(charCollectionExp,min,-1);
                    else
                    {
                        // 继续解析最大值
                        max = this.parseDigit();
                        // 判断值大小是否合理
                        if(min == max && min == 0) throw new RuntimeException("min和max不能同时为0");
                        else if(max < min) throw new RuntimeException(String.format("预期max值大于等于min值，实际max:%d,min:%d",max,min));
                        if(!this.matchChar('}')) this.throwUnMatchExpectedCharException("",'}');
                        repeatExp = RepeatExp.of(charCollectionExp,min,max);
                    }
                }
            }
        }
        if(repeatExp == null) return charCollectionExp;
        // 如果repeatExp不为空，那么就判断是否是非贪婪匹配
        if(this.matchChar('?')) repeatExp.greedy = false;
        return repeatExp.process();
    }

    protected RegexExp parseCharCollectionExp() {
        if(this.matchChar('[')){
            this.isInCollection = true;
            // 集合表达式中必须有字符
            if(this.matchChar(']')) throw new RuntimeException("提前结束的集合表达式[]，内容不能为空！");
            boolean isNegative = this.matchChar('^');
            List<RegexExp> charSequenceExp = this.parseCharSequenceExp();
            if(!this.matchChar(']')) this.throwUnMatchExpectedCharException("",']');
            this.isInCollection = false;
            return CharCollectionExp.of(charSequenceExp,isNegative);
        }
        return this.parseCharGroupExp();
    }

    protected List<RegexExp> parseCharSequenceExp() {
        List<RegexExp> list = new ArrayList<>();
        while(!this.includeChar("]")){
            List<RegexExp> charRangeExp = this.parseCharRangeExp();
            list.addAll(charRangeExp);
        }
        return list;
    }

    protected RegexExp parseCharGroupExp() {
        if(this.matchChar('(')){
            if(this.matchChar(')')) throw new RuntimeException("()内不能为空");
            RegexExp groupExp;
            if(this.matchChar('?')){
                // 非捕获组
                if(this.matchChar(':')) groupExp = this.parseUnionExp();
                // 具名捕获组
                else if(this.matchChar('<')) {
                    // 可能是后行断言
                    groupExp = this.parseLookaroundExp(false);
                    if(groupExp == null){
                        // 解析捕获组的名字
                        String groupName = this.parseGroupName();
                        // 不能以数字开头，只能是大小写字母
                        char firstChar = groupName.charAt(0);
                        if((firstChar < 'A' || firstChar > 'z') || (firstChar > 'Z' && firstChar < 'a'))
                            throw new RuntimeException(String.format("捕获组名称%s不能以数字开头，开头只能是字母大小写",groupName));
                        if(this.groupNameToIdMap == null) groupNameToIdMap = new LinkedHashMap<>();
                        else if(this.groupNameToIdMap.get(groupName) != null) throw new RuntimeException("重复的捕获组名称"+groupName);
                        if(!this.matchChar('>')) this.throwUnMatchExpectedCharException("具名捕获组。",'>');
                        int groupId = ++this.groupNumber;
                        // 加入组名->id的映射表中
                        this.groupNameToIdMap.put(groupName,groupId);
                        groupExp = GroupExp.of(groupId,this.parseUnionExp());
                    }
                }
                // 可能是环视先行断言
                else{
                    groupExp = this.parseLookaroundExp(true);
                    if(groupExp == null) throw new RuntimeException(String.format("预期是非捕获组、具名捕获组或者环视表达式，实际语句无法解析。位置%d，字符%s", this.cursor, this.next()));
                }
            }
            // 普通捕获组
            else{
                int groupId = ++this.groupNumber;
                groupExp = this.parseUnionExp();
                groupExp = GroupExp.of(groupId,groupExp);
            }
            if(!this.matchChar(')')) this.throwUnMatchExpectedCharException("",')');
            return groupExp;
        }
        return this.parseCharExp();
    }

    protected RegexExp parseLookaroundExp(boolean isRight){
        LookaroundExp innerExp = null;
        // 右边肯定
        if(this.matchChar('=')) innerExp = LookaroundExp.of(LookaroundExp.LookaroundType.identity(isRight,true),this.parseUnionExp());
        // 右边否定
        else if(this.matchChar('!')) innerExp = LookaroundExp.of(LookaroundExp.LookaroundType.identity(isRight,false),this.parseUnionExp());
        return innerExp;
    }

    protected List<RegexExp> parseCharRangeExp() {
        RegexExp left = this.parseCharExp();
        // 做类型校验
        List<RegexExp> list = new ArrayList<>();
        if(this.matchChar('-')){
            if(this.isEnd()) throw new RuntimeException("解析结束，错误：字符范围结束符-后面没有字符！");
            // a-] 这种情况，连续两个CharExp
            if(this.includeChar("]")){
                CharExp charExp = CharExp.of('-');
                list.add(left);
                list.add(charExp);
            }
            // CharRangeExp
            else {
                RegexExp right = this.parseCharExp();
                // 做类型检查
                if(!(left instanceof CharExp && right instanceof CharExp)) throw new RuntimeException(
                        String.format("预期为CharExp类型，实际left:%s，right:%s", left.getClass().getSimpleName(),right.getClass().getSimpleName()));
                CharExp leftCharExp = (CharExp) left;
                CharExp rightCharExp = (CharExp) right;
                // 做类别检查,必须同是字母或者数字
                char leftValue = leftCharExp.charValue;
                char rightValue = rightCharExp.charValue;
                if(!((leftValue >= '0' && leftValue <= '9' && rightValue >= '0' && rightValue <= '9')
                        || (leftValue >= 'A' && leftValue <= 'z' && rightValue  >= 'A' && rightValue <= 'z')))
                    throw new RuntimeException(String.format("预期为数字或者字母，实际left:%s,right:%s", leftValue,rightValue));
                // 做范围检查
                if(leftValue >= rightValue) throw new RuntimeException(
                        String.format("预期右字符码值大于左边，实际left:%s,right:%s", leftValue,rightValue));
                CharRangeExp charRangeExp =  CharRangeExp.of(leftCharExp.charValue,rightCharExp.charValue);
                list.add(charRangeExp);
            }
            return list;
        }
        list.add(left);
        return list;
    }

    protected RegexExp parseCharExp() {
        // 针对^$的特殊处理
        if(this.cursor == 0 && this.matchChar('^')) {
            this.hasStartLimit = true;
            return MetaExp.of("^");
        }
        if(this.cursor == this.regexString.length()-1 && this.matchChar('$')) {
            this.hasEndLimit = true;
            return MetaExp.of("$");
        }
        // 不在[]解析中，点被看作元字符，否则就是普通字符
        if(this.matchChar('.')) return this.isInCollection ? CharExp.of('.') : MetaExp.of(".");
        // 如果是转义或者元字符
        else if(this.matchChar('\\')){
            // 如果是元字符
            if(this.matchChar('d')) return MetaExp.of("\\d");
            if(this.matchChar('D')) return MetaExp.of("\\D");
            if(this.matchChar('w')) return MetaExp.of("\\w");
            if(this.matchChar('W')) return MetaExp.of("\\W");
            if(this.matchChar('s')) return MetaExp.of("\\s");
            if(this.matchChar('S')) return MetaExp.of("\\S");
            if(this.matchChar('b')) return MetaExp.of("\\b");
            if(this.matchChar('B')) return MetaExp.of("\\B");
            // 如果是捕获组引用
            if(this.includeChar("123456789")){
                int refId = this.parseDigit();
                if(refId > this.groupNumber) throw new RuntimeException(String.format("引用编号为%d的捕获组不存在",refId));
                return GroupRefExp.of(refId);
            }
            // 具名捕获组
            if(this.matchChar('k')){
                // 兼容\n（n是普通字符）的写法
                if(!this.matchChar('<')) return CharExp.of('k');
                String refGroupName = this.parseGroupName();
                if(!this.matchChar('>')) this.throwUnMatchExpectedCharException("具名捕获组引用。",'>');
                Integer refId = this.groupNameToIdMap.get(refGroupName);
                if(refId == null) throw new RuntimeException(String.format("具名捕获组引用，引用的组名%s不存在",refGroupName));
                return GroupRefExp.of(refId);
            }
        }
        // 转义字符不能单独出现，在特定的环境下也有不能出现的，避免语义混乱
        else {
            if(this.isInCollection && this.includeChar(COLLECTION_ESCAPE_CHAR_SEQUENCE)) throw new RuntimeException(String.format("[]中出现不合法的特殊字符%s，必须转义",this.next()));
            else if(!this.isInCollection && this.includeChar(REGEX_ESCAPE_CHAR_SEQUENCE)) throw new RuntimeException(String.format("出现不合法的特殊字符%s，必须转义",this.next()));
        }
        // 剩下的是转义字符或者合法的普通字符，统一交给最后处理
        return CharExp.of(this.next());
    }

}
