package tptp

import combinator.*
import formula.*
import parser.*
import tools.Either
import java.util.*
import java.util.regex.Pattern


const val RE_NUMERIC = "[0-9]"
const val RE_NON_ZERO_NUMERIC = "[1-9]"
const val RE_DECIMAL = "([0]|$RE_NON_ZERO_NUMERIC$RE_NUMERIC*)"
const val RE_POSITIVE_DECIMAL = "($RE_NON_ZERO_NUMERIC$RE_NUMERIC*)"
const val RE_DECIMAL_FRACTION = "$RE_DECIMAL\\.$RE_NUMERIC$RE_NUMERIC*"
const val SQ_CHAR = "([\\x28-\\x2E\\x32-\\x85\\x87-\\xB0]|[^\\\\][^\\'])"
const val DO_CHAR = "([\\x28-\\x2E\\x32-\\x85\\x87-\\xB0]|[^\\\\][^\\'])"

/**
 * Token Type
 */
// remove special tokens like fof and $fof from tokens and look for them in the parser.
enum class TokenType(vararg regex: String) {
    WHITESPACE("( |\\t|\\f|(\\r\\n)|\\r|\\n)") {
        override fun toString(): String = "whitespace"
    },
    GENTZEN_ARROW("-->") {
        override fun toString(): String = "-->"
    },
    NAND("\\~\\&") {
        override fun toString(): String = "~&"
    },
    NOR("\\~\\|") {
        override fun toString(): String = "~|"
    },
    NOT("~") {
        override fun toString(): String = "~"
    },
    IFF("<->", "<=>") {
        override fun toString(): String = "<=>"
    },
    IMPLIES("=>") {
        override fun toString(): String = "=>"
    },
    IMPLIED("<=") {
        override fun toString(): String = "<="
    },
    NOT_EQUALS("\\!\\=") {
        override fun toString(): String = "!="
    },
    EQUALS("\\=") {
        override fun toString(): String = "="
    },
    OR("\\|") {
        override fun toString(): String = "|"
    },
    QMARK("\\?") {
        override fun toString(): String = "?"
    },
    BANG("\\!") {
        override fun toString(): String = "!"
    },
    AND("\\&") {
        override fun toString(): String = "&"
    },
    STAR("\\*") {
        override fun toString(): String = "*"
    },
    LESS_SIGN("<") {
        override fun toString(): String = "<"
    },
    ARROW(">") {
        override fun toString(): String = ">"
    },
    COMMA(",") {
        override fun toString(): String = ","
    },
    UNSIGNED_RATIONAL("$RE_DECIMAL\\/$RE_POSITIVE_DECIMAL") {
        override fun toString(): String = "<exponent>"
    },
    DECIMAL_EXPONENT("($RE_DECIMAL|$RE_DECIMAL_FRACTION)[Ee]([\\+\\-]?$RE_NUMERIC$RE_NUMERIC*)") {
        override fun toString(): String = "<exponent>"
    },
    DECIMAL_FRACTION(RE_DECIMAL_FRACTION) {
        override fun toString(): String = "<decimal_fraction>"
    },
    DECIMAL(RE_DECIMAL) {
        override fun toString(): String = "<decimal>"
    },
    DOT("\\.") {
        override fun toString(): String = "."
    },
    COLON(":") {
        override fun toString(): String = ":"
    },
    LPAREN("\\(") {
        override fun toString(): String = "("
    },
    RPAREN("\\)") {
        override fun toString(): String = ")"
    },
    LBRACKET("\\[") {
        override fun toString(): String = "["
    },
    RBRACKET("\\]") {
        override fun toString(): String = "]"
    },
    PLUS("\\+") {
        override fun toString(): String = "+"
    },
    MINUS("\\-") {
        override fun toString(): String = "-"
    },
    LEFT_RIGHT_ARROW("<=>") {
        override fun toString(): String = "]"
    },
    FOF("fof") {
        override fun toString(): String = "fof"
    },
    DOLLAR_FOF("\\\$fof") {
        override fun toString(): String = "\$fof"
    },
    UNKNOWN("unknown") {
        override fun toString(): String = "unknown"
    },
    INTRODUCED("introduced") {
        override fun toString(): String = "introduced"
    },
    LOWER_WORD("[a-z][a-zA-Z0-9_]*") {
        override fun toString(): String = "<lower_word>"
    },
    UPPER_WORD("[A-Z][a-zA-Z0-9_]*") {
        override fun toString(): String = "<upper_word>"
    },
    DOLLAR_DOLLAR_WORD("\\$\\$[a-z][a-zA-Z0-9_]*") {
        override fun toString(): String = "<dollar_dollar_word>"
    },
    DOLLAR_WORD("\\$[a-z][a-zA-Z0-9_]*") {
        override fun toString(): String = "<dollar_word>"
    },
    COMMENT("\\%[^\\r\\n]*[\\r\\n]?", "\\/\\*([^*]|\\*+[^*/])*\\**\\*\\/", "\\/\\*([^*]|\\*+[^*/])*") {
        override fun toString(): String = "<comment>"
    },
    SINGLE_QUOTED("'$SQ_CHAR$SQ_CHAR*'") {
        override fun toString(): String = "<sq_word>"
    },
    DOUBLE_QUOTED("\"$DO_CHAR$DO_CHAR*\"") {
        override fun toString(): String = "<do_word>"
    },

