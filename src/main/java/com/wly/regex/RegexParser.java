package com.wly.regex;

import com.wly.regex.exp.*;

import java.util.ArrayList;
import java.util.List;

/**
 * <RegexExp> ::= <UnionExp>
 *
 * <UnionExp> ::= <ConcatExp> | <UnionExp>
 * <UnionExp> ::= <ConcatExp>
 *
 * FOLLOW(<ConcatExp>) = {end，|,)}
 *
 * <ConcatExp> ::= <RepeatExp><ConcatExp>
 * <ConcatExp> ::= <RepeatExp>
 *
 * <RepeatExp> ::= <CharCollectionExp><RepeatModifiers>
 * <RepeatModifiers> ::= ? | + | * | {m} | {m,} | {m,n} | 空
 *
 * <CharCollectionExp> ::= [<CharSequenceExp>]
 * <CharCollectionExp> ::= [^<CharSequenceExp>]
 * <CharCollectionExp> ::= <CharGroupExp>
 *
 * <CharGroupExp> ::= (<UnionExp>)
 * <CharGroupExp> ::= <CharExp>
 *
 * <CharSequenceExp> ::= <CharRangeExp><CharSequenceExp>
 * <CharSequenceExp> ::= <CharRangeExp>
 *
 * <CharRangeExp> ::= <CharExp> - <CharExp> //这里只支持26个英文字母大小写
 * <CharRangeExp> ::= <CharExp> -
 * <CharRangeExp> ::= <CharExp>
 *
 * <CharExp> ::= <BasicChar>
 * <CharExp> ::= <MetaChar>
 *
 * <BasicChar> ::= 0x0000-0xFFFF // 内部使用Char类型，表示范围是0x00-0xFFFF，也能够表示绝大多数中文
 * <MetaChar> ::= \d | \w | \s  | \\ // 以及他们的大小写，除了富含语义的单个字符外还包括转义字符
 */

/**
 * 正则表达式解析器
 */
public class RegexParser {

    // 要解析的正则表达式字符串
    protected String regexString;

    // 记录当前指向的字符位置
    protected int pointer;

    /**
     * 是否解析结束
     * @return
     */
    protected boolean isEnd(){
        return this.pointer >= this.regexString.length();
    }

    /**
     * 匹配消耗当前字符并指向下一个字符
     * @param target
     * @return
     */
    protected boolean matchChar(char target){
        if(this.isEnd() | this.regexString.charAt(pointer) != target) return false;
        this.pointer++;
        return true;
    }

    /**
     * 获取当前字符，并指向下一个字符
     * @return
     */
    protected char next(){
        if (this.isEnd()) throw new IndexOutOfBoundsException("正则表达式解析结束，无法获取当前字符");
        return this.regexString.charAt(this.pointer++);
    }

    /**
     * 判断给定的target字符串中是否有当前的指针指向的字符
     * @param target
     * @return
     */
    protected boolean includeChar(String target){
        return !this.isEnd() && target.indexOf(this.regexString.charAt(this.pointer)) > -1;
    }

    /**
     * 解析从当前指针开始的第一个数字
     */
    protected int parseDigit(){
        int initialPos = this.pointer;
        char next = this.next();
        while(next >= 48 && next <= 57){next = this.next();}
        // 如果不是数字那么返回0
        if (this.pointer - initialPos <= 1) throw new RuntimeException(String.format("未能解析出数字，位置%d,符号%s",this.pointer, next));
        return Integer.parseInt(this.regexString.substring(initialPos, this.pointer));
    }

    /**
     * 解析字符串化的正则表达式
     * @return AST的正则表达式，对应RegexExp类
     */
    public RegexExp parse() {
        return this.parseUnionExp();
    }


    protected RegexExp parseUnionExp() {
        RegexExp left = this.parseConcatExp();
        if (this.matchChar('|')) {
            RegexExp right = this.parseUnionExp();
            return UnionExp.builder().left(left).right(right).build();
        }
        return left;
    }

