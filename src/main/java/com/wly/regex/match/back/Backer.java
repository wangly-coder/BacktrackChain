package com.wly.regex.match.back;

import com.wly.regex.match.Pointer;

public interface Backer {
    boolean back(String str, Pointer pointer, BackContext context,BackPoint backPoint);
}
