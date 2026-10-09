package com.tobethebest.regex.match.group;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
public class GroupPair {
    public int startIndex;
    public int endIndex;

    public boolean isEquals(){
        return this.startIndex == this.endIndex;
    }
}
