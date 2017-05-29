package formula

typealias ErrorMessage = String

val EXPECTED_NNF_FORMULA: ErrorMessage = "Internal Error: Expecting a formula in negation normal form."


fun ErrorMessage.internalError() = RuntimeException(this)