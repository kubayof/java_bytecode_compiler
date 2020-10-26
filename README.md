# Compiler

Simple compiler written using antlr4 parser generator and ASM bytecode modification library.

Example of working code:
```
class Main {
  test() {
    var a = 1;
    int b = 12;
    var c = a * b;
    return c + 2;      
  }
}
```