package com.example.data.crypto

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.UUID

object DidManager {

    data class DidIdentity(
        val did: String,
        val publicKeyHex: String,
        val recoveryPhrase: String,
        val createdAt: Long = System.currentTimeMillis()
    )

    private val wordList = listOf(
        "cyber", "quantum", "neural", "matrix", "vector", "latent", "prism", "orbit",
        "neon", "spark", "syhakal", "stellar", "fusion", "tensor", "vortex", "flux",
        "zenith", "hyper", "echo", "beacon", "crypto", "shuttle", "pulsar", "nexus"
    )

    fun createIdentity(customSeed: String? = null): DidIdentity {
        val random = SecureRandom()
        val seedBytes = ByteArray(32)
        random.nextBytes(seedBytes)

        val digest = MessageDigest.getInstance("SHA-256")
        if (customSeed != null) {
            digest.update(customSeed.toByteArray())
        }
        val hash = digest.digest(seedBytes)

        val publicKeyHex = hash.joinToString("") { "%02x".format(it) }
        val didSuffix = publicKeyHex.take(16)
        val did = "did:syhakal:$didSuffix"

        // Generate 12-word mnemonic recovery phrase
        val phraseWords = mutableListOf<String>()
        for (i in 0 until 12) {
            val idx = (hash[i % hash.size].toInt() and 0xFF + i * 7) % wordList.size
            phraseWords.add(wordList[idx])
        }

        return DidIdentity(
            did = did,
            publicKeyHex = publicKeyHex,
            recoveryPhrase = phraseWords.joinToString(" ")
        )
    }

    fun signPayload(did: String, payload: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val input = "$did:$payload:${System.currentTimeMillis()}"
        val signatureBytes = md.digest(input.toByteArray())
        return "sig_" + signatureBytes.joinToString("") { "%02x".format(it) }.take(32)
    }
}
