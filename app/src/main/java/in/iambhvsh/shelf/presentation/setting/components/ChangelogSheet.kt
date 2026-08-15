package `in`.iambhvsh.shelf.presentation.setting.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.m3.markdownTypography
import com.mikepenz.markdown.compose.components.markdownComponents
import kotlin.math.ceil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangelogSheet(
    changelogText: String,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Changelog",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Strip everything before the first "## " and add separators before subsequent versions
                val cleanText = changelogText
                    .replaceFirst(Regex("(?s)^.*?## "), "## ")
                    .replace(Regex("\\r?\\n## "), "\n---\n\n\n## ")
                    .replace(Regex("\\r?\\n### "), "\n\n### ")
                
                Markdown(
                    content = cleanText,
                    typography = markdownTypography(
                        h2 = MaterialTheme.typography.titleLarge.copy(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 32.sp
                        ),
                        h3 = MaterialTheme.typography.titleLarge.copy(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            lineHeight = 22.sp
                        ),
                        text = MaterialTheme.typography.bodyMedium.copy(
                            lineHeight = 24.sp
                        )
                    ),
                    components = markdownComponents(
                        horizontalRule = {
                            WavyDivider(
                                modifier = Modifier.padding(top = 28.dp, bottom = 48.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    )
                )
            }
        }
    }
}

@Composable
fun WavyDivider(
    modifier: Modifier = Modifier,
    color: Color = Color.Gray,
    thickness: Dp = 2.dp,
    period: Dp = 24.dp,
    amplitude: Dp = 4.dp
) {
    Canvas(modifier = modifier.fillMaxWidth().height(amplitude * 2)) {
        val path = Path().apply {
            val periodPx = period.toPx()
            val amplitudePx = amplitude.toPx()
            val halfPeriod = periodPx / 2f
            
            moveTo(0f, amplitudePx)
            
            val count = ceil(size.width / halfPeriod).toInt() + 1
            for (i in 0 until count) {
                relativeQuadraticTo(
                    dx1 = halfPeriod / 2f,
                    dy1 = if (i % 2 == 0) -amplitudePx else amplitudePx,
                    dx2 = halfPeriod,
                    dy2 = 0f
                )
            }
        }
        
        drawPath(
            path = path,
            color = color,
            style = Stroke(
                width = thickness.toPx(),
                cap = StrokeCap.Round
            )
        )
    }
}
