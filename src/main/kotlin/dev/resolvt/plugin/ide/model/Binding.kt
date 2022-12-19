package dev.resolvt.plugin.ide.model

data class Binding(
    val filePath: String,
    val lines: Pair<Int, Int>,
    val advancedBinding: AdvancedBinding? = null,
    val status: BindingStatus = BindingStatus.ACTIVE,
    val id: String? = null
) {
    override fun toString(): String {
        if (advancedBinding != null) {
            val advancedBindingTitle = advancedBinding.toString()
            return if (status != BindingStatus.TRACKING_LOST)
                advancedBindingTitle
            else {
                "$advancedBindingTitle (tracking lost)"
            }
        }
        val file = filePath.substringAfterLast("/")
        val lines = "${lines.first}${if (lines.first == lines.second) "" else "-${lines.second}"}"
        return "Source code $file:$lines"
    }
}

enum class BindingStatus {
    ACTIVE, ARCHIVED, TRACKING_LOST
}

val Pair<Int, Int>.start: Int
    get() = first
val Pair<Int, Int>.end: Int
    get() = second