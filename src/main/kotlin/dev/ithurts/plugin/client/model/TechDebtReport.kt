package dev.ithurts.plugin.client.model

import com.fasterxml.jackson.annotation.JsonAutoDetect
import dev.ithurts.plugin.ide.service.binding.Language
import dev.ithurts.plugin.ide.model.AdvancedBinding as IdeInternalBinding
import dev.ithurts.plugin.ide.model.Binding as IdeBinding

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class TechDebtReport(
    val title: String,
    val description: String,
    val remoteUrl: String,
    val bindings: List<Binding>
) {
    companion object {
        fun from(
            title: String,
            description: String,
            remoteUrl: String,
            binding: List<IdeBinding>
        ): TechDebtReport {
            return TechDebtReport(
                title,
                description,
                remoteUrl,
                binding.map { Binding.from(it) }
            )
        }
    }
}


class Binding(
    val filePath: String,
    val startLine: Int,
    val endLine: Int,
    val advancedBinding: AdvancedBinding?
) {
    companion object {
        fun from(binding: IdeBinding): Binding {
            return Binding(
                binding.filePath,
                binding.lines.first,
                binding.lines.second,
                binding.advancedBinding?.let { AdvancedBinding.from(it) }
            )
        }
    }
}


class AdvancedBinding(
    val language: Language,
    val type: String,
    val name: String,
    val params: List<String>,
    val parent: String?
) {
    companion object {
        fun from(advancedBinding: IdeInternalBinding) = AdvancedBinding(
            advancedBinding.language,
            advancedBinding.type,
            advancedBinding.name,
            advancedBinding.params,
            advancedBinding.parent
        )
    }
}