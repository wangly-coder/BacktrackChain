package com.tobethebest.regex.util;

import com.google.common.collect.Lists;
import com.tobethebest.regex.ast.exp.MetaExp;

import java.util.HashMap;
import java.util.List;

/**
 * 专门处理和元字符相关的工具类
 */
public class MetaUtil {
    public static final List<CharRange> LOW_D_RANGE_LIST;
    public static final List<CharRange> UP_D_RANGE_LIST;
    public static final List<CharRange> LOW_W_RANGE_LIST;
    public static final List<CharRange> UP_W_RANGE_LIST;
    public static final List<CharRange> LOW_S_RANGE_LIST;
    public static final List<CharRange> UP_S_RANGE_LIST;
    public static final List<CharRange> DOT_RANGE_LIST;

    public static final HashMap<String, List<CharRange>> MetaToCharRangeMap = new HashMap<>();

    static {
        // \d的处理
        CharRange lowD = CharRange.of('0','9');
        LOW_D_RANGE_LIST = Lists.newArrayList(lowD);
        MetaUtil.MetaToCharRangeMap.put(MetaExp.LOW_D, LOW_D_RANGE_LIST);

        // \D的处理
        CharRange upD1 = CharRange.of(Character.MIN_VALUE,CharRange.back('0'));
        CharRange upD2 = CharRange.of(CharRange.forward('9'),Character.MAX_VALUE);
        UP_D_RANGE_LIST = Lists.newArrayList(upD1,upD2);
        MetaUtil.MetaToCharRangeMap.put(MetaExp.UP_D,UP_D_RANGE_LIST);

        // \w的处理
        CharRange lowW1 = lowD;
        CharRange lowW2 = CharRange.of('A','Z');
        CharRange lowW3 = CharRange.of('_');
        CharRange lowW4 = CharRange.of('a','z');
        LOW_W_RANGE_LIST = Lists.newArrayList(lowW1,lowW2,lowW3,lowW4);
        MetaUtil.MetaToCharRangeMap.put(MetaExp.LOW_W,LOW_W_RANGE_LIST);

        // \W的处理
        CharRange upW1 = upD1;
        CharRange upW2 = CharRange.of(CharRange.forward('9'),CharRange.back('A'));
        CharRange upW3 = CharRange.of(CharRange.forward('Z'),CharRange.back('_'));
        CharRange upW4 = CharRange.of(CharRange.forward('_'),CharRange.back('a'));
        CharRange upW5 = CharRange.of(CharRange.forward('z'),Character.MAX_VALUE);
        UP_W_RANGE_LIST = Lists.newArrayList(upW1,upW2,upW3,upW4,upW5);
        MetaUtil.MetaToCharRangeMap.put(MetaExp.UP_W,UP_W_RANGE_LIST);

        // \s的处理
        // 空格,\t,\n,\v,\f,\r，后续五个都是连续的，ascii码值9-13
        CharRange lowS1 = CharRange.of('\t','\r');
        CharRange lowS2 = CharRange.of(' ');
        LOW_S_RANGE_LIST = Lists.newArrayList(lowS1,lowS2);
        MetaUtil.MetaToCharRangeMap.put(MetaExp.LOW_S,LOW_S_RANGE_LIST);

        // \S的处理
        CharRange upS1 = CharRange.of(Character.MIN_VALUE,(char)8);
        CharRange upS2 = CharRange.of((char)14,(char)31);
        CharRange upS3 = CharRange.of((char)33,Character.MAX_VALUE);
        UP_S_RANGE_LIST = Lists.newArrayList(upS1,upS2,upS3);
        MetaUtil.MetaToCharRangeMap.put(MetaExp.UP_S,UP_S_RANGE_LIST);

        // .的处理
        CharRange dot1 = CharRange.of(Character.MIN_VALUE,CharRange.back('\n'));
        CharRange dot2 = CharRange.of(CharRange.forward('\n'),Character.MAX_VALUE);
        DOT_RANGE_LIST = Lists.newArrayList(dot1,dot2);
        MetaUtil.MetaToCharRangeMap.put(MetaExp.DOT,DOT_RANGE_LIST);
    }
}
