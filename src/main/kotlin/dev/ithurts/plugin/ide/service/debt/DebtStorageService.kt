package dev.ithurts.plugin.ide.service.debt

import com.intellij.openapi.project.Project
import dev.ithurts.plugin.client.model.BindingDto
import dev.ithurts.plugin.client.model.DebtDto
import dev.ithurts.plugin.ide.model.*

class DebtStorageService(private val project: Project) {
    private var debts: Map<String, List<DebtView>>? = null //fixme store as model entity, not a dto

    fun indexDebts(debtDtos: Set<DebtDto>) {
        val debts = debtDtos.map(::dtoToDebt)
        this.debts = debts.flatMap { debt ->
            debt.bindings.map { binding -> binding.filePath to debt }
        }.groupBy({ it.first }, { it.second }).map { it.key to it.value.distinctBy(DebtView::id) }.toMap()
    }

    fun getDebts(): Map<String, List<DebtView>> {
        return debts!!
    }

    fun getDebts(filePath: String) = debts?.get(filePath) ?: emptyList()

    fun getDebts(filePath: String, lineNumber: Int) =
        getDebts(filePath).filter { it.bindings.any { binding -> binding.lines.start == lineNumber } }

    fun getDebt(id: String): DebtView? {
        return debts?.flatMap { it.value }?.find { it.id == id }
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


}