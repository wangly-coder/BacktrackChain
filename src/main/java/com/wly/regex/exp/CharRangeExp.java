package com.wly.regex.exp;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CharRangeExp extends RegexExp{
    private CharExp left;
    private CharExp right;
}
