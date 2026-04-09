package com.wly.regex.exp;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CharCollectionExp extends RegexExp{
    private List<RegexExp> charSequenceExp;
    // @Getter生成isNegative()方法
    private boolean negative;
}