    END("^$") {
        override fun toString(): String = "end of input"
    },
    INVALID("[^\\s]+") {
        override fun toString(): String = "invalid token"
    };

    /**
     * The pattern by which the token is detected.
     */
    val pattern: Pattern = Pattern.compile("^(${regex.joinToString(separator = "|")})")
}

/**
 * Token
 */
data class Token(val type: TokenType, val token: String, val location: Location) {
    /**
     * Location of tokens
     */
    data class Location(private val line: Int, private val column: Int) {
        override fun toString(): String = "($line, $column)"
    }
}

/**
 * Tokenizes the input {@code source} according to {@link #TokenType}
 */
fun tokenize(source: String): List<Token> {
    val tokens = LinkedList<Token>()
    var line = 1
    var column = 1
    var src = source

    do {
        var match = false
        var isEnd = false

        for (type in TokenType.values()) {
            val m = type.pattern.matcher(src)
            if (m.find()) {
                match = true
                isEnd = type == TokenType.END
                val tok = if (isEnd) {
                    TokenType.END.toString()
                } else {
                    m.group()
                }
                if (type == TokenType.WHITESPACE || type == TokenType.COMMENT) {
                    var lastCarriageReturn = false
                    tok.forEach {
                        // \r\n, \r or \n indicate line breaks.
                        when (it) {
                            '\r' -> {
                                line++; column = 1; lastCarriageReturn = true
                            }
                            '\n' -> if (!lastCarriageReturn) {
                                line++; column = 1; lastCarriageReturn = false
                            }
                            else -> {
                                column++; lastCarriageReturn = false
                            }
                        }
                    }
                } else {
                    tokens.add(Token(type, tok.trim(), Token.Location(line, column)))
                    column += tok.length
                }
                src = m.replaceFirst("")
                break
            }
        }
        if (!match) throw RuntimeException("Unexpected token at: $src")
    } while (!isEnd)

    return tokens
}


fun expect(type: TokenType, token: String? = null) = { tokens: Sequence<Token> ->
    tokens.firstOrNull().let {
        when {
            it == null -> Either.left(UnexpectedEndOfInputFailure(type.toString())) to tokens
            it.type == type && (if (token != null) it.token == token else true) -> Either.right(it) to tokens.drop(1)
            else -> {
                Either.left(UnexpectedTokenFailure(Token(type, token ?: "", Token.Location(0, 0)))) to tokens
            }
        }
    }
}

fun <R> parens(parser: Parser<Token, R>): Parser<Token, R> =
        between(expect(TokenType.LPAREN), expect(TokenType.RPAREN), parser)

