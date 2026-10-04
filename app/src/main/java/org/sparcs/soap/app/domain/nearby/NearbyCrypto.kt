package org.sparcs.soap.app.domain.nearby

import java.math.BigInteger
import java.security.AlgorithmParameters
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.MessageDigest
import java.security.PrivateKey
import java.security.PublicKey
import java.security.SecureRandom
import java.security.interfaces.ECPublicKey
import java.security.spec.ECGenParameterSpec
import java.security.spec.ECParameterSpec
import java.security.spec.ECPoint
import java.security.spec.ECPrivateKeySpec
import java.security.spec.ECPublicKeySpec
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyAgreement
import javax.crypto.Mac
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Buddy nearby protocol v1 primitives (see the cross-team nearby friends plan,
 * §2). Pure JVM so the shared test vectors run as plain unit tests; every
 * output must match iOS (CryptoKit) byte for byte.
 */
object NearbyCrypto {
    const val TOKEN_LENGTH = 12
    const val NONCE_LENGTH = 12
    private const val TAG_BITS = 128
    private const val COORDINATE_LENGTH = 32

    private val LOOKUP_PREFIX = "buddy-nearby-v1/lookup".toByteArray()
    val PRESENCE_INFO = "buddy-nearby-v1/presence".toByteArray()
    val HEADER_AAD = "buddy-nearby-v1/header".toByteArray()
    private val PAIR_INFO = "buddy-nearby-v1/pair".toByteArray()
    val BODY_AAD = "buddy-nearby-v1/body".toByteArray()

    private val secureRandom = SecureRandom()

    val p256: ECParameterSpec by lazy {
        AlgorithmParameters.getInstance("EC").run {
            init(ECGenParameterSpec("secp256r1"))
            getParameterSpec(ECParameterSpec::class.java)
        }
    }

    // MARK: - Keys

    fun generateKeyPair(): KeyPair = KeyPairGenerator.getInstance("EC").run {
        initialize(ECGenParameterSpec("secp256r1"), secureRandom)
        generateKeyPair()
    }

    /** Rebuilds a key pair from a raw 32-byte scalar (test vectors only). */
    fun keyPairFromRawPrivateKey(raw: ByteArray): KeyPair {
        val factory = KeyFactory.getInstance("EC")
        val scalar = BigInteger(1, raw)
        val private = factory.generatePrivate(ECPrivateKeySpec(scalar, p256))
        val point = multiply(p256.generator, scalar)
        val public = factory.generatePublic(ECPublicKeySpec(point, p256))
        return KeyPair(public, private)
    }

    /** ANSI X9.63 uncompressed: `0x04 ‖ X ‖ Y`, each coordinate left-padded to 32 bytes. */
    fun x963(publicKey: PublicKey): ByteArray {
        val point = (publicKey as ECPublicKey).w
        return byteArrayOf(0x04) + point.affineX.toFixedBytes() + point.affineY.toFixedBytes()
    }

    /** Parses an X9.63 public key; throws if it isn't a valid P-256 point. */
    fun publicKeyFromX963(bytes: ByteArray): PublicKey {
        require(bytes.size == 1 + 2 * COORDINATE_LENGTH && bytes[0] == 0x04.toByte()) {
            "Not an uncompressed P-256 public key"
        }
        val x = BigInteger(1, bytes.copyOfRange(1, 1 + COORDINATE_LENGTH))
        val y = BigInteger(1, bytes.copyOfRange(1 + COORDINATE_LENGTH, bytes.size))
        return KeyFactory.getInstance("EC").generatePublic(ECPublicKeySpec(ECPoint(x, y), p256))
    }

    // MARK: - Derivations

    fun sha256(vararg parts: ByteArray): ByteArray = MessageDigest.getInstance("SHA-256").run {
        parts.forEach(::update)
        digest()
    }

    fun token(pubX963: ByteArray): ByteArray = sha256(pubX963).copyOf(TOKEN_LENGTH)

    fun lookupId(token: ByteArray): ByteArray = sha256(LOOKUP_PREFIX, token)

    fun presenceKey(token: ByteArray): ByteArray = hkdf(token, ByteArray(0), PRESENCE_INFO)

    fun sharedSecret(privateKey: PrivateKey, peerPublicKey: PublicKey): ByteArray =
        KeyAgreement.getInstance("ECDH").run {
            init(privateKey)
            doPhase(peerPublicKey, true)
            generateSecret()
        }

    fun pairKey(privateKey: PrivateKey, peerPubX963: ByteArray, myToken: ByteArray, peerToken: ByteArray): ByteArray {
        val shared = sharedSecret(privateKey, publicKeyFromX963(peerPubX963))
        val (low, high) = if (compareUnsigned(myToken, peerToken) <= 0) myToken to peerToken else peerToken to myToken
        return hkdf(shared, low + high, PAIR_INFO)
    }

