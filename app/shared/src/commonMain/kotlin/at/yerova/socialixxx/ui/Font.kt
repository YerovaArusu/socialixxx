package at.yerova.socialixxx.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import org.jetbrains.compose.resources.Font
import socialixxx.app.shared.generated.resources.Res
import socialixxx.app.shared.generated.resources.material_symbols_outlined

@Composable
fun getMaterialSymbolsFont(): FontFamily {
    return FontFamily(
        Font(Res.font.material_symbols_outlined)
    )
}