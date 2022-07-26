package icons

import com.intellij.openapi.util.IconLoader

object ItHurtsIcons {
    @JvmField
    val DEFAULT_ICON = IconLoader.getIcon("/icon.svg", ItHurtsIcons::class.java)
    @JvmField
    val MUTED_ICON = IconLoader.getIcon("/icon_muted.svg", ItHurtsIcons::class.java)
}