    /** HKDF-SHA256 (RFC 5869) with a 32-byte output. An empty salt means 32 zero bytes. */
    fun hkdf(ikm: ByteArray, salt: ByteArray, info: ByteArray): ByteArray {
        val prk = hmac(if (salt.isEmpty()) ByteArray(32) else salt, ikm)
        return hmac(prk, info + byteArrayOf(1))
    }

    private fun hmac(key: ByteArray, data: ByteArray): ByteArray = Mac.getInstance("HmacSHA256").run {
        init(SecretKeySpec(key, "HmacSHA256"))
        doFinal(data)
    }

    // MARK: - AEAD

    fun randomBytes(count: Int): ByteArray = ByteArray(count).also(secureRandom::nextBytes)

    /** AES-256-GCM, wire format `nonce(12) ‖ ciphertext ‖ tag(16)`. */
    fun seal(key: ByteArray, plaintext: ByteArray, aad: ByteArray, nonce: ByteArray = randomBytes(NONCE_LENGTH)): ByteArray {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(key, "AES"), GCMParameterSpec(TAG_BITS, nonce))
        cipher.updateAAD(aad)
        return nonce + cipher.doFinal(plaintext)
    }

    /** Opens a sealed box; throws if it was tampered with or the key/AAD is wrong. */
    fun open(key: ByteArray, sealed: ByteArray, aad: ByteArray): ByteArray {
        require(sealed.size >= NONCE_LENGTH + TAG_BITS / 8) { "Sealed box too short" }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(
            Cipher.DECRYPT_MODE,
            SecretKeySpec(key, "AES"),
            GCMParameterSpec(TAG_BITS, sealed, 0, NONCE_LENGTH)
        )
        cipher.updateAAD(aad)
        return cipher.doFinal(sealed, NONCE_LENGTH, sealed.size - NONCE_LENGTH)
    }

    // MARK: - Encoding

    fun base64Url(bytes: ByteArray): String = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)

    fun fromBase64Url(text: String): ByteArray = Base64.getUrlDecoder().decode(text)

    fun hex(bytes: ByteArray): String = bytes.joinToString("") { "%02x".format(it) }

    fun fromHex(text: String): ByteArray {
        require(text.length % 2 == 0) { "Odd-length hex" }
        return ByteArray(text.length / 2) { text.substring(it * 2, it * 2 + 2).toInt(16).toByte() }
    }

    fun compareUnsigned(a: ByteArray, b: ByteArray): Int {
        for (i in 0 until minOf(a.size, b.size)) {
            val diff = (a[i].toInt() and 0xff) - (b[i].toInt() and 0xff)
            if (diff != 0) return diff
        }
        return a.size - b.size
    }

    /** Left-pads (or strips the sign byte from) a coordinate to exactly 32 bytes. */
    private fun BigInteger.toFixedBytes(): ByteArray {
        val raw = toByteArray()
        return when {
            raw.size == COORDINATE_LENGTH -> raw
            raw.size > COORDINATE_LENGTH -> raw.copyOfRange(raw.size - COORDINATE_LENGTH, raw.size)
            else -> ByteArray(COORDINATE_LENGTH - raw.size) + raw
        }
    }

    // MARK: - Point arithmetic (only to derive a public key from a raw test scalar)

    private fun multiply(point: ECPoint, scalar: BigInteger): ECPoint {
        var result = ECPoint.POINT_INFINITY
        var addend = point
        for (i in 0 until scalar.bitLength()) {
            if (scalar.testBit(i)) result = add(result, addend)
            addend = add(addend, addend)
        }
        return result
    }

    private fun add(p: ECPoint, q: ECPoint): ECPoint {
        if (p == ECPoint.POINT_INFINITY) return q
        if (q == ECPoint.POINT_INFINITY) return p
        val prime = (p256.curve.field as java.security.spec.ECFieldFp).p
        val a = p256.curve.a
        val lambda = if (p.affineX == q.affineX) {
            if ((p.affineY + q.affineY).mod(prime) == BigInteger.ZERO) return ECPoint.POINT_INFINITY
            (BigInteger.valueOf(3) * p.affineX.pow(2) + a) * (BigInteger.TWO * p.affineY).modInverse(prime)
        } else {
            (q.affineY - p.affineY) * (q.affineX - p.affineX).modInverse(prime)
        }.mod(prime)
        val x = (lambda.pow(2) - p.affineX - q.affineX).mod(prime)
        val y = (lambda * (p.affineX - x) - p.affineY).mod(prime)
        return ECPoint(x, y)
    }
}
