package com.wly.regex.match.back;

import com.wly.regex.match.Pointer;

public interface Backer {
    boolean back(String str, Pointer pointer, BackPoint backPoint, BackContext context);
}
