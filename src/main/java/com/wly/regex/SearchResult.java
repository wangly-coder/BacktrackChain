package com.wly.regex;

import com.wly.regex.match.Pointer;
import com.wly.regex.match.back.BackContext;
import com.wly.regex.match.group.GroupPair;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// TODO 新增捕获组功能，这里需要增加group(int groupId)函数
public class SearchResult {
    private final RegexMatcher regexMatcher;
    protected final Pointer pointer;
    /*
     当前字符串已经没有任何匹配的内容了
     可能是模式串中有^$限定符或者已经遍历匹配了目标字符串的所有子串都找不到任何匹配的
     再下一次匹配已经没有任何意义了
     */
    protected boolean isAlreadyOver;
    protected BackContext backContext; // 回溯上下文信息，用于传递捕获组信息以及后续查找使用
    private GroupPair[] groupPairs; // 匹配到的捕获组元组信息

    public SearchResult(RegexMatcher regexMatcher,Pointer pointer,BackContext backContext
            ,boolean isAlreadyOver) {
        this.regexMatcher = regexMatcher;
        this.pointer = pointer;
        this.backContext = backContext;
        this.fillGroupPairs();
        this.isAlreadyOver = isAlreadyOver;
    }

    protected void fillGroupPairs(){
        this.groupPairs = backContext.groupPairs;
        this.backContext.groupPairs = null;
    }

    /**
     * 根据捕获组组id获取对应的字符串
     * @param groupId 组id
     * @return 对应的字符串
     */
    public String group(int groupId){
        if(groupId < 0) throw new RuntimeException("所给组编号不能小于0");
        String searchString = this.backContext.searchString;
        if (groupId == 0) {
            int preIndex = pointer.preIndex;
            int index = pointer.index;
            return index >= preIndex && index <= searchString.length() ? searchString.substring(preIndex,index) : null;
        }
        // 检验id大小
        int groupNumber = this.groupPairs.length;
        if(groupId > groupNumber) throw new RuntimeException(String.format("所给组编号%d超过最大组编号%d",groupId,groupNumber));
        GroupPair groupPair = this.groupPairs[groupId - 1];
        if(groupPair == null) return null;
        return searchString.substring(groupPair.startIndex,groupPair.endIndex);
    }

    /**
     * 根据捕获组组名字获取对应的字符串
     * @param groupName 组名字
     * @return 对应的字符串
     */
    public String group(String groupName){
        Optional<Integer> groupIdOpt = this.regexMatcher.getGroupId(groupName);
        groupIdOpt.orElseThrow(() -> new RuntimeException(String.format("不存在组名为%s的捕获组",groupName)));
        return this.group(groupIdOpt.get());
    }

    /**
     * 将匹配到的整串和所有捕获组对应的子串按顺序加入到链表中
     * @return
     */
    public List<String> getGroupStrList(){
        List<String> result = new ArrayList<>();
        result.add(this.group(0));
        for(int i = 1; i <= this.groupPairs.length; i++) result.add(this.group(i));
        return result;
    }

    /**
     * 返回下一个搜索结果
     * @return 是否还有下一个搜索结果
     */
    public boolean next(){
        if(this.isAlreadyOver) return false;
        String searchString = this.backContext.searchString;
        // 如果两个指针相同，那么则是空串。只能匹配一次
        if(this.pointer.isEquals()){
            // 如果达到了末尾位置的下一个位置，那么匹配结束。允许指针到达等于字符串长度的索引位置
            if(this.pointer.index > searchString.length()) {
                this.isAlreadyOver = true;
                return false;
            }
                // 没有则都向前推进一步，防止多次空串且死循环
            else pointer.bothForward();
        }
        else pointer.forward();
        // 找不到说明所有子串都匹配失败，后续不再需要寻找了。!相应的状态和捕获组信息都在search函数处理！
        if(!this.regexMatcher.match(searchString,this.pointer,this.backContext)) {
            this.isAlreadyOver = true;
            return false;
        }
        // 设置searchResult的组数据
        this.fillGroupPairs();
        // 清空回溯上下文信息
        this.backContext.clear();
        return true;
    }
}
