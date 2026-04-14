package com.wly.regex.util;

import com.wly.regex.exp.*;

public interface ASTVisitor<R,C> {
    R visit(UnionExp unionExp,C context);
    R visit(ConcatExp concatExp,C context);
    R visit(RepeatExp repeatExp,C context);
    R visit(CharCollectionExp charCollectionExp,C context);
    R visit(CharRangeExp charRangeExp,C context);
    R visit(MetaExp metaExp,C context);
    R visit(CharExp charExp,C context);
}
