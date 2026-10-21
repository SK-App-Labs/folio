// File: app/src/main/java/com/folioledger/app/domain/util/MoneyFormatterAndCalculator.kt
package com.folioledger.app.domain.util

import com.folioledger.app.domain.model.CurrencySpec
import com.folioledger.app.domain.model.MinorMoney
import com.folioledger.app.domain.model.NumberGroupingSystem
import java.math.BigDecimal
import java.math.BigInteger
import java.math.MathContext
import java.math.RoundingMode

object MoneyFormatterAndCalculator {

    fun formatMinorMoney(
        money: MinorMoney,
        currencySpec: CurrencySpec,
        grouping: NumberGroupingSystem = currencySpec.defaultGrouping,
        includeSymbol: Boolean = true,
        showPlusSignForPositive: Boolean = false
    ): String {
        val fractionDigits = currencySpec.fractionDigits.coerceAtLeast(0)
        val signPrefix = when {
            money.isNegative -> "-"
            money.isPositive && showPlusSignForPositive -> "+"
            else -> ""
        }

        val absMinor: BigInteger = BigInteger.valueOf(money.amountMinor).abs()
        val divisor: BigInteger = BigInteger.TEN.pow(fractionDigits)
        val integerPartRaw: String = absMinor.divide(divisor).toString()
        val groupedIntegerPart: String = when (grouping) {
            NumberGroupingSystem.INDIAN -> formatIndianGrouping(integerPartRaw)
            NumberGroupingSystem.INTERNATIONAL -> formatInternationalGrouping(integerPartRaw)
        }

        val formattedNumber = if (fractionDigits > 0) {
            val fractionalPart = absMinor.remainder(divisor)
                .toString()
                .padStart(fractionDigits, '0')
            "$groupedIntegerPart.$fractionalPart"
        } else {
            groupedIntegerPart
        }

        val symbolPart = if (includeSymbol) currencySpec.symbol else ""
        return "$signPrefix$symbolPart$formattedNumber"
    }

    private fun formatInternationalGrouping(digits: String): String {
        if (digits.length <= 3) return digits
        val sb = StringBuilder(digits.length + digits.length / 3)
        val firstGroupLen = digits.length % 3
        if (firstGroupLen > 0) {
            sb.append(digits, 0, firstGroupLen)
        }
        var index = firstGroupLen
        while (index < digits.length) {
            if (sb.isNotEmpty()) sb.append(',')
            sb.append(digits, index, index + 3)
            index += 3
        }
        return sb.toString()
    }

    private fun formatIndianGrouping(digits: String): String {
        if (digits.length <= 3) return digits
        val lastThree = digits.substring(digits.length - 3)
        val prefix = digits.substring(0, digits.length - 3)

        val sb = StringBuilder(digits.length + digits.length / 2)
        val firstGroupLen = prefix.length % 2
        if (firstGroupLen > 0) {
            sb.append(prefix, 0, firstGroupLen)
        }
        var index = firstGroupLen
        while (index < prefix.length) {
            if (sb.isNotEmpty()) sb.append(',')
            sb.append(prefix, index, index + 2)
            index += 2
        }
        sb.append(',').append(lastThree)
        return sb.toString()
    }

    fun evaluateKeypadExpressionToMinor(
        expression: String,
        fractionDigits: Int
    ): Result<MinorMoney> {
        if (fractionDigits !in 0..6) {
            return Result.failure(
                IllegalArgumentException("Unsupported fractionDigits: $fractionDigits")
            )
        }

        val sanitized = expression
            .replace(" ", "")
            .replace(",", "")
            .replace('×', '*')
            .replace('÷', '/')

        if (sanitized.isEmpty()) {
            return Result.failure(IllegalArgumentException("Expression cannot be empty."))
        }

        return try {
            val parser = ExpressionParser(sanitized)
            val evaluatedValue: BigDecimal = parser.parse()
            val scaledMajor = evaluatedValue.setScale(fractionDigits, RoundingMode.HALF_UP)
            val minorBigDecimal = scaledMajor.movePointRight(fractionDigits)
            val minorLong = minorBigDecimal.longValueExact()

            if (minorLong <= 0L) {
                Result.failure(
                    IllegalArgumentException("Evaluated transaction total must be strictly positive.")
                )
            } else {
                Result.success(MinorMoney(minorLong))
            }
        } catch (e: ArithmeticException) {
            Result.failure(
                ArithmeticException(e.message ?: "Arithmetic overflow or division by zero.")
            )
        } catch (e: IllegalArgumentException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(
                IllegalArgumentException("Malformed keypad expression: $expression", e)
            )
        }
    }

    private class ExpressionParser(private val input: String) {
        private var pos: Int = 0

        fun parse(): BigDecimal {
            val result = parseExpression()
            if (pos < input.length) {
                throw IllegalArgumentException(
                    "Unexpected character '${input[pos]}' at index $pos"
                )
            }
            return result
        }

        private fun parseExpression(): BigDecimal {
            var left = parseTerm()
            while (pos < input.length) {
                val op = input[pos]
                if (op == '+' || op == '-') {
                    pos++
                    val right = parseTerm()
                    left = if (op == '+') {
                        left.add(right, MathContext.DECIMAL128)
                    } else {
                        left.subtract(right, MathContext.DECIMAL128)
                    }
                } else {
                    break
                }
            }
            return left
        }

        private fun parseTerm(): BigDecimal {
            var left = parseFactor()
            while (pos < input.length) {
                val op = input[pos]
                if (op == '*' || op == '/') {
                    pos++
                    val right = parseFactor()
                    left = if (op == '*') {
                        left.multiply(right, MathContext.DECIMAL128)
                    } else {
                        if (right.compareTo(BigDecimal.ZERO) == 0) {
                            throw ArithmeticException("Division by zero is not allowed.")
                        }
                        left.divide(right, MathContext.DECIMAL128)
                    }
                } else {
                    break
                }
            }
            return left
        }

        private fun parseFactor(): BigDecimal {
            if (pos >= input.length) {
                throw IllegalArgumentException("Unexpected end of expression.")
            }

            val current = input[pos]
            if (current == '+') {
                pos++
                return parseFactor()
            }
            if (current == '-') {
                pos++
                return parseFactor().negate()
            }
            if (current == '(') {
                pos++
                val inside = parseExpression()
                if (pos >= input.length || input[pos] != ')') {
                    throw IllegalArgumentException("Missing closing parenthesis ')'.")
                }
                pos++
                return inside
            }

            val start = pos
            var dotCount = 0
            while (pos < input.length && (input[pos].isDigit() || input[pos] == '.')) {
                if (input[pos] == '.') {
                    dotCount++
                    if (dotCount > 1) {
                        throw IllegalArgumentException("Multiple decimal points in number token.")
                    }
                }
                pos++
            }

            if (start == pos) {
                throw IllegalArgumentException(
                    "Expected number at index $start but found '${input[start]}'."
                )
            }

            val token = input.substring(start, pos)
            if (token == ".") {
                throw IllegalArgumentException("Standalone decimal point is not a valid number.")
            }
            return BigDecimal(token)
        }
    }
}