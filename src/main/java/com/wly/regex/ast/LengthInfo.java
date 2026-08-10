package com.wly.regex.ast;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;


@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class LengthInfo {
    public int minLength;
    public int maxLength; // -1表示无穷

    public void compare(int min,int max){
        this.minLength = Math.min(this.minLength,min);
        if(this.maxLength == -1 || max == -1) this.maxLength = -1;
        else this.maxLength = Math.max(this.maxLength,max);
    }

    public void add(int min,int max){
        this.minLength = this.minLength + min;
        if(this.maxLength == -1 || max == -1) this.maxLength = -1;
        else this.maxLength = this.maxLength + max;
    }

    public void multiply(int min,int max){
        this.minLength = this.minLength * min;
        if(this.maxLength == -1 || max == -1) this.maxLength = -1;
        else this.maxLength = this.maxLength * max;
    }
}
