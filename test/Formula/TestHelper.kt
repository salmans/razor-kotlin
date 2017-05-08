package Formula

import org.junit.jupiter.api.Assertions.assertEquals

// Functions
val f = Func("f")
val g = Func("g")
val h = Func("h")

// Variables
val x = Var("x")
val y = Var("y")
val z = Var("z")

// Relations
val E = Rel("E")
val P = Rel("P")
val Q = Rel("Q")
val R = Rel("R")
val S = Rel("S")


fun assertTheoriesEqual(vararg expected: Formula, actual: Theory?) = assertEquals(Theory(expected.toList()), actual)

fun <T> assertFailure(errorMessage: String, parseFunc: () -> T) {
    return try {
        parseFunc()
        assert(false, { "exception expected!" })
    } catch (e: Exception) {
        assertEquals(errorMessage, e.message)
    }
}