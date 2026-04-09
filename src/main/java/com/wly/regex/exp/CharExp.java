package com.wly.regex.exp;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CharExp extends RegexExp {
    private char charValue;
}
