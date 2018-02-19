package tptp

fun loc(line: Int, column: Int) = Token.Location(line, column)

fun token(type: TokenType, string: String, line: Int = 0, column: Int = 0) = Token(type, string, loc(line, column))
