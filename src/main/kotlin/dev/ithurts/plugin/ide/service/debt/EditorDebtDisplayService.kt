package dev.ithurts.plugin.ide.service.debt

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.markup.MarkupModel
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.project.Project
import dev.ithurts.git.DiffUtils
import dev.ithurts.git.GitDiffAnalyzer
import dev.ithurts.git.HunkResolvingStrategy
import dev.ithurts.git.LineRange
import dev.ithurts.plugin.common.FileUtils
import dev.ithurts.plugin.ide.editor.DebtGutterIconRenderer
import dev.ithurts.plugin.ide.model.Binding
import dev.ithurts.plugin.ide.model.BindingStatus
import dev.ithurts.plugin.ide.model.DebtView
import dev.ithurts.plugin.ide.model.start
import git4idea.commands.Git
import io.reflectoring.diffparser.api.UnifiedDiffParser
import io.reflectoring.diffparser.api.model.Diff

class EditorDebtDisplayService(private val project: Project) {
    private val analyzer = GitDiffAnalyzer(HunkResolvingStrategy())
    private val unifiedDiffParser = UnifiedDiffParser()
    private val git: Git = Git.getInstance()
    private val gitRepositoryService: ItHurtsGitRepositoryService = project.service()
    private val gitRepository = gitRepositoryService.repository

    fun renderDebtHighlighters() {
        renderDebtHighlighters(FileEditorManager.getInstance(project).allEditors)
    }

    fun renderDebtHighlighters(fileEditors: Array<FileEditor>) {
        val diffsByFilePath = diffBetweenWorkingDirAndMainBranch()
            .groupBy { DiffUtils.trimFilePath(it.fromFileName) }
        renderDebtHighlighters(fileEditors, diffsByFilePath)
    }

    private fun renderDebtHighlighters(fileEditors: Array<FileEditor>, diffsByFilePath: Map<String, List<Diff>>) {
        ApplicationManager.getApplication().executeOnPooledThread {
            fileEditors.forEach { fileEditor ->
                if (fileEditor !is TextEditor) {
                    return@forEach
                }
                val file = fileEditor.file!!
                val debtsService = project.service<DebtStorageService>()
                val relativePath = FileUtils.getRelativePath(project, file) ?: return@forEach
                val debts = debtsService.getDebts(relativePath)

                val markupModel = fileEditor.editor.markupModel
                if (debts.isEmpty()) return@forEach

                val debtGroupsByStartLine = debts.flatMap { debt ->
                    debt.bindings.map { binding ->
                        val diffs = diffsByFilePath[binding.filePath] ?: return@forEach
                        getActualPosition(binding, diffs).start to (debt)
                    }
                }.groupBy({ it.first }, { it.second })

                ApplicationManager.getApplication().invokeLater {
                    removeOldHighlighters(markupModel)
                    renderNewHighlighters(debtGroupsByStartLine, markupModel, relativePath)
                }
            }
        }
    }

    private fun renderNewHighlighters(
        debtGroupsByStartLine: Map<Int, List<DebtView>>,
        markupModel: MarkupModel,
        relativePath: String,
    ) {
        debtGroupsByStartLine.forEach { (line, debts) ->
            val lineHighlighter = markupModel.addLineHighlighter(
                null, line - 1, 1
            )
            val renderAsActive = debts.flatMap { it.bindings }
                .filter {it.lines.start == line}
                .none { it.status == BindingStatus.TRACKING_LOST }
            lineHighlighter.gutterIconRenderer =
                DebtGutterIconRenderer(
                    debts.size,
                    debts[0].title,
                    relativePath,
                    line,
                    renderAsActive
                )
        }
    }

    private fun removeOldHighlighters(markupModel: MarkupModel) {
        markupModel.allHighlighters.filter { highlighter ->
            highlighter.gutterIconRenderer is DebtGutterIconRenderer
        }.forEach { markupModel.removeHighlighter(it) }
    }

    private fun diffBetweenWorkingDirAndMainBranch(): List<Diff> {
        val diff = git.diff(gitRepository, emptyList(), gitRepositoryService.mainBranch).outputAsJoinedString
        val parse = unifiedDiffParser.parse(diff.toByteArray());
        return parse
    }

    private fun getActualPosition(binding: Binding, diffs: List<Diff>): LineRange {
        val lookupCodeRangeChange =
            analyzer.lookupCodeRangeChange(binding.lines, diffs)
        return lookupCodeRangeChange.position
    }
}