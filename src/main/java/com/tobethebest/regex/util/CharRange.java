package com.tobethebest.regex.util;

import com.tobethebest.regex.ast.exp.CharExp;
import com.tobethebest.regex.ast.exp.CharRangeExp;
import com.tobethebest.regex.ast.exp.MetaExp;
import com.tobethebest.regex.ast.exp.RegexExp;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CharRange {
    public char left;
    public char right;

    public CharRange(char left, char right) {
        if(left > right) throw new RuntimeException("CharRange构造时right字符必须大于等于left字符");
        this.left = left;
        this.right = right;
    }

    public static CharRange of(char left,char right){
        return new CharRange(left,right);
    }

    public static CharRange of(char c){
        return new CharRange(c,c);
    }

    @Override
    public String toString() {
        if(left == right) return String.format("[%s]",left);
        return String.format("[%s,%s]",left,right);
    }

    public boolean include(char c){
        return c >= left && c <= right;
    }

    public static boolean rangeListInclude(List<CharRange> ranges,char c){
        return ranges.stream().anyMatch(range -> range.include(c));
    }

    /**
     * 将exp集合转化为对应的区间集合
     * @param regexExps 要转换的表达式集合
     * @return 转换后的区间
     */
    public static List<CharRange> regexExpsToCharRanges(List<RegexExp> regexExps){
        if(regexExps == null) return null;
        List<CharRange> charRanges = new ArrayList<>();
        regexExps.forEach(regexExp -> {
            if(regexExp instanceof CharExp) {
                CharExp charExp = (CharExp) regexExp;
                CharRange charRange = CharRange.of(charExp.charValue);
                charRanges.add(charRange);
            } else if (regexExp instanceof CharRangeExp) {
                CharRangeExp charRangeExp = (CharRangeExp) regexExp;
                charRanges.add(CharRange.of(charRangeExp.left,charRangeExp.right));
            } else if (regexExp instanceof MetaExp) {
                MetaExp metaExp = (MetaExp) regexExp;
                charRanges.addAll(MetaUtil.MetaToCharRangeMap.get(metaExp.metaValue));
            } else{
                throw new RuntimeException("无法转换的边类型");
            }
        });
        return charRanges;
    }

    /**
     * 将区间转化为RegexExp
     * @param charRanges 被转化的区间集合
     * @return 转换后的表达式集合
     */
    public static List<RegexExp> charRangesToRegexExps(List<CharRange> charRanges){
        if(charRanges == null || charRanges.isEmpty()) return null;
        List<RegexExp> result = new ArrayList<>();
        // 区间转化的正则表达式也就CharRangeExp和CharExp两种可能
        charRanges.forEach(charRange -> {
            if(charRange.left == charRange.right) result.add(CharExp.of(charRange.left));
            else result.add(CharRangeExp.of(charRange.left,charRange.right));
        });
        return result;
    }

    public static char forward(char c){
        return (char)(c+1);
    }

    public static char back(char c){
        return (char)(c-1);
    }

    /**
     * 判断两个区间是否可以合并
     */
    public static boolean canMerge(CharRange range1,CharRange range2){
        /*
         * 1.只要任意一个区间的左值落在另一个区间内就能合并
         * 2.由于字符是离散整数分布的，所以非相交区间但是相差距离为1也可以进行合并
         * 这里默认range1.left < range2.left，能够达到一种优化，往往只需要先判断左边就能够提前结束
         */
        char left1 = range1.left,right1 = range1.right;
        char left2 = range2.left,right2 = range2.right;
        return (right1 >= left2 && right1 <= right2) || (left2 - right1 == 1)
                || (right2 >= left1 && right2 <= right1) || (left1 - right2 == 1);
    }

    /**
     * 合并两个区间，前提是区间可合并，如果不能则返回NULL
     */
    public static CharRange doMerge(CharRange range1,CharRange range2){
        if(!CharRange.canMerge(range1,range2)) return null;
        // 合并区间就是取两个区间左边最小值和右边最大值组成新的区间
        char minLeft = (char) Math.min(range1.left,range2.left);
        char maxRight = (char) Math.max(range1.right,range2.right);
        return CharRange.of(minLeft,maxRight);
    }

    /**
     * 多区间合并
     * @param charRanges 多个区间List集合
     * @return 合并好的，按区间左值大小升序排序的区间集合
     */
    public static List<CharRange> mergeMulti(List<CharRange> charRanges){
        if(charRanges == null || charRanges.isEmpty() ) return null;
        if(charRanges.size() == 1) return charRanges;
        // 对区间进行升序排序，依据区间左值排序
        charRanges.sort(Comparator.comparingInt(o -> o.left));
        List<CharRange> result = new ArrayList<>();
        CharRange previous = charRanges.get(0);
        // 遍历区间并尝试进行合并
        for(int i=1;i< charRanges.size();i++){
            CharRange current = charRanges.get(i);
            // 如果不能合并则将当区间加入结果集
            if(!CharRange.canMerge(previous,current)){
                result.add(previous);
                previous = current;
                continue;
            }
            // 如果可以则合并
            previous = CharRange.doMerge(previous,current);
        }
        result.add(previous);
        return result;
    }

    /**
     * 取区间补集
     */
    public static List<CharRange> negativeMulti(List<CharRange> charRanges){
        if(charRanges == null || charRanges.isEmpty()) return null;
        // 先进行区间合并
        List<CharRange> mergedRanges = CharRange.mergeMulti(charRanges);
        List<CharRange> result = new ArrayList<>();
        // 处理头区间
        CharRange current = mergedRanges.get(0);
        char left = current.left,right;
        if(left > (char)0) result.add(CharRange.of(Character.MIN_VALUE,CharRange.back(left)));
        // 处理中间部分数据
        for(int i=1;i<mergedRanges.size();i++){
            left = current.right;
            current = mergedRanges.get(i);
            right = current.left;
            result.add(CharRange.of(CharRange.forward(left),CharRange.back(right)));
        }
        // 处理尾区间
        current = mergedRanges.get(mergedRanges.size()-1);
        right = current.right;
        if(right < Character.MAX_VALUE) result.add(CharRange.of(CharRange.forward(right),Character.MAX_VALUE));
        return result;
    }

    /**
     * 将一系列表达式进行区间合并后返回合并后的表达式
     * @param regexExps 要合并的表达式集合
     * @param isNegative 是否取区间补集
     * @return 合并后的表达式集合
     */
    public static List<RegexExp> mergeRegexExps(List<RegexExp> regexExps,boolean isNegative){
        List<CharRange> charRanges = regexExpsToCharRanges(regexExps);
        List<CharRange> mergedCharRanges = isNegative ? negativeMulti(charRanges) : mergeMulti(charRanges);
        return charRangesToRegexExps(mergedCharRanges);
    }
}
