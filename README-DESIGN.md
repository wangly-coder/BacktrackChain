# 1.文件介绍

DESIGN文件讲述的是该项目中的重要基础类，以及设计的一些思考过程。

# 2.实现思路

## 2.1 基本过程

RegexString --> RegexParser --> AST --> NFABuilder --> NFA --> DFABuilder --> DFA

## 2.2 前端

### 2.2.1 文法产生式

个人认为，文法产生式除了满足你所实现语言所采用的解析技术（本项目采用递归下降解析法）外，还应该结构化，分类化涵盖语言的所有特性。能够有效地构建结构清晰的AST。

```java
<RegexExp> ::= <UnionExp>
    
<UnionExp> ::= <ConcatExp> | <UnionExp>
<UnionExp> ::= <ConcatExp>

<ConcatExp> ::= <RepeatExp><ConcatExp>
<ConcatExp> ::= <RepeatExp>

<RepeatExp> ::= <CharCollectionExp><RepeatModifiers>
<RepeatExp> ::= <CharCollectionExp>
<RepeatModifiers> ::= ? | + | * | {m} | {m,} | {m,n} | 空

<CharCollectionExp> ::= [<CharSequenceExp>]
<CharCollectionExp> ::= [^<CharSequenceExp>]
<CharCollectionExp> ::= <CharGroupExp>

<CharGroupExp> ::= (<UnionExp>)
<CharGroupExp> ::= <CharExp>
    
<CharSequenceExp> ::= <CharRangeExp><CharSequenceExp>
<CharSequenceExp> ::= <CharRangeExp>  
    
<CharRangeExp> ::= <CharExp> - <CharExp> //这里只支持26个英文字母大小写
<CharRangeExp> ::= <CharExp> -
<CharRangeExp> ::= <CharExp>

<CharExp> ::= <BasicChar>
<CharExp> ::= <MetaChar>
    
<BasicChar> ::= 0x0000-0xFFFF // 内部使用Char类型，表示范围是0x00-0xFFFF，也能够表示绝大多数中文
<MetaChar> ::= \d | \w | \s  | \\ // 以及他们的大小写，除了富含语义的单个字符外还包括转义字符

```



# 3.重要类

## RegexParser

正则表达式解析器，无状态的