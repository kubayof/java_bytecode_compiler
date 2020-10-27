grammar NfLang;

classDef: 'class' IDENTIFIER '{' classMember* '}';

classMember: field | method;

field: type variable;

method: IDENTIFIER '(' (formalParam (',' formalParam)*)? ')' block;

formalParam: type variable;

block: '{' statement* '}';

statement
    : expression ';'                                #simpleStatement
    | 'while' '(' boolExpression ')' block          #whileStmt
    | ifStatement                                   #if
    | variable '=' expr ';'                         #assignment
    | type variable ';'                             #typeDef
    | type variable '=' expr ';'                    #typeInitDef
    | 'var' variable '=' expr ';'                   #varInitDef
    | 'return' (expr (',' expr)*)? ';'              #return
    ;

ifStatement
    : 'if' '(' boolExpression ')' block #ifStmt
    | 'if' '(' boolExpression ')' block 'else' elseInner #ifElseStmt
    ;

elseInner
    : ifStatement  #elseIfStmt
    | block        #elseStmt;

expr: expression | boolExpression;

boolExpression: eqExpression | compExpression;

eqExpression
    : expression op4 expression
    | compExpression op4 compExpression
    ;

compExpression
    : bool_term
    | expression op3 expression
    ;

expression: factor (op1 factor)*;

factor
    : term (op2 term)*   #simpleFactor
    | '(' expression ')' #parenthesizedFactor
    ;

op1: ('+' | '-');
op2: ('*' | '/');
op3: ('>' | '>=' | '<' | '<=');
op4: ('==' | '!=');

term: literal | variable;

literal: DECIMAL_NUMBER;

type: 'bool'
    | 'byte'
    | 'short'
    | 'int'
    | 'long'
    | 'float'
    | 'double'
    | 'char'
    ;

bool_term: 'true' | 'false';

variable: IDENTIFIER;

IDENTIFIER: [a-zA-Z][a-zA-Z0-9]*;

DECIMAL_NUMBER: [-]?[0-9]+;

WHITESPACE: [ \n]+ -> skip;
