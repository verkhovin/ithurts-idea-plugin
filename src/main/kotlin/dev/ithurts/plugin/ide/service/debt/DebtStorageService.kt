package dev.ithurts.plugin.ide.service.debt

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import dev.ithurts.git.DiffUtils
import dev.ithurts.git.GitDiffAnalyzer
import dev.ithurts.git.HunkResolvingStrategy
import dev.ithurts.git.LineRange
import dev.ithurts.plugin.client.model.BindingDto
import dev.ithurts.plugin.client.model.DebtDto
import dev.ithurts.plugin.ide.model.*
import git4idea.commands.Git
import io.reflectoring.diffparser.api.UnifiedDiffParser
import io.reflectoring.diffparser.api.model.Diff

/**
 * getDebts/getDebt methods must be run within pooled thread
 */
class DebtStorageService(project: Project) {
    private var debts: Map<String, List<DebtView>>? = null

    private val analyzer = GitDiffAnalyzer(HunkResolvingStrategy())
    private val unifiedDiffParser = UnifiedDiffParser()
    private val git: Git = Git.getInstance()
    private val gitRepositoryService: ItHurtsGitRepositoryService = project.service()
    private val gitRepository = gitRepositoryService.repository

    fun indexDebts(debtDtos: Set<DebtDto>) {
        val debts = debtDtos.map(::dtoToDebt)
        this.debts = debts.flatMap { debt ->
            debt.bindings.map { binding -> binding.filePath to debt }
        }.groupBy({ it.first }, { it.second }).map { it.key to it.value.distinctBy(DebtView::id) }.toMap()
    }

    fun getDebts(): Map<String, List<DebtView>> {
        val diffsByFilePath = diffBetweenWorkingDirAndMainBranch()
        return debts!!.mapValues { calculateActualPositions(it.value, diffsByFilePath) }
    }

    fun getDebts(filePath: String) = getDebts()[filePath] ?: emptyList()

    fun getDebts(filePath: String, lineNumber: Int) =
        getDebts(filePath).filter { it.bindings.any { binding -> binding.actualPosition.start == lineNumber } }

    fun getDebt(id: String): DebtView? {
        return calculateActualPositions(debts?.flatMap { it.value }?.find { it.id == id })
    }

    private fun dtoToDebt(it: DebtDto): DebtView {
        fun mapBinding(bindingDto: BindingDto) =
            Binding(bindingDto.filePath, bindingDto.startLine to bindingDto.endLine,
                bindingDto.advancedBinding?.let { advancedBindingDto ->
                    AdvancedBinding(
                        advancedBindingDto.language,
                        advancedBindingDto.type,
                        advancedBindingDto.name,
                        advancedBindingDto.params,
                        advancedBindingDto.parent
                    )
                },
                bindingDto.status,
                bindingDto.id
            )

        return DebtView(
            it.id, it.title, it.description, it.status,
            it.bindings.map { bindingDto ->
                mapBinding(bindingDto)
            }, it.votes, it.voted, Account(it.reporter.name), it.createdAt, it.updatedAt, it.cost,
            it.hasBindingTrackingLost
        )
    }

    private fun calculateActualPositions(debts: List<DebtView>, diffsByFilePath: Map<String, List<Diff>>): List<DebtView> {
        return debts.map { calculateActualPositions(it, diffsByFilePath) }
    }

    private fun calculateActualPositions(debt: DebtView?): DebtView? {
        debt ?: return null
        val diffsByFilePath = diffBetweenWorkingDirAndMainBranch()
        return calculateActualPositions(debt, diffsByFilePath)
    }

    private fun calculateActualPositions(debt: DebtView, diffsByFilePath: Map<String, List<Diff>>): DebtView {
        return debt.copy(
            bindings = debt.bindings.map {
                it.copy(overriddenPosition = getActualPosition(it, diffsByFilePath[it.filePath]))
            }
        )
    }

    private fun diffBetweenWorkingDirAndMainBranch(): Map<String, List<Diff>> {
        ApplicationManager.getApplication().invokeAndWait {
            FileDocumentManager.getInstance().saveAllDocuments()
        }
        val patch = git.diff(gitRepository, emptyList(), "main").outputAsJoinedString
        val diffs = unifiedDiffParser.parse(patch.toByteArray())
        return diffs.groupBy { DiffUtils.trimFilePath(it.fromFileName) }
    }

    private fun getActualPosition(binding: Binding, diffs: List<Diff>?): LineRange {
        if (diffs == null || diffs.isEmpty()) return binding.lines
        val lookupCodeRangeChange =
            analyzer.lookupCodeRangeChange(binding.lines, diffs)
        return lookupCodeRangeChange.position
    }
}