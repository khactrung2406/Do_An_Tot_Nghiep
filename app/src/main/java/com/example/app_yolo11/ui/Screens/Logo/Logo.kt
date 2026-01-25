import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AppHeaderLogo() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)
    ) {

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(100.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val path = Path().apply {
                    moveTo(w * 0.5f, h * 0.55f)
                    cubicTo(w * 0.8f, h * 0.55f, w * 0.8f, h * 0.2f, w * 0.5f, h * 0.2f)
                    cubicTo(w * 0.1f, h * 0.2f, w * 0.1f, h * 0.85f, w * 0.5f, h * 0.85f)
                    cubicTo(w * 1.05f, h * 0.85f, w * 1.05f, h * 0.05f, w * 0.5f, h * 0.05f)
                }

                // Vẽ nét outline (Gradient)
                drawPath(
                    path = path,
                    brush = Brush.sweepGradient(
                        colors = listOf(Color(0xFF4FC3F7), Color(0xFF0288D1), Color(0xFF01579B))
                    ),
                    style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "TheSea",
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF01579B),
            letterSpacing = (-1).sp
        )
        Text(
            text = "Khám phá đại dương",
            fontSize = 16.sp,
            color = Color(0xFF546E7A),
            fontWeight = FontWeight.Medium
        )
    }
}