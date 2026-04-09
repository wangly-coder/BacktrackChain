package com.wly.regex.exp;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CharSequenceExp extends RegexExp{
    private List<RegexExp> charRangeExp;
}
