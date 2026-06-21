package com.adel.common.email

import com.adel.config.SmtpConfig
import jakarta.mail.*
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeBodyPart
import jakarta.mail.internet.MimeMessage
import jakarta.mail.internet.MimeMultipart
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import java.util.*

/**
 * SMTP-based email service for sending transactional emails.
 *
 * Emails are dispatched asynchronously on [Dispatchers.IO] via a
 * dedicated [CoroutineScope] to avoid blocking Ktor's request threads,
 * since Jakarta Mail performs blocking network I/O internally.
 *
 * The HTML template for password-reset emails is loaded once from
 * classpath resources and cached for the lifetime of the service.
 */
class EmailService(private val config: SmtpConfig) {

    private val logger = LoggerFactory.getLogger(EmailService::class.java)
    private val emailScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val session: Session by lazy {
        val props = Properties().apply {
            put("mail.smtp.auth", "true")
            put("mail.smtp.starttls.enable", config.startTls.toString())
            put("mail.smtp.host", config.host)
            put("mail.smtp.port", config.port.toString())
            // Timeouts to avoid indefinite hangs on network issues
            put("mail.smtp.connectiontimeout", TIMEOUT_MS)
            put("mail.smtp.timeout", TIMEOUT_MS)
            put("mail.smtp.writetimeout", TIMEOUT_MS)
        }
        Session.getInstance(props, object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication =
                PasswordAuthentication(config.username, config.password)
        })
    }

    private val passwordResetTemplate: String by lazy {
        val stream = this::class.java.classLoader
            .getResourceAsStream("templates/password-reset-email.html")
            ?: error("Missing classpath resource: templates/password-reset-email.html")
        stream.bufferedReader().use { it.readText() }
    }

    /**
     * Sends a password-reset email asynchronously. The caller returns
     * immediately — delivery failures are logged but never propagated.
     */
    fun sendPasswordResetEmailAsync(toEmail: String, code: String, expiryMinutes: Int) {
        emailScope.launch {
            try {
                val htmlBody = passwordResetTemplate
                    .replace("{{RESET_CODE}}", code)
                    .replace("{{CODE_EXPIRY_MINUTES}}", expiryMinutes.toString())

                val plainBody = buildString {
                    append("Your WC26 password reset code is: $code\n\n")
                    append("This code will expire in $expiryMinutes minutes.\n\n")
                    append("If you didn't request a password reset, please ignore this email.")
                }

                send(
                    to = toEmail,
                    subject = "Reset your password — WC26",
                    plainText = plainBody,
                    html = htmlBody,
                )

                logger.info("[Email] Password reset email sent to {}", toEmail)
            } catch (e: Exception) {
                logger.error("[Email] Failed to send password reset email to {}", toEmail, e)
            }
        }
    }

    /**
     * Low-level send function. Builds a MIME multipart/alternative message
     * containing both a plain-text fallback and the HTML body, then
     * dispatches it via SMTP.
     */
    private fun send(to: String, subject: String, plainText: String, html: String) {
        val message = MimeMessage(session).apply {
            setFrom(InternetAddress(config.fromAddress, config.fromName))
            setRecipients(Message.RecipientType.TO, InternetAddress.parse(to))
            setSubject(subject)

            val multipart = MimeMultipart("alternative").apply {
                // Plain-text part (email clients pick the last part they can render,
                // so HTML goes second to be preferred when supported)
                addBodyPart(MimeBodyPart().apply {
                    setText(plainText, "utf-8")
                })
                addBodyPart(MimeBodyPart().apply {
                    setContent(html, "text/html; charset=utf-8")
                })
            }
            setContent(multipart)
        }
        Transport.send(message)
    }

    private companion object {
        /** 10-second timeout for SMTP connection, read, and write. */
        const val TIMEOUT_MS = "10000"
    }
}
