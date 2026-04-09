package com.wly.regex.exp;

import lombok.Builder;
import lombok.Data;

/**
 * '|'运算符对应的AST节点类
 */
@Builder
@Data
public class UnionExp extends RegexExp{
    private RegexExp left;
    private RegexExp right;
}
