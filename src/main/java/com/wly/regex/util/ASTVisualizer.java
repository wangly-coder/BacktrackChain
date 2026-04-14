package com.wly.regex.util;

import com.wly.regex.exp.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 正则表达式AST的可视化工具类，采用访问者模式
 */
public class ASTVisualizer implements ASTVisitor<ASTVisualizer.ASTNode, Void> {

    public static String notLastString = "├──"; // 非最后一个兄弟节点
    public static String lastString = "└──"; // 最后一个兄弟节点
    public static String tabStillHasBroPrefix = "│   "; // 还有兄弟节点的节点的子节点前缀
    public static String tabNoBroPrefix = "    "; // 没有兄弟节点的节点的子节点前缀

    /**
     * 设计与思路
     * Program
     * ├── FuncDecl
     * │   ├── Param
     * │   │   └──List
     * │   └── Block
     * │       └── Return
     * └── VarDecl
     * 对于AST的最好做法，从来都是抽象的看待，遍历的顺序是前序遍历，最后的结果就是左子树字符串+根节点字符串+右子树字符串
     * 现在有这么几个问题：
     * 1.怎么知道打印的这个节点是不是最后一个节点
     * 通过一个list来记录状态，false代表不是最后一个节点，true代表是最后一个节点
     * 2.怎么计算打印的这个节点以及后续子树打印的前缀
     * 我们需要知道的是打印本节点时，分为多层，根父节点前缀，二次根父节点前缀，一直到父节点前缀，对于每一个父节点，如果他们是最后一个节点
     * 那么久添加"    "，否则就是"│   "，我们利用list记录了状态，同时也保证了打印本节点时，前面加入的几个节点都是其父节点
     * 3.怎么做
     * 这里我为了保证ASTVisualizer的工具性，即不存储状态，另外新建了一个内部Context类，使用时new一个即可
     * 除了状态list容器，还需要一个StringBuilder记录结果
     *
     * 更改和后续思考
     * 在有了上述思路后，我尝试在visit方法中实现，但是每一个visit都有大量重复的判断和处理语句
     * 原因在于每次我都要判断是否是最后一个节点，并且重新计算前缀，然后将本次节点从list中移除
     * 既然是树，那么我就采用递归的方法，将AST转化为统一抽象的树即可
     */

    protected static class ASTNode{
        public String treeString;
        public ASTNode parent;
        public List<ASTNode> children;
        boolean isLast;

        public ASTNode(String treeString){
            this.treeString = treeString;
            this.children = new ArrayList<>(2);
        }
    }

//    protected static class ASTVisualizerContext{
//        ASTNode root;
//        StringBuilder stringBuilder = new StringBuilder();
//    }

    /**
     * 获取结果的公共方法
     * @param regexExp AST
     * @return AST的树形字符串
     */
    public String getASTString(RegexExp regexExp){
        ASTNode root = regexExp.accept(this,null);
        StringBuilder stringBuilder = new StringBuilder();
        // 先将根节点访问，后访问后续节点
        stringBuilder.append(root.treeString).append("\n");
        for(int i = 0;i<root.children.size();i++){
            ASTNode child = root.children.get(i);
            if (i == root.children.size() -1) child.isLast = true;
            printASTNode(child,"    ",stringBuilder);
        }
        return stringBuilder.toString();
    }

    /**
     * 具体的实现方法
     * @param root 根节点
     * @param subPrefix 子树打印的前缀
     * @param stringBuilder 保存结果的StringBuilder
     */
    protected void printASTNode(ASTNode root,String subPrefix,StringBuilder stringBuilder){
        // 确定自己的前缀
        String selfPrefix = root.isLast ? lastString : notLastString;
        String connector = subPrefix + selfPrefix;
        // 打印自身树形化字符串
        String selfTreeString = connector + root.treeString;
        stringBuilder.append(selfTreeString).append("\n");
        // 确认子节点前缀
        String newSubPrefix = root.isLast ? subPrefix+tabNoBroPrefix : subPrefix + tabStillHasBroPrefix;
        for(int i = 0;i<root.children.size();i++){
            ASTNode child = root.children.get(i);
            if (i == root.children.size() -1) child.isLast = true;
            printASTNode(child,newSubPrefix,stringBuilder);
        }
    }

    @Override
    public ASTNode visit(UnionExp unionExp, Void context) {
        ASTNode parent = new ASTNode(unionExp.treeString());
        // 访问左子树
        ASTNode leftParent = unionExp.getLeft().accept(this,context);
        // 访问右子树
        ASTNode rightParent = unionExp.getRight().accept(this,context);
        // 设置关系
        leftParent.parent = parent;
        rightParent.parent = parent;
        parent.children.add(leftParent);
        parent.children.add(rightParent);
        return parent;
    }

    @Override
    public ASTNode visit(ConcatExp concatExp, Void context) {
        ASTNode parent = new ASTNode(concatExp.treeString());
        // 访问左子树
        ASTNode leftParent = concatExp.getLeft().accept(this,context);
        // 访问右子树
        ASTNode rightParent = concatExp.getRight().accept(this,context);
        // 设置关系
        leftParent.parent = parent;
        rightParent.parent = parent;
        parent.children.add(leftParent);
        parent.children.add(rightParent);
        return parent;
    }

    @Override
    public ASTNode visit(RepeatExp repeatExp, Void context) {
        ASTNode parent = new ASTNode(repeatExp.treeString());
        // 访问左子树
        ASTNode leftParent = repeatExp.getCharCollectionExp().accept(this,context);
        // 设置父子关系
        leftParent.parent = parent;
        parent.children.add(leftParent);
        return parent;
    }

    @Override
    public ASTNode visit(CharCollectionExp charCollectionExp, Void context) {
        ASTNode parent = new ASTNode(charCollectionExp.treeString());
        // 访问孩子节点
        charCollectionExp.getCharSequenceExp().forEach(child -> {
            ASTNode childNode = new ASTNode(child.treeString());
            childNode.parent = parent;
            parent.children.add(childNode);
        });
        return parent;
    }

    @Override
    public ASTNode visit(CharRangeExp charRangeExp, Void context) {
        return new ASTNode(charRangeExp.treeString());
    }

    @Override
    public ASTNode visit(MetaExp metaExp, Void context) {
        return new ASTNode(metaExp.treeString());
    }

    @Override
    public ASTNode visit(CharExp charExp, Void context) {
        return new ASTNode(charExp.treeString());
    }
}
