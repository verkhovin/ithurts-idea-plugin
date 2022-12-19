package icons

import com.intellij.openapi.util.IconLoader

object ResolvtIcons {
    @JvmField
    val DEFAULT_ICON = IconLoader.getIcon("/icon.svg", ResolvtIcons::class.java)
    @JvmField
    val MUTED_ICON = IconLoader.getIcon("/icon_muted.svg", ResolvtIcons::class.java)
}