package dev.ithurts.plugin.settings

import com.fasterxml.jackson.databind.ObjectMapper
import com.intellij.ide.BrowserUtil
import com.intellij.openapi.components.service
import dev.ithurts.plugin.client.ItHurtsClient
import dev.ithurts.plugin.common.Consts
import dev.ithurts.plugin.model.Tokens
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

    private val accountInfoLabel: JLabel = JLabel()

    private var codeVerifier: String? = null

    private val objectMapper = ObjectMapper()
    private val credentialsService = service<CredentialsService>()

    init {
        connectButton.addActionListener(this::doConnect)
        submitCodeButton.addActionListener(this::submitAuthCode)
        val boxLayout = BoxLayout(panel, BoxLayout.PAGE_AXIS)
        panel.layout = boxLayout

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
        panel.add(accountInfoLabel)
        panel.add(connectButton)

        authCodePanel.isVisible = false;
        connectButton.isVisible = false;
        accountInfoLabel.text = "Loading.."

        if (credentialsService.hasCredentials()) {
            showAccountInfo()
        } else {
            showInitialScreen()
        }
    }

    private fun showInitialScreen() {
        connectButton.isVisible = true;
        authCodePanel.isVisible = false;
        generateCodeAgainButton.isVisible = false;
        accountInfoLabel.isVisible = false;
    }

    private fun showAuthCodeEnteringPanel() {
        connectButton.isVisible = false;
        authCodePanel.isVisible = true;
        generateCodeAgainButton.isVisible = true;
    }

    private fun showAccountInfo() {
        ItHurtsClient.me(
            {
                accountInfoLabel.text = "Logged as ${it.name}"
                connectButton.isVisible = false;
                authCodePanel.isVisible = false;
                generateCodeAgainButton.isVisible = false;
                accountInfoLabel.isVisible = true;
            }, { showInitialScreen() })
    }

    private fun submitAuthCode(e: ActionEvent) {
        val authCode = this.authCodeField.text;
        ItHurtsClient.getTokens(authCode, this.codeVerifier!!) {
            credentialsService.saveTokens(it)
            showAccountInfo();
        }
    }

    private fun doConnect(e: ActionEvent) {
        this.codeVerifier = generateCodeVerifier()
        val url = buildAuthUrl(this.codeVerifier!!)

        showAuthCodeEnteringPanel()

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