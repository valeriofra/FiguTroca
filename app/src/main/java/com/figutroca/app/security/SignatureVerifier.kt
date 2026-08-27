package com.figutroca.app.security

import android.util.Base64
import java.security.KeyFactory
import java.security.PublicKey
import java.security.Signature
import java.security.spec.X509EncodedKeySpec

/**
 * Base for the anti-forgery layer (README §2, "Autenticidade").
 *
 * Every `collectible` collection file is signed with the author's *private*
 * key; the app ships only the matching *public* key and verifies the signature
 * before trusting a file. Without the private key nobody can produce content
 * the app accepts as authentic — and it works fully offline.
 *
 * The public key is embedded here as a Base64 X.509 (SubjectPublicKeyInfo)
 * string. It's intentionally blank for now: [isConfigured] is false and
 * [verify] returns false until the real key is pasted in, so nothing is ever
 * accepted by accident before the signing setup exists.
 *
 * minSdk is 26, so we use RSA (SHA256withRSA) or EC (SHA256withECDSA). Ed25519
 * would be nicer but needs API 33.
 */
object SignatureVerifier {

    /** Base64-encoded X.509 public key. Fill in when the signing keypair exists. */
    private const val PUBLIC_KEY_BASE64: String = ""

    /** Algorithm of the embedded key: "RSA" or "EC". */
    private const val KEY_ALGORITHM: String = "RSA"

    /** Signature algorithm matching the key: "SHA256withRSA" or "SHA256withECDSA". */
    private const val SIGNATURE_ALGORITHM: String = "SHA256withRSA"

    /** True once a real public key is embedded; until then verification is off. */
    val isConfigured: Boolean get() = PUBLIC_KEY_BASE64.isNotBlank()

    private val publicKey: PublicKey? by lazy {
        if (!isConfigured) return@lazy null
        runCatching {
            val bytes = Base64.decode(PUBLIC_KEY_BASE64, Base64.DEFAULT)
            KeyFactory.getInstance(KEY_ALGORITHM)
                .generatePublic(X509EncodedKeySpec(bytes))
        }.getOrNull()
    }

    /**
     * Verifies a detached [signature] (Base64) over [data] with the embedded
     * public key. Returns false when unconfigured or on any error — callers
     * should treat "not configured" as "cannot prove authenticity yet".
     */
    fun verify(data: ByteArray, signature: String): Boolean {
        val key = publicKey ?: return false
        return runCatching {
            val sigBytes = Base64.decode(signature, Base64.DEFAULT)
            Signature.getInstance(SIGNATURE_ALGORITHM).run {
                initVerify(key)
                update(data)
                verify(sigBytes)
            }
        }.getOrDefault(false)
    }

    /** Convenience for verifying a text file's signature. */
    fun verify(text: String, signature: String): Boolean =
        verify(text.toByteArray(Charsets.UTF_8), signature)
}
