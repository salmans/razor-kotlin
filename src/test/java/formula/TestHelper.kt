package formula

import org.junit.Assert.assertEquals


// Functions
val f = Func("f")
val g = Func("g")
val h = Func("h")
// Skolem functions
val sk_0 = Func("sk#0")
val sk_1 = Func("sk#1")
val sk_2 = Func("sk#2")
val sk_3 = Func("sk#3")
val sk_4 = Func("sk#4")

// Variables
val u = Var("u")
val u1 = Var("u1")
val u2 = Var("u2")
val u3 = Var("u3")
val u4 = Var("u4")
val u_1 = Var("u'")
val u_2 = Var("u''")
val u_3 = Var("u'''")
val u_4 = Var("u''''")
val v = Var("v")
val v1 = Var("v1")
val v2 = Var("v2")
val v3 = Var("v3")
val v4 = Var("v4")
val v_1 = Var("v'")
val v_2 = Var("v''")
val v_3 = Var("v'''")
val v_4 = Var("v''''")
val w = Var("w")
val w1 = Var("w1")
val w2 = Var("w2")
val w3 = Var("w3")
val w4 = Var("w4")
val w_1 = Var("w'")
val w_2 = Var("w''")
val w_3 = Var("w'''")
val w_4 = Var("w''''")
val x = Var("x")
val x1 = Var("x1")
val x2 = Var("x2")
val x3 = Var("x3")
val x4 = Var("x4")
val x_1 = Var("x'")
val x_2 = Var("x''")
val x_3 = Var("x'''")
val x_4 = Var("x''''")
val y = Var("y")
val y1 = Var("y1")
val y2 = Var("y2")
val y3 = Var("y3")
val y4 = Var("y4")
val y_1 = Var("y'")
val y_2 = Var("y''")
val y_3 = Var("y'''")
val y_4 = Var("y''''")
val z = Var("z")
val z1 = Var("z1")
val z2 = Var("z2")
val z3 = Var("z3")
val z4 = Var("z4")
val z_1 = Var("z'")
val z_2 = Var("z''")
val z_3 = Var("z'''")
val z_4 = Var("z''''")

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

val BAD_TERM = object: Term(){
    override fun print() = throw RuntimeException("Invalid Term")

    override val freeVars
        get() = throw RuntimeException("Invalid Term")

}

val BAD_FORMULA = object: Formula(){
    override fun print() = throw RuntimeException("Invalid Term")

    override val freeVars
        get() = throw RuntimeException("Invalid Term")

}