package dev.ithurts.plugin.ide.settings

import com.intellij.ide.BrowserUtil
import com.intellij.openapi.components.service
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.ui.Messages
import com.intellij.ui.components.JBTextField
import com.intellij.ui.layout.panel
import dev.ithurts.plugin.client.ItHurtsClient
import dev.ithurts.plugin.common.Consts
import dev.ithurts.plugin.ide.service.CredentialsService
import dev.ithurts.plugin.ide.service.ItHurtsProjectInitiator
import net.miginfocom.swing.MigLayout
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.lang3.RandomStringUtils
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.nio.charset.StandardCharsets
import java.util.*
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel

class ItHurtsSettingsWindow {
    private var generateCodeUrl: String? = null
    private var codeVerifier: String? = null
    private var host: String = "https://ithurts.dev"

    val content = JPanel()
    private val  hostField: JBTextField = JBTextField(host)

    private var initialPanel =  panel {
        row {
            button("Connect to It Hurts") {
                host = formatHost(hostField.text)
                navigateToAuthCode()
                show(authCodePanel)
            }
        }
        row {
            label("Your web browser will be navigated to It Hurts page, where you'll get an auth code")
        }
        titledRow("Advanced") {
            row {
                cell {
                    label("Host")
                    component(hostField)
                }
            }
        }
    }

    private fun formatHost(host: String): String {
        val trimmed = host.trim()
        return if (trimmed.endsWith("/")) trimmed.substringBeforeLast("/") else trimmed
    }

    private val authCodePanel = JPanel(MigLayout("fillx"))
    private val authCodeField: JBTextField = JBTextField()
    private val copyUrlButton: JButton = JButton("Copy URL")
    private val submitAuthCodeButton: JButton = JButton("Ok")

    private val connectedPanel = JPanel(MigLayout("fillx"))
    private val accountInfoLabel = JLabel()
    private val generateCodeAgainButton: JButton = JButton("Generate the code again")
    private val logoutButton: JButton = JButton("Logout")

    private val credentialsService = service<CredentialsService>()


    init {
//        initialPanel.add(connectButton, "align center, wrap")
//        initialPanel.add(
//            JLabel("Your web browser will be navigated to It Hurts page, where you'll get an auth code"),
//            "align center, wrap"
//        )
////        initialPanel.add(SeparatorWithText().apply { caption = "Advanced" }, "wrap")
//        initialPanel.add(JLabel("Host"))
//        hostField.emptyText.text = "https://ithurts.dev"
//        hostField.text = "https://ithurts.dev"
//        initialPanel.add(hostField, "span, grow")


        authCodeField.emptyText.text = "Code"
        copyUrlButton.addActionListener {
            Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection(generateCodeUrl), null)
        }

        submitAuthCodeButton.addActionListener { submitAuthCode() }
        authCodePanel.add(authCodeField, "grow")
        authCodePanel.add(submitAuthCodeButton, "align center, wrap")
        authCodePanel.add(copyUrlButton, "align center, wrap")
        authCodePanel.add(generateCodeAgainButton, "align center")

        generateCodeAgainButton.addActionListener { logout() }
        logoutButton.addActionListener { logout() }
        connectedPanel.add(accountInfoLabel, "align center, wrap")
        connectedPanel.add(logoutButton, "align center, wrap")

        if (credentialsService.hasCredentials()) {
            show(connectedPanel)
            showAccountInfo()
        } else {
            show(initialPanel)
        }
    }

    private fun navigateToAuthCode() {
        this.codeVerifier = generateCodeVerifier()
        generateCodeUrl = buildAuthUrl(this.codeVerifier!!)
        BrowserUtil.browse(generateCodeUrl!!)
    }

    private fun submitAuthCode() {
        show(connectedPanel)
        accountInfoLabel.text = "Loading..."
        val authCode = this.authCodeField.text
        service<ItHurtsClient>().getTokens(host, authCode, this.codeVerifier!!, {
            credentialsService.saveCredentials(it, host)
            showAccountInfo()
            initProjectData()
        }) {
            Messages.showErrorDialog(
                it.message,
                "Connection Failed"
            )
            show(initialPanel)
        }
    }

    private fun logout() {
        credentialsService.clearTokens()
        show(initialPanel)
    }

    private fun showAccountInfo() {
        service<ItHurtsClient>().me(
            {
                accountInfoLabel.text = "Logged as ${it.name}"
            }, { show(initialPanel) })
    }

    private fun initProjectData() {
        val projectManager = ProjectManager.getInstanceIfCreated() ?: return
        projectManager.openProjects.forEach { project ->
            ItHurtsProjectInitiator().runActivity(project)
        }
    }

    private fun show(panel: JPanel) {
        initialPanel.isVisible = false
        authCodePanel.isVisible = false
        connectedPanel.isVisible = false

        content.removeAll()
        content.add(panel)

        panel.isVisible = true
    }

    private fun generateCodeVerifier() = RandomStringUtils.random(100, true, true)

    private fun buildAuthUrl(codeVerifier: String): String {
        val codeChallenge = Base64.getUrlEncoder().encodeToString(
            DigestUtils.sha256Hex(codeVerifier.toByteArray(StandardCharsets.UTF_8)).toByteArray()
        )
        return (host + Consts.authUrl).toHttpUrl().newBuilder()
            .addQueryParameter("code_challenge", codeChallenge)
            .build().toString()
    }
}