package tptp

import formula.Const
import formula.Func
import formula.Pred
import formula.Var


// Variables
val U = Var("U")
val V = Var("V")
val W = Var("W")
val X = Var("X")
val Y = Var("Y")
val Z = Var("Z")

// Constants
val a = Const("a")
val b = Const("b")
val c = Const("c")
val d = Const("d")
val e = Const("e")

// Functions
val f = Func("f")
val g = Func("g")
val h = Func("h")

// Predicates
val p = Pred("p")
val q = Pred("q")
val r = Pred("r")
val s = Pred("s")
val t = Pred("t")

fun loc(line: Int, column: Int) = Token.Location(line, column)

fun token(type: TokenType, string: String, line: Int = 0, column: Int = 0) = Token(type, string, loc(line, column))
