package dev.ithurts.plugin.ide.action

import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.psi.PsiElement
import dev.ithurts.plugin.common.UiUtils
import dev.ithurts.plugin.ide.model.Binding
import dev.ithurts.plugin.ide.service.binding.Language
import dev.ithurts.plugin.ide.service.binding.resolver.BindingOptionsResolver
import dev.ithurts.plugin.ide.service.debt.StagedDebtService


class PostDebtAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val elem = e.getData(CommonDataKeys.PSI_FILE)!!.findElementAt(editor.caretModel.offset)!!

        stageNewDebtAndShow(project, editor, elem, e)
    }

    private fun stageNewDebtAndShow(
        project: Project,
        editor: Editor,
        elem: PsiElement,
        e: AnActionEvent,
    ) {
        val language: Language? = Language.from(elem.language)
        val bindingOptions: List<Binding> =
            project.service<BindingOptionsResolver>().getBindingOptions(editor, elem, language)

        val instance = JBPopupFactory.getInstance()
        val popup = instance.createActionGroupPopup(
            "Choose the Binding",
            ChooseTheBindingActionGroup(bindingOptions),
            e.dataContext,
            JBPopupFactory.ActionSelectionAid.NUMBERING,
            true
        )
        popup.showInBestPositionFor(editor)
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = true
    }
}

class ChooseTheBindingActionGroup(
    private val bindingOptions: List<Binding>,
) : ActionGroup() {
    override fun getChildren(anActionEvent: AnActionEvent?): Array<AnAction> {
        return bindingOptions.map { binding -> AddBindingAction(binding) }.toTypedArray()
    }

    internal inner class AddBindingAction(private val binding: Binding) : AnAction(binding.toString()) {
        override fun actionPerformed(e: AnActionEvent) {
            val project = e.project ?: return
            val stagedDebtService = project.service<StagedDebtService>()
            stagedDebtService.stageBinding(binding)

            UiUtils.rerenderReportDebtToolWindow(project)
        }
    }
}