fun <R> bracks(parser: Parser<Token, R>): Parser<Token, R> =
        between(expect(TokenType.LBRACKET), expect(TokenType.RBRACKET), parser)

fun <T, R> chainl2(parser: Parser<T, R>, operator: Parser<T, (R, R) -> R>): Parser<T, R> = run {
    fun helper(l: R, o: (R, R) -> R, r: R): Parser<T, R> = give(o(l, r))

    (parser and operator and chainl1(parser, operator)) { (lo, r) ->
        helper(lo.first, lo.second, r)
    }
}

fun reportError(value: ParserFailure, found: Token?): Exception = when (value) {
    is UnexpectedTokenFailure<*> -> {
        val tokens = value.tokens
        val message = if (tokens.size == 1) {
            "Parse error at ${found?.location ?: "unknown"}: expecting '${(tokens[0] as Token).type}' but '${printToken(found)}' is found."
        } else {
            "Parse error at ${found?.location ?: "unknown"}: " +
                    "expecting one of ${tokens.joinToString(",") { "'${(it as Token).type}'" }} but '${printToken(found)}' is found."
        }
        Exception(message)
    }
    is UnexpectedEndOfInputFailure<*> -> {
        val message = when (value.tokens.size) {
            0 -> "Unexpected 'end of input' was found."
            1 -> "Expecting '${value.tokens[0]}' but 'end of input' was found."
            else -> "Expecting one of ${value.tokens.joinToString(",") { "'$it'" }} but 'end of input' was found."
        }
        Exception(message)
    }
    is UnexpectedFailure -> Exception("Unexpected ${value.message}")
}

private fun printToken(token: Token?): String = token?.token ?: "unknown"

fun pDistinctObject(): Parser<Token, Unit> = expect(TokenType.DOUBLE_QUOTED) right give(Unit)

fun pAtomicWord(): Parser<Token, Token> = expect(TokenType.LOWER_WORD) or expect(TokenType.SINGLE_QUOTED)

fun pName(): Parser<Token, Token> = pAtomicWord() /* TODO or pInteger */

fun pGeneralData(): Parser<Token, Unit> = {
    ((pAtomicWord() right give(Unit)) or pGeneralFunction() or
            (pVariable() right give(Unit)) or (pNumber() right give(Unit)) or
            pDistinctObject() or pFormulaData())(it)
}

fun pGeneralTerm(): Parser<Token, Unit> = {
    (pGeneralData() or
            (pGeneralData() right expect(TokenType.COLON) right pGeneralTerm()) or
            pGeneralList())(it)
}

fun pGeneralList(): Parser<Token, Unit> = bracks(optional(pGeneralTerms()))

fun pGeneralTerms(): Parser<Token, Unit> = sepBy1(pGeneralTerm(), expect(TokenType.COMMA)) right give(Unit)

fun pGeneralFunction(): Parser<Token, Unit> = pAtomicWord() right parens(pGeneralTerms())

fun pVariable(): Parser<Token, Var> = run {
    fun makeVariable(name: String): Parser<Token, Var> = give(Var(name))

    (expect(TokenType.UPPER_WORD)) { makeVariable(it.token) }
}

fun pReal(): Parser<Token, String> = run {
    fun makeInteger(sign: String?, i: String): Parser<Token, String> = give("${sign ?: ""}$i")

    (optionMaybe(pSign()) and pUnsignedReal()) { (s, i) -> makeInteger(s, i) }
}

fun pUnsignedReal(): Parser<Token, String> = run {
    fun makeString(token: Token): Parser<Token, String> = give(token.token)
    (expect(TokenType.DECIMAL_FRACTION) or expect(TokenType.DECIMAL_EXPONENT)) { makeString(it) }
}

fun pRational(): Parser<Token, String> = run {
    fun makeInteger(sign: String?, i: String): Parser<Token, String> = give("${sign ?: ""}$i")

    (optionMaybe(pSign()) and pUnsignedRational()) { (s, i) -> makeInteger(s, i) }
}

