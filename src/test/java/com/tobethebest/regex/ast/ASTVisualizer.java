package com.tobethebest.regex.ast;

import com.tobethebest.regex.ast.exp.*;
import com.tobethebest.regex.match.AssertionTest;

import java.util.List;

/**
 * 正则表达式AST的可视化工具类，采用访问者模式
 */
public class ASTVisualizer implements ASTVisitor<Void, ASTVisualizer.PrintContext> {

    private ASTVisualizer(){}
    public static final ASTVisualizer INSTANCE = new ASTVisualizer();

    public static final String notLastString = "├──"; // 非最后一个兄弟节点，三个长度
    public static final String lastString = "└──"; // 最后一个兄弟节点，三个长度
    public static final String tabStillHasBroPrefix = "│   "; // 还有兄弟节点的节点的子节点前缀，四个长度
    public static final String tabNoBroPrefix = "    "; // 没有兄弟节点的节点的子节点前缀，四个长度

    protected class PrintContext{
        boolean isLast;
        String subPrefix;
        StringBuilder stringBuilder = new StringBuilder();

        public PrintContext(boolean isLast, String subPrefix) {
            this.isLast = isLast;
            this.subPrefix = subPrefix;
        }
    }

    /**
     * 获取结果的公共方法
     * @param regexExp AST
     * @return AST的树形字符串
     */
    public static String getASTString(RegexExp regexExp){
        // 创建打印上下文并传递给根节点信息
        PrintContext printContext = INSTANCE.new PrintContext(true,"");
        // 开始访问AST
        regexExp.accept(ASTVisualizer.INSTANCE,printContext);
        return printContext.stringBuilder.toString();
    }

    // 处理节点并返回子树的打印前缀
    protected String processParentNode(String treeString,PrintContext context){
        // 判断是不是root节点
        if(context.subPrefix.isEmpty()){
            context.stringBuilder.append(treeString).append("\n");
            context.subPrefix = tabNoBroPrefix;
        }
        else{
            // 打印自身前缀
            String selfPrefix = context.isLast ? context.subPrefix+lastString: context.subPrefix+notLastString;
            context.stringBuilder.append(selfPrefix).append(treeString).append("\n");
            // 确认子树前缀
            context.subPrefix = context.isLast ? context.subPrefix+tabNoBroPrefix: context.subPrefix+tabStillHasBroPrefix;
        }
        return context.subPrefix;
    }

    @Override
    public Void visit(UnionExp unionExp, PrintContext context) {
        String treeString = "[Union]";
        String subPrefix = this.processParentNode(treeString,context);
        // 访问左子树，不是最后一个节点
        context.isLast = false;
        unionExp.left.accept(this,context);
        // 访问右子树，是最后一个节点，同时要更新子树前缀，遍历时可能被修改
        context.subPrefix = subPrefix;
        context.isLast = true;
        unionExp.right.accept(this,context);
        return null;
    }

    @Override
    public Void visit(ConcatExp concatExp, PrintContext context) {
        String treeString = "[Concat]";
        String subPrefix = this.processParentNode(treeString,context);
        // 访问左子树，不是最后一个节点
        context.isLast = false;
        concatExp.left.accept(this,context);
        // 访问右子树，是最后一个节点，同时要更新子树前缀，遍历时可能被修改
        context.subPrefix = subPrefix;
        context.isLast = true;
        concatExp.right.accept(this,context);
        return null;
    }

    @Override
    public Void visit(RepeatExp repeatExp, PrintContext context) {
        String greedyStr = repeatExp.greedy ? "" : "?",treeString;
        int min = repeatExp.min,max = repeatExp.max;
        if(max == -1) treeString = String.format("[Repeat:{%d,}%s]",min,greedyStr);
        else if(min == max) treeString = String.format("[Repeat:{%d}%s]",min,greedyStr);
        else treeString = String.format("[Repeat:{%d,%d}%s]",min,max,greedyStr);
        this.processParentNode(treeString,context);
        // 访问左子树，是最后一个节点
        context.isLast = true;
        repeatExp.charCollectionExp.accept(this,context);
        return null;
    }

    @Override
    public Void visit(CharCollectionExp charCollectionExp, PrintContext context) {
        String treeString = String.format("[CharCollection%s]",charCollectionExp.negative ? ":^":"");
        String subPrefix = this.processParentNode(treeString,context);
        // 访问各个子树
        List<RegexExp> regexExps = charCollectionExp.charSequenceExp;
        for(int i=0;i<regexExps.size();i++){
            context.isLast = i == regexExps.size() -1;
            context.subPrefix = subPrefix;
            regexExps.get(i).accept(this,context);
        }
        return null;
    }

    /**
     * 剩下的Exp都是作为叶子节点处理的，只需要考虑怎么打印他们即可
     */

    @Override
    public Void visit(CharRangeExp charRangeExp, PrintContext context) {
        // CharRangeExp只能是子树中的叶子节点，不可能是root节点
        // 打印自身前缀
        String selfPrefix = context.isLast ? context.subPrefix+lastString: context.subPrefix+notLastString;
        String treeString = String.format("[CharRange:%s-%s]",charRangeExp.left,charRangeExp.right);
        context.stringBuilder.append(selfPrefix).append(treeString).append("\n");
        return null;
    }

    @Override
    public Void visit(MetaExp metaExp, PrintContext context) {
        // 有可能成为parent节点需要考虑
        String treeString = String.format("[Meta:%s]",metaExp.metaValue.isEmpty() ? "empty" : metaExp.metaValue);
        this.processParentNode(treeString,context);
        return null;
    }

    @Override
    public Void visit(CharExp charExp, PrintContext context) {
        // 有可能成为parent节点需要考虑
        String treeString = String.format("[Char:%s]",charExp.charValue);
        this.processParentNode(treeString,context);
        return null;
    }

    @Override
    public Void visit(GroupExp groupExp, PrintContext context) {
        // 有可能成为parent节点需要考虑
        String treeString = String.format("[Group:%d]", groupExp.groupId);
        this.processParentNode(treeString,context);
        // 访问唯一子树
        context.isLast = true;
        groupExp.innerExp.accept(this,context);
        return null;
    }

    @Override
    public Void visit(GroupRefExp groupRefExp, PrintContext context) {
        // 有可能成为parent节点需要考虑
        String treeString = String.format("[GroupRef:%d]", groupRefExp.refId);
        this.processParentNode(treeString,context);
        return null;
    }

    @Override
    public Void visit(LookaroundExp lookaroundExp, PrintContext context) {
        String typeStr = AssertionTest.getLookTypeStr(lookaroundExp.lookaroundType);
        String treeString = String.format("[Lookaround:%s]",typeStr);
        // 有可能成为parent节点需要考虑
        this.processParentNode(treeString,context);
        // 访问唯一子树
        context.isLast = true;
        lookaroundExp.innerExp.accept(this,context);
        return null;
    }
}