    protected RegexExp parseConcatExp() {
        RegexExp left = this.parseRepeatExp();
        if (!this.isEnd() && !this.includeChar("|)")) {
            RegexExp right = this.parseConcatExp();
            return ConcatExp.builder().left(left).right(right).build();
        }
        return left;
    }

    protected RegexExp parseRepeatExp() {
        RegexExp charCollectionExp = this.parseCharCollectionExp();
        // 开始解析修饰符
        int min, max;
        if (!this.isEnd()) {
            if (this.matchChar('?')) {
                min = 0;
                max = 1;
            } else if (this.matchChar('*')) {
                min = max = 0;
            } else if (this.matchChar('+')) {
                min = 1;
                max = 0;
            } else if (this.matchChar('{')) {
                // 一定有一个最小值数字
                min = this.parseDigit();
                // 如果是}就固定数量，提前结束
                if(this.matchChar('}')) return RepeatExp.builder().charCollectionExp(charCollectionExp).min(min).max(min).build();
                // 如果不是}，那么一定有一个逗号
                if(!this.matchChar(',')) throw new RuntimeException(String.format("预期是,，实际位置%d,字符%s",this.pointer, this.next()));
                // 继续解析最大值
                max = this.parseDigit();
                if(!this.matchChar('}')) throw new RuntimeException(String.format("预期是}，实际位置%d,字符%s",this.pointer, this.next()));
                return RepeatExp.builder().charCollectionExp(charCollectionExp).min(min).max(max).build();
            }
        }
        min = max = 1;
        return RepeatExp.builder().charCollectionExp(charCollectionExp).min(min).max(max).build();
    }

    protected RegexExp parseCharCollectionExp() {
        if(this.matchChar('[')){
            boolean isNegative = this.matchChar('^');
            List<RegexExp> charSequenceExp = this.parseCharSequenceExp();
            if(!this.matchChar(']')) throw new RuntimeException(String.format("预期是]，实际位置%d，字符%s", this.pointer, this.next()));
            return CharCollectionExp.builder().charSequenceExp(charSequenceExp).negative(isNegative).build();
        }
        return this.parseCharGroupExp();
    }

    protected RegexExp parseCharGroupExp() {
        if(this.matchChar('(')){
            RegexExp unionExp = this.parseUnionExp();
            if(!this.matchChar(')')) throw new RuntimeException(String.format("预期是)，实际位置%d，字符%s", this.pointer, this.next()));
            return unionExp;
        }
        return this.parseCharExp();
    }

    protected List<RegexExp> parseCharSequenceExp() {
        List<RegexExp> list = new ArrayList<>();
        while(!this.includeChar("]")){
            List<RegexExp> charRangeExp = this.parseCharRangeExp();
            list.addAll(charRangeExp);
        }
        return list;
    }

    protected List<RegexExp> parseCharRangeExp() {
        CharExp left = this.parseCharExp();
        List<RegexExp> list = new ArrayList<>();
        if(this.matchChar('-')){
            if(this.isEnd()) throw new RuntimeException("解析结束，错误：字符范围结束符-后面没有字符！");
            // a- 这种情况，连续两个CharExp
            if(this.includeChar("]")){
                CharExp charExp = CharExp.builder().charValue('-').build();
                list.add(left);
                list.add(charExp);
                return list;
            }
            // CharRangeExp
            else {
                CharExp right = this.parseCharExp();
                CharRangeExp charRangeExp =  CharRangeExp.builder().left(left).right(right).build();
                list.add(charRangeExp);
                return list;
            }
        }
        list.add(left);
        return list;
    }

    protected CharExp parseCharExp() {
        // 如果是转义元字符
        if(this.matchChar('\\')){
            if(this.matchChar('d')) return CharExp.builder().charValue('d').build();
            if(this.matchChar('D')) return CharExp.builder().charValue('D').build();
            if(this.matchChar('w')) return CharExp.builder().charValue('w').build();
            if(this.matchChar('W')) return CharExp.builder().charValue('W').build();
            if(this.matchChar('s')) return CharExp.builder().charValue('s').build();
            if(this.matchChar('S')) return CharExp.builder().charValue('S').build();
        }
        return CharExp.builder().charValue(this.next()).build();
    }

}