fun pUnsignedRational(): Parser<Token, String> = run {
    fun makeString(t: Token): Parser<Token, String> = give(t.token)
    (expect(TokenType.UNSIGNED_RATIONAL)){ makeString(it) }
}

fun pSign(): Parser<Token, String> = run {
    fun makeString(t: Token): Parser<Token, String> = give(t.token)

    (expect(TokenType.PLUS) or expect(TokenType.MINUS)) { makeString(it) }
}

fun pInteger(): Parser<Token, String> = run {
    fun makeInteger(sign: String?, i: String): Parser<Token, String> = give("${sign ?: ""}$i")

    (optionMaybe(pSign()) and pUnsignedInteger()) { (s, i) -> makeInteger(s, i) }
}

fun pUnsignedInteger(): Parser<Token, String> = run {
    fun makeString(t: Token): Parser<Token, String> = give(t.token)
    (expect(TokenType.DECIMAL)) { makeString(it) }
}

fun pNumber(): Parser<Token, String> = pInteger() or pRational() or pReal()

fun pFOFUnitaryFormula(): Parser<Token, Formula> = {
    (pFOFQuantifiedFormula() or pFOFUnaryFormula() or pFOFAtomicFormula() or parens(pFOFLogicFormula()))(it)
}

fun pFOFQuantifiedFormula(): Parser<Token, Formula> = {
    fun makeFormula(quantifier: Token, variables: List<Var>, formula: Formula): Parser<Token, Formula> {
        return give(if (quantifier.type == TokenType.BANG) {
            Forall(variables, formula)
        } else {
            Exists(variables, formula)
        })
    }

    // the compiler is dumb!
    fun helper(): Parser<Token, Formula> = ((expect(TokenType.BANG) or expect(TokenType.QMARK)) and
            bracks(pFOFVariableList()) left expect(TokenType.COLON) and pFOFUnitaryFormula()) { (qvs, f) ->
        makeFormula(qvs.first, qvs.second, f)
    }

    helper()(it)
}

fun pFOFBinaryNonAssoc(): Parser<Token, Formula> = {
    fun operator(): Parser<Token, (Formula, Formula) -> Formula> = run {
        fun makeIFF(): Parser<Token, (Formula, Formula) -> Formula> = give({ l, r -> Iff(l, r) })
        fun makeIMPLIES(): Parser<Token, (Formula, Formula) -> Formula> = give({ l, r -> Implies(l, r) })
        fun makeIMPLIED(): Parser<Token, (Formula, Formula) -> Formula> = give({ l, r -> Implies(r, l) })
        fun makeNAND(): Parser<Token, (Formula, Formula) -> Formula> = give({ l, r -> Not(And(l, r)) })
        fun makeNOR(): Parser<Token, (Formula, Formula) -> Formula> = give({ l, r -> Not(Or(l, r)) })

        (expect(TokenType.IFF) or expect(TokenType.IMPLIES) or expect(TokenType.IMPLIED)
                or expect(TokenType.NAND) or expect(TokenType.NOR)) {
            when (it.type) {
                TokenType.IMPLIES -> makeIMPLIES()
                TokenType.IMPLIED -> makeIMPLIED()
                TokenType.IFF -> makeIFF()
                TokenType.NAND -> makeNAND()
                TokenType.NOR -> makeNOR()
                else -> throw RuntimeException("unreachable code")
            }
        }
    }

    chainl2(pFOFUnitaryFormula(), operator())(it)
}

fun pFOFAndFormula(): Parser<Token, Formula> = chainl2(pFOFUnitaryFormula(), expect(TokenType.AND) right give({ l, r -> And(l, r) }))

fun pFOFOrFormula(): Parser<Token, Formula> = chainl2(pFOFUnitaryFormula(), expect(TokenType.OR) right give({ l, r -> Or(l, r) }))

fun pFOFBinaryAssoc(): Parser<Token, Formula> = {
    (attempt(pFOFOrFormula()) or pFOFAndFormula())(it)
}

fun pFOFBinaryFormula(): Parser<Token, Formula> = {
    (attempt(pFOFBinaryNonAssoc()) or attempt(pFOFBinaryAssoc()))(it)
}


