package com.wly.regex.util;

import com.wly.regex.exp.*;

import java.util.List;

/**
 * 正则表达式AST的可视化工具类，采用访问者模式
 */
public class ASTVisualizer implements ASTVisitor<Void, ASTVisualizer.PrintContext> {

    public static String notLastString = "├──"; // 非最后一个兄弟节点
    public static String lastString = "└──"; // 最后一个兄弟节点
    public static String tabStillHasBroPrefix = "│   "; // 还有兄弟节点的节点的子节点前缀
    public static String tabNoBroPrefix = "    "; // 没有兄弟节点的节点的子节点前缀

    protected static class PrintContext{
        boolean isLast;
        String subPrefix;
        StringBuilder stringBuilder = new StringBuilder();

        public PrintContext(){}
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
        PrintContext printContext = new PrintContext(true,"");
        // 开始访问AST
        regexExp.accept(new ASTVisualizer(),printContext);
        return printContext.stringBuilder.toString();
    }

    protected String processRootNode(RegexExp regexExp,PrintContext context){
        // 判断是不是root节点
        if(context.subPrefix.isEmpty()){
            context.stringBuilder.append(regexExp.treeString()).append("\n");
            // 这里我默认设置根节点传递给子树有四个空格的缩进
            context.subPrefix = tabNoBroPrefix;
        }else{
            // 打印自身前缀
            String selfPrefix = context.isLast ? context.subPrefix+lastString: context.subPrefix+notLastString;
            context.stringBuilder.append(selfPrefix).append(regexExp.treeString()).append("\n");
            // 确认子树前缀
            context.subPrefix = context.isLast ? context.subPrefix+tabNoBroPrefix: context.subPrefix+tabStillHasBroPrefix;
        }
        return context.subPrefix;
    }

    @Override
    public Void visit(UnionExp unionExp, PrintContext context) {
        String subPrefix = this.processRootNode(unionExp,context);
        // 访问左子树，不是最后一个节点
        context.isLast = false;
        unionExp.getLeft().accept(this,context);
        // 访问右子树，是最后一个节点，同时要更新子树前缀，遍历时可能被修改
        context.subPrefix = subPrefix;
        context.isLast = true;
        unionExp.getRight().accept(this,context);
        return null;
    }

    @Override
    public Void visit(ConcatExp concatExp, PrintContext context) {
        String subPrefix = this.processRootNode(concatExp,context);
        // 访问左子树，不是最后一个节点
        context.isLast = false;
        concatExp.getLeft().accept(this,context);
        // 访问右子树，是最后一个节点，同时要更新子树前缀，遍历时可能被修改
        context.subPrefix = subPrefix;
        context.isLast = true;
        concatExp.getRight().accept(this,context);
        return null;
    }

    @Override
    public Void visit(RepeatExp repeatExp, PrintContext context) {
        this.processRootNode(repeatExp,context);
        // 访问左子树，是最后一个节点
        context.isLast = true;
        repeatExp.getCharCollectionExp().accept(this,context);
        return null;
    }

    @Override
    public Void visit(CharCollectionExp charCollectionExp, PrintContext context) {
        String subPrefix = this.processRootNode(charCollectionExp,context);
        // 访问各个子树
        List<RegexExp> regexExps = charCollectionExp.getCharSequenceExp();
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
        context.stringBuilder.append(selfPrefix).append(charRangeExp.treeString()).append("\n");
        return null;
    }

    @Override
    public Void visit(MetaExp metaExp, PrintContext context) {
        // 有可能成为root节点需要考虑
        this.processRootNode(metaExp,context);
        return null;
    }

    @Override
    public Void visit(CharExp charExp, PrintContext context) {
        // 有可能成为root节点需要考虑
        this.processRootNode(charExp,context);
        return null;
    }

}
