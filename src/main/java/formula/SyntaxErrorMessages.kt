package formula

typealias ErrorMessage = String

val EXPECTED_NNF_FORMULA: ErrorMessage = "Internal Error: Expecting a formula in negation normal form."
val EXPECTED_STANDARD_SEQUENT: ErrorMessage = "Internal Error: Expecting a geometric sequent in standard form."
val INVALID_SEQUENT_FALSE_BODY: ErrorMessage = "Invalid Sequent: Unexpected falsehood in the body of a sequent."


fun ErrorMessage.internalError() = RuntimeException(this)
fun ErrorMessage.parserException() = ParserException(this)