fun pFOFLogicFormula(): Parser<Token, Formula> = {
    (pFOFBinaryFormula() or pFOFUnitaryFormula())(it)
}

fun pFOFUnaryFormula(): Parser<Token, Formula> = {
    fun makeNot(formula: Formula): Parser<Token, Formula> = give(Not(formula))

    // the compiler is buggy!
    fun helper(): Parser<Token, Formula> = ((expect(TokenType.NOT) right pFOFUnitaryFormula()){ makeNot(it) }) or attempt(pFOFInfixUnary())

    helper()(it)
}

fun pFOFPlainTerm(): Parser<Token, Term> = {
    fun makeConstant(t: Token): Parser<Token, Const> = give(Const(t.token))
    fun makeFunctionTerm(f: Token, args: Terms): Parser<Token, Term> = give(App(Func(f.token), args))
    // stupid compiler
    fun helper(): Parser<Token, Term> = (pAtomicWord() and optionMaybe(parens(pFOFArguments()))){ (f, args) ->
        if (args == null) {
            makeConstant(f)
        } else {
            makeFunctionTerm(f, args)
        }
    }
    helper()(it)
}

fun pFOFFunctionTerm(): Parser<Token, Term> = pFOFPlainTerm() // TODO or pFOFDefinedTerm or pFOFSystemTerm

fun pFOFTerm(): Parser<Token, Term> = pFOFFunctionTerm() or pVariable()

fun pFOFArguments(): Parser<Token, Terms> = sepBy1(pFOFTerm(), expect(TokenType.COMMA))

fun pFOFPlainAtomicFormula(): Parser<Token, Formula> = run {
    fun makeFunctionTerm(p: Token, args: Terms): Parser<Token, Atom> = give(Atom(Pred(p.token), args))

    (pAtomicWord() and option(emptyList(), parens(pFOFArguments()))){ (f, args) ->
        makeFunctionTerm(f, args)
    }
}

fun pFOFAtomicFormula(): Parser<Token, Formula> = pFOFPlainAtomicFormula()// TODO or pFOFDefinedAtomicFormula or pFOFSystemAtomicFormula

fun pFOFSequent(): Parser<Token, Formula> = {
    // TODO
    fun makeSequence(l: Unit, r: Unit): Parser<Token, Formula> = give(TRUE)

    ((pFOFFormulaTuple() left expect(TokenType.GENTZEN_ARROW) and pFOFFormulaTuple()){ (l, r) ->
        makeSequence(l, r)
    } or parens(pFOFSequent()))(it)
}

fun pFOFFormula(): Parser<Token, Formula> = pFOFLogicFormula() or pFOFSequent()

fun pFormulaData(): Parser<Token, Unit> = expect(TokenType.DOLLAR_FOF) right parens(pFOFFormula()) right give(Unit)

fun pUsefulInfo(): Parser<Token, Unit> = pGeneralList()

fun pOptionalInfo(): Parser<Token, Unit> = optional(expect(TokenType.COMMA) right pUsefulInfo())

fun pDagSource(): Parser<Token, Unit> = { _ ->
    TODO()
}

fun pInternalSource(): Parser<Token, Unit> = { _ ->
    TODO()
}

fun pExternalSource(): Parser<Token, Unit> = { _ ->
    TODO()
}

fun pSources(): Parser<Token, Unit> = sepBy1(pSource(), expect(TokenType.COMMA)) right give(Unit)

fun pSource(): Parser<Token, Unit> = {
    (pDagSource() or pInternalSource() or
            pExternalSource() or (expect(TokenType.UNKNOWN) right give(Unit)) or bracks(pSources()))(it)
}

fun pTPTPFile(): Parser<Token, List<Formula>> = many(pTPTPInput())

fun pTPTPInput(): Parser<Token, Formula> = pAnnotatedFormula() /*TODO or pInclude() */

fun pAnnotatedFormula(): Parser<Token, Formula> = pFOFAnnotated() /*TODO or pCNFAnnotated() */

