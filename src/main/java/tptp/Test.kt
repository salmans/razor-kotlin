package tptp

import combinator.*
import parser.Parser

val parseA: Parser<Char, Char> = token('A') right give('A')
val parseB: Parser<Char, Char> = token('B') right give('B')

val parseSomething: Parser<Char, String> by lazy { parseSomethingDef }

val parseSomethingDef: Parser<Char, String> by lazy {
    fun makeFirst(a: Char, sth: String): Parser<Char, String> = give("$a$sth")
    fun makeSecond(a: Char): Parser<Char, String> = give("$a")

    (parseA and parseSomething) { (sth, a) -> makeFirst(sth, a) } or
            parseB { makeSecond(it) }
}

fun main(args: Array<String>) {
    println(parseSomethingDef("AAB".asSequence()).first.either({ it.toString() }, { it }))
}


