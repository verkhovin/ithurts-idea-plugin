package dev.ithurts.plugin.settings

import com.fasterxml.jackson.databind.ObjectMapper
import com.intellij.ide.BrowserUtil
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import dev.ithurts.plugin.ItHurtsClient
import dev.ithurts.plugin.common.Consts
import dev.ithurts.plugin.service.CredentialsService
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.lang3.RandomStringUtils
import java.awt.*
import java.awt.event.ActionEvent
import java.nio.charset.StandardCharsets
import java.util.*
import javax.swing.*


class ItHurstSettingsManager() {
    val panel = JPanel()
    private val connectButton: JButton = JButton("Connect to It Hurts")
    private val submitCodeButton: JButton = JButton("Ok")
    private val generateCodeAgainButton: JButton = JButton("Generate the code again")

    private val authCodePanel: JPanel = JPanel()
    private val authCodeLabel: JLabel = JLabel("Enter the code here");
    private val authCodeField: JTextField = JTextField("")

    private var codeVerifier: String? = null

    private val objectMapper = ObjectMapper()

    init {
        connectButton.addActionListener(this::doConnect)
        submitCodeButton.addActionListener(this::submitAuthCode)
        val boxLayout = BoxLayout(panel, BoxLayout.PAGE_AXIS)
        panel.layout = boxLayout
        panel.add(connectButton)

        authCodePanel.add(authCodeLabel)
        authCodeField.maximumSize = Dimension(200, 30)
        authCodeField.preferredSize = Dimension(200, 30)
        authCodeField.minimumSize = Dimension(200, 30)

        authCodePanel.add(authCodeField)
        authCodePanel.add(submitCodeButton)
        authCodePanel.maximumSize = Dimension(1000, 50)
        panel.add(authCodePanel)

        panel.add(Box.createRigidArea(Dimension(0, 5)))
        panel.add(generateCodeAgainButton)
        authCodePanel.isVisible = false;
        generateCodeAgainButton.isVisible = false;
    }

    private fun submitAuthCode(e: ActionEvent) {
        val authCode = this.authCodeField.text;
        ItHurtsClient.getTokens(authCode, this.codeVerifier!!) {
            val credentialsService = service<CredentialsService>()
            credentialsService.saveTokens(it)
        }
    }

    private fun doConnect(e: ActionEvent) {
        this.codeVerifier = generateCodeVerifier()
        val url = buildAuthUrl(this.codeVerifier!!)

        connectButton.isVisible = false
        authCodePanel.isVisible = true;
        generateCodeAgainButton.isVisible = true;

        BrowserUtil.browse(url)
    }

    private fun generateCodeVerifier() = RandomStringUtils.random(100, true, true)


    private fun buildAuthUrl(codeVerifier: String): String {
        val codeChallenge = Base64.getUrlEncoder().encodeToString(
                DigestUtils.sha256Hex(codeVerifier.toByteArray(StandardCharsets.UTF_8)).toByteArray()
        );
        return Consts.authUrl.toHttpUrl().newBuilder()
                .addQueryParameter("code_challenge", codeChallenge)
                .build().toString()
    }

}