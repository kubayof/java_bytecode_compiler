grammar NfLang;

classDef: 'class' IDENTIFIER '{' classMember* '}';

classMember: field | method;

field: type variable;

method: IDENTIFIER '(' (formalParam (',' formalParam)*)? ')' block;

formalParam: type variable;

block: '{' statement* '}';

statement
    : expression ';'                                #simpleStatement
    | variable '=' expression ';'                   #assignment
    | type variable ';'                             #typeDef
    | type variable '=' expression ';'              #typeInitDef
    | 'var' variable '=' expression ';'             #varInitDef
    | 'return' (expression (',' expression)*)? ';'  #return
    ;

expression: factor (op1 factor)*;

factor
    : term (op2 term)*   #simpleFactor
    | '(' expression ')' #parenthesizedFactor
    ;

op1: ('+' | '-');
op2: ('*' | '/');

term: literal | variable;

literal: DECIMAL_NUMBER;

type: 'byte'
    | 'short'
    | 'int'
    | 'long'
    | 'float'
    | 'double'
    | 'char'
    ;

variable: IDENTIFIER;

IDENTIFIER: [a-zA-Z][a-zA-Z0-9]*;

DECIMAL_NUMBER: [0-9]+;

WHITESPACE: [ \n]+ -> skip;
