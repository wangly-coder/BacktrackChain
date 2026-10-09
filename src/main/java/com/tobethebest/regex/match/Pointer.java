package com.tobethebest.regex.match;

public class Pointer {
    public int preIndex;
    public int index;
    public Pointer(){}

    public Pointer(int index){
        this.preIndex = index;
        this.index = index;
    }

    public void setIndex(int index){
        this.preIndex = index;
        this.index = index;
    }

    /**
     * 旧指针移动到当前指针位置
     */
    public void forward(){
        this.preIndex = this.index;
    }

    /**
     * 当前指针移动到旧指针位置
     */
    public void backward(){
        this.index = this.preIndex;
    }

    /**
     * 旧指针移动到当前指针位置，同时二者向前移动一步
     */
    public void bothForward(){
        this.preIndex = ++index;
    }

    public boolean isEquals(){
        return this.preIndex == this.index;
    }
}
