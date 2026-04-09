package com.wly.regex.exp;

import lombok.Builder;
import lombok.Data;

/**
 * 正则表达式中的连接表达式
 */
@Builder
@Data
public class ConcatExp extends RegexExp{
    private RegexExp left;
    private RegexExp right;
}
