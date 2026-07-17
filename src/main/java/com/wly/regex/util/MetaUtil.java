package com.wly.regex.util;

import com.google.common.collect.Lists;
import com.wly.regex.ast.exp.MetaExp;

import java.util.HashMap;
import java.util.List;

/**
 * 专门处理和元字符相关的工具类
 */
public class MetaUtil {
    public static final HashMap<String, List<CharRange>> MetaToCharRangeMap = new HashMap<>();

    static {
        // \d的处理
        CharRange lowD = CharRange.of('0','9');
        MetaUtil.MetaToCharRangeMap.put(MetaExp.LOW_D, Lists.newArrayList(lowD));

        // \D的处理
        CharRange upD1 = CharRange.of(Character.MIN_VALUE,CharRange.back('0'));
        CharRange upD2 = CharRange.of(CharRange.forward('9'),Character.MAX_VALUE);
        MetaUtil.MetaToCharRangeMap.put(MetaExp.UP_D,Lists.newArrayList(upD1,upD2));

        // \w的处理
        CharRange lowW1 = lowD;
        CharRange lowW2 = CharRange.of('A','Z');
        CharRange lowW3 = CharRange.of('_');
        CharRange lowW4 = CharRange.of('a','z');
        MetaUtil.MetaToCharRangeMap.put(MetaExp.LOW_W,Lists.newArrayList(lowW1,lowW2,lowW3,lowW4));

        // \W的处理
        CharRange upW1 = upD1;
        CharRange upW2 = CharRange.of(CharRange.forward('9'),CharRange.back('A'));
        CharRange upW3 = CharRange.of(CharRange.forward('Z'),CharRange.back('_'));
        CharRange upW4 = CharRange.of(CharRange.forward('_'),CharRange.back('a'));
        CharRange upW5 = CharRange.of(CharRange.forward('z'),Character.MAX_VALUE);
        MetaUtil.MetaToCharRangeMap.put(MetaExp.UP_W,Lists.newArrayList(upW1,upW2,upW3,upW4,upW5));

        // \s的处理
        // 空格,\r,\n,\t,\f,\v，后续五个都是连续的，ascii码值9-13
        CharRange lowS1 = CharRange.of((char)9,(char)13);
        CharRange lowS2 = CharRange.of((char)32);
        MetaUtil.MetaToCharRangeMap.put(MetaExp.LOW_S,Lists.newArrayList(lowS1,lowS2));

        // \S的处理
        CharRange upS1 = CharRange.of(Character.MIN_VALUE,(char)8);
        CharRange upS2 = CharRange.of((char)14,(char)31);
        CharRange upS3 = CharRange.of((char)33,Character.MAX_VALUE);
        MetaUtil.MetaToCharRangeMap.put(MetaExp.UP_S,Lists.newArrayList(upS1,upS2,upS3));

        // .的处理
        CharRange dot1 = CharRange.of(Character.MIN_VALUE,CharRange.back('\n'));
        CharRange dot2 = CharRange.of(CharRange.forward('\n'),Character.MAX_VALUE);
        MetaUtil.MetaToCharRangeMap.put(MetaExp.DOT,Lists.newArrayList(dot1,dot2));
    }
}
