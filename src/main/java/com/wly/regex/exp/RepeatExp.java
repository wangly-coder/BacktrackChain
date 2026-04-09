package com.wly.regex.exp;

import lombok.Builder;
import lombok.Data;

/**
 * 正则表达式中的重复表达式
 * 关于{}修饰符，只支持{m},{m,}, {m, n}三种形式，且内部不支持任何带有空格的写法
 */

@Data
@Builder
public class RepeatExp extends RegexExp{
    private RegexExp charCollectionExp;
    private int min;
    private int max;
}