fun pFOFAnnotated(): Parser<Token, Formula> =
        expect(TokenType.FOF) right
                parens(
                        pName() right
                                expect(TokenType.COMMA) right
                                expect(TokenType.LOWER_WORD) right
                                expect(TokenType.COMMA) right
                                pFOFFormula() /*TODO left pAnnotations*/) left
                expect(TokenType.DOT)

fun pFormulaRole(): Parser<Token, String> = run {
    fun makeString(token: Token): Parser<Token, String> = give(token.token)

    (expect(TokenType.LOWER_WORD)){ makeString(it) }
}

private val pAnnotations: Parser<Token, Unit> = optional(expect(TokenType.COMMA) right pSource() right pOptionalInfo()) right give(Unit)

fun pFOFFormulaTuple(): Parser<Token, Unit> = bracks(sepBy(pFOFLogicFormula(), expect(TokenType.COMMA))) right give(Unit)

fun pFOFVariableList(): Parser<Token, List<Var>> = sepBy1(pVariable(), expect(TokenType.COMMA))

fun pFOFInfixUnary(): Parser<Token, Formula> = run {
    fun makeFormula(t1: Term, t2: Term): Parser<Token, Formula> = give<Token, Formula>(Not(Equals(t1, t2)))

    (pFOFTerm() left expect(TokenType.NOT_EQUALS) and pFOFTerm()){ (l, r) -> makeFormula(l, r) }
}

fun pFOFSystemTerm(): Parser<Token, Term> = run {
    fun makeConstant(t: Token): Parser<Token, Const> = give(Const(t.token))
    fun makeFunctionTerm(f: Token, args: Terms): Parser<Token, Term> = give(App(Func(f.token), args))
    (expect(TokenType.DOLLAR_WORD)){
        makeConstant(it)
    } or (expect(TokenType.DOLLAR_WORD) and parens(pFOFArguments())){ (f, args) ->
        makeFunctionTerm(f, args)
    }
}

// TODO
fun pDefinedTerm(): Parser<Token, Term> = (pNumber() or pDistinctObject()) right give(Var("x"))

fun pAtomicDefinedWord(): Parser<Token, String> = run {
    fun makeString(token: Token): Parser<Token, String> = give(token.token)

    (expect(TokenType.DOLLAR_WORD)) { makeString(it) }
}

fun pFOFDefinedPlainTerm(): Parser<Token, Term> = run {
    fun makeConstant(t: String): Parser<Token, Const> = give(Const(t))
    fun makeFunctionTerm(f: String, args: Terms): Parser<Token, Term> = give(App(Func(f), args))
    (pAtomicDefinedWord()){
        makeConstant(it)
    } or (pAtomicDefinedWord() and parens(pFOFArguments())){ (f, args) ->
        makeFunctionTerm(f, args)
    }
}

fun pFOFDefinedAtomicTerm(): Parser<Token, Term> = pFOFDefinedPlainTerm()

fun pFOFDefinedTerm(): Parser<Token, Term> = pDefinedTerm() or pFOFDefinedAtomicTerm()


fun pFOFSystemAtomicFormula(): Parser<Token, Formula> = { token ->
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
}

fun pFOFDefinedAtomicFormula(): Parser<Token, Formula> = { token ->
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
}

fun pFileName(): Parser<Token, String> = run {
    fun makeString(token: Token): Parser<Token, String> = give(token.token)

    (expect(TokenType.SINGLE_QUOTED)) { makeString(it) }
}

fun pAtomicSystemWord(): Parser<Token, String> = run {
    fun makeString(token: Token): Parser<Token, String> = give(token.token)

    (expect(TokenType.DOLLAR_DOLLAR_WORD)) { makeString(it) }
}


fun main(args: Array<String>) {
    val source = "fof(prove_reflexivity, conjecture, (! [C] : part_of(C, C)))."
    val tokens = tokenize(source).asSequence()
    val result = pFOFAnnotated()(tokens)
    System.out.println(result.first.either({ reportError(it, result.second.firstOrNull()) }, { it }))
}
