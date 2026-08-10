package com.wly.regex.match.control;

import com.wly.regex.ast.LengthInfo;
import com.wly.regex.ast.RegexParser;

/**
 * 匹配控制器，包含^$限定符和正则修饰符等一系列匹配控制信息。皆由布尔值表示
 */
public class MatchController {
    public boolean hasStartLimit;
    public boolean hasEndLimit;
    public LengthInfo lengthInfo;

    public MatchController(){}

    public MatchController(RegexParser parser){
        this.hasStartLimit = parser.hasStartLimit;
        this.hasEndLimit = parser.hasEndLimit;
    }
}
