package com.ascdev.checkmo.data.auth

import kotlin.random.Random

object InviteCodeGenerator {

    const val CODE_LENGTH = 6

    // No 0/O or 1/I/L so codes are easy to read aloud and type.
    private const val Alphabet = "ABCDEFGHJKMNPQRSTUVWXYZ23456789"

    fun generate(random: Random = Random.Default): String =
        (1..CODE_LENGTH).map { Alphabet[random.nextInt(Alphabet.length)] }.joinToString("")

    fun normalize(code: String): String = code.trim().uppercase()
}
