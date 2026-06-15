package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MyApplicationTheme
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.roundToInt
import kotlin.random.Random

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        FestiveEffectsDashboard()
      }
    }
  }
}

// Particle details for Snowflakes
class SnowflakeParticle(
  var x: Float,
  var y: Float,
  var initialX: Float,
  val speed: Float,
  val size: Float, // size in DP
  val rotationSpeed: Float,
  var rotation: Float,
  val driftFrequency: Float,
  val driftAmplitude: Float,
  val opacity: Float
)

// Particle details for Balloons
class BalloonParticle(
  var x: Float,
  var y: Float,
  var initialX: Float,
  val speed: Float,
  val sizeWidth: Float, // in DP
  val sizeHeight: Float, // in DP
  val color: Color,
  val driftFrequency: Float,
  val driftAmount: Float,
  val stringLength: Float // in DP
)

fun createRandomSnowflake(startY: Float): SnowflakeParticle {
  val xVal = Random.nextFloat()
  return SnowflakeParticle(
    x = xVal,
    y = startY,
    initialX = xVal,
    speed = 0.12f + Random.nextFloat() * 0.15f,
    size = 14f + Random.nextFloat() * 12f, // medium-sized range (14dp to 26dp)
    rotationSpeed = -45f + Random.nextFloat() * 90f,
    rotation = Random.nextFloat() * 360f,
    driftFrequency = 1.0f + Random.nextFloat() * 2.0f,
    driftAmplitude = 0.02f + Random.nextFloat() * 0.03f,
    opacity = 0.4f + Random.nextFloat() * 0.5f
  )
}

fun createRandomBalloon(startY: Float): BalloonParticle {
  val xVal = Random.nextFloat()
  val balloonColors = listOf(
    Color(0xFFEF4444), // Crimson Red
    Color(0xFF3B82F6), // Executive Blue
    Color(0xFF10B981), // Emerald Green
    Color(0xFFF59E0B), // Corporate Amber Gold
    Color(0xFF8B5CF6), // Amethyst Purple
    Color(0xFFEC4899)  // Deep Rose Pink
  )
  return BalloonParticle(
    x = xVal,
    y = startY,
    initialX = xVal,
    speed = 0.10f + Random.nextFloat() * 0.12f,
    sizeWidth = 34f + Random.nextFloat() * 12f, // medium size width (34dp to 46dp)
    sizeHeight = 44f + Random.nextFloat() * 16f, // medium size height (44dp to 60dp)
    color = balloonColors[Random.nextInt(balloonColors.size)],
    driftFrequency = 0.8f + Random.nextFloat() * 1.5f,
    driftAmount = 0.03f + Random.nextFloat() * 0.04f,
    stringLength = 50f + Random.nextFloat() * 30f
  )
}

@Composable
fun FestiveEffectsDashboard() {
  // Timer values in seconds
  var snowflakeTimer by remember { mutableStateOf(0f) }
  var balloonTimer by remember { mutableStateOf(0f) }

  // Particle state lists
  var snowflakes by remember { mutableStateOf<List<SnowflakeParticle>>(emptyList()) }
  var balloons by remember { mutableStateOf<List<BalloonParticle>>(emptyList()) }

  // Global time elapsed indicator for physics drift
  var simulationTime by remember { mutableStateOf(0f) }

  // High-performance state updates synced to display frames
  LaunchedEffect(Unit) {
    var lastNanos = withFrameNanos { it }
    while (true) {
      val currentNanos = withFrameNanos { it }
      val deltaSeconds = ((currentNanos - lastNanos) / 1_000_000_000f).coerceAtMost(0.1f)
      lastNanos = currentNanos

      simulationTime += deltaSeconds

      // Decrement timers
      if (snowflakeTimer > 0f) {
        snowflakeTimer = (snowflakeTimer - deltaSeconds).coerceAtLeast(0f)
      }
      if (balloonTimer > 0f) {
        balloonTimer = (balloonTimer - deltaSeconds).coerceAtLeast(0f)
      }

      // Update snowflakes list
      val currentSnowflakes = snowflakes.toMutableList()
      val snowIterator = currentSnowflakes.iterator()
      while (snowIterator.hasNext()) {
        val s = snowIterator.next()
        s.y += s.speed * deltaSeconds
        s.rotation += s.rotationSpeed * deltaSeconds
        s.x = s.initialX + sin(simulationTime * s.driftFrequency) * s.driftAmplitude

        // If off bottom
        if (s.y > 1.1f) {
          if (snowflakeTimer > 0f) {
            // Loop back to top
            s.y = -0.1f
            s.x = Random.nextFloat()
            s.initialX = s.x
          } else {
            // Expired, let them die
            snowIterator.remove()
          }
        }
      }

      // Populate new snowflakes if active
      if (snowflakeTimer > 0f && currentSnowflakes.size < 40) {
        val toSpawn = 40 - currentSnowflakes.size
        for (i in 0 until toSpawn) {
          currentSnowflakes.add(createRandomSnowflake(startY = Random.nextFloat() * -0.5f))
        }
      }
      snowflakes = currentSnowflakes

      // Update balloons list
      val currentBalloons = balloons.toMutableList()
      val balloonIterator = currentBalloons.iterator()
      while (balloonIterator.hasNext()) {
        val b = balloonIterator.next()
        b.y -= b.speed * deltaSeconds
        b.x = b.initialX + sin(simulationTime * b.driftFrequency) * b.driftAmount

        // If off top
        if (b.y < -0.2f) {
          if (balloonTimer > 0f) {
            // Loop back to bottom
            b.y = 1.1f
            b.x = Random.nextFloat()
            b.initialX = b.x
          } else {
            // Expired, let them die
            balloonIterator.remove()
          }
        }
      }

      // Populate new balloons if active
      if (balloonTimer > 0f && currentBalloons.size < 18) {
        val toSpawn = 18 - currentBalloons.size
        for (i in 0 until toSpawn) {
          currentBalloons.add(createRandomBalloon(startY = 1.0f + Random.nextFloat() * 0.5f))
        }
      }
      balloons = currentBalloons
    }
  }

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    containerColor = Color.Transparent
  ) { innerPadding ->
    Box(modifier = Modifier.fillMaxSize()) {

      // Dynamics / Canvas Rendering Engine
      Canvas(modifier = Modifier.fillMaxSize()) {
        // Deep Obsidian / Obsidian Blue Backing
        drawRect(
          brush = Brush.verticalGradient(
            colors = listOf(
              Color(0xFF0F172A), // Slate 900
              Color(0xFF020617)  // Slate 950
            )
          )
        )

        // Draw Snowflakes
        snowflakes.forEach { s ->
          drawSnowflake(
            centerX = s.x * size.width,
            centerY = s.y * size.height,
            size = s.size.dp.toPx(),
            rotation = s.rotation,
            color = Color(0xFFA5F3FC), // Mint icy blue
            alpha = s.opacity
          )
        }

        // Draw Balloons
        balloons.forEach { b ->
          drawBalloon(
            centerX = b.x * size.width,
            centerY = b.y * size.height,
            width = b.sizeWidth.dp.toPx(),
            height = b.sizeHeight.dp.toPx(),
            color = b.color,
            stringLength = b.stringLength.dp.toPx(),
            driftOffset = simulationTime
          )
        }
      }

      // Main Executive Dashboard Controls
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(innerPadding)
          .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // Upper Block: Title and Status Display Node
        Column(
          modifier = Modifier.fillMaxWidth(),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Spacer(modifier = Modifier.height(16.dp))

          // Top Header Title
          Text(
            text = "FESTIVE EFFECTS STUDIO",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp,
            fontFamily = FontFamily.SansSerif,
            textAlign = TextAlign.Center
          )

          // Top Subtitle/System Deck Description
          Text(
            text = "EXECUTIVE CONTROLLER INTERFACE",
            color = Color(0xFF94A3B8),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 1.5.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(top = 4.dp),
            textAlign = TextAlign.Center
          )

          Spacer(modifier = Modifier.height(28.dp))

          // System Status Monitor Card
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .border(1.dp, Color(0xFF334155), RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(
              containerColor = Color(0xFF1E293B).copy(alpha = 0.85f)
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column {
                  Text(
                    text = "SIMULATION ENGINE",
                    color = Color(0xFF64748B),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp
                  )
                  val simulationActive = snowflakeTimer > 0f || balloonTimer > 0f
                  val engineStatus = if (simulationActive) "RUNNING" else "STANDBY"
                  val statusColor = if (simulationActive) Color(0xFF34D399) else Color(0xFF94A3B8)

                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                  ) {
                    Box(
                      modifier = Modifier
                        .size(8.dp)
                        .background(statusColor, RoundedCornerShape(50))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                      text = engineStatus,
                      color = Color.White,
                      fontSize = 15.sp,
                      fontWeight = FontWeight.Bold
                    )
                  }
                }

                Column(horizontalAlignment = Alignment.End) {
                  Text(
                    text = "ACTIVE PARTICLES",
                    color = Color(0xFF64748B),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp
                  )
                  Text(
                    text = "${snowflakes.size + balloons.size}",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(top = 2.dp)
                  )
                }
              }

              Spacer(modifier = Modifier.height(16.dp))
              // Styled light horizontal bar
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .height(1.dp)
                  .background(Color(0xFF334155))
              )
              Spacer(modifier = Modifier.height(16.dp))

              // Snowflakes system monitor
              val snowFormattedTime = "${(snowflakeTimer * 10).roundToInt() / 10f}s"
              Text(
                text = "SNOWFLAKE GENERATOR: $snowFormattedTime LEFT",
                color = if (snowflakeTimer > 0f) Color(0xFF38BDF8) else Color(0xFF64748B),
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
              )
              LinearProgressIndicator(
                progress = snowflakeTimer / 5f,
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(top = 6.dp)
                  .height(6.dp),
                color = Color(0xFF38BDF8),
                trackColor = Color(0xFF334155),
                strokeCap = StrokeCap.Round
              )

              Spacer(modifier = Modifier.height(16.dp))

              // Balloons system monitor
              val balloonFormattedTime = "${(balloonTimer * 10).roundToInt() / 10f}s"
              Text(
                text = "BALLOON GENERATOR: $balloonFormattedTime LEFT",
                color = if (balloonTimer > 0f) Color(0xFFFBBF24) else Color(0xFF64748B),
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
              )
              LinearProgressIndicator(
                progress = balloonTimer / 5f,
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(top = 6.dp)
                  .height(6.dp),
                color = Color(0xFFFBBF24),
                trackColor = Color(0xFF334155),
                strokeCap = StrokeCap.Round
              )
            }
          }
        }

        // Lower Block: Trigger Console with responsive action buttons
        Column(
          modifier = Modifier.fillMaxWidth(),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Text(
            text = "ACTIVATION CONSOLE",
            color = Color(0xFF64748B),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(bottom = 12.dp)
          )

          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
          ) {
            // Snowflakes Button
            Button(
              onClick = {
                snowflakeTimer = 5f
                // Instant top-to-middle seed wave for visual delight
                snowflakes = List(40) { createRandomSnowflake(startY = -0.4f + (it * 0.025f)) }
              },
              modifier = Modifier
                .weight(1f)
                .height(56.dp)
                .testTag("snowflakes_button"),
              colors = ButtonDefaults.buttonColors(
                containerColor = if (snowflakeTimer > 0f) Color(0xFF0284C7) else Color(0xFF1E293B),
                contentColor = Color.White
              ),
              border = borderStrokeForButton(snowflakeTimer > 0f, Color(0xFF38BDF8)),
              shape = RoundedCornerShape(12.dp),
              elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp, pressedElevation = 8.dp)
            ) {
              Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
              ) {
                SnowflakeIcon(color = if (snowflakeTimer > 0f) Color.White else Color(0xFF38BDF8))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "Snowflakes",
                  fontSize = 15.sp,
                  fontWeight = FontWeight.Bold,
                  letterSpacing = 0.5.sp
                )
              }
            }

            // Balloons Button
            Button(
              onClick = {
                balloonTimer = 5f
                // Instant bottom-to-middle seed wave for visual delight
                balloons = List(18) { createRandomBalloon(startY = 0.4f + (it * 0.04f)) }
              },
              modifier = Modifier
                .weight(1f)
                .height(56.dp)
                .testTag("balloons_button"),
              colors = ButtonDefaults.buttonColors(
                containerColor = if (balloonTimer > 0f) Color(0xFFD97706) else Color(0xFF1E293B),
                contentColor = Color.White
              ),
              border = borderStrokeForButton(balloonTimer > 0f, Color(0xFFFBBF24)),
              shape = RoundedCornerShape(12.dp),
              elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp, pressedElevation = 8.dp)
            ) {
              Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
              ) {
                BalloonIcon(color = if (balloonTimer > 0f) Color.White else Color(0xFFFBBF24))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "Balloons",
                  fontSize = 15.sp,
                  fontWeight = FontWeight.Bold,
                  letterSpacing = 0.5.sp
                )
              }
            }
          }

          Text(
            text = "EFFECTS DECK V1.0 • SECURE CONNECTIVITY",
            color = Color(0xFF475569),
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 8.dp)
          )
        }
      }
    }
  }
}

@Composable
fun borderStrokeForButton(isActive: Boolean, activeColor: Color): BorderStroke {
  return if (isActive) {
    BorderStroke(1.5.dp, Color.White.copy(alpha = 0.8f))
  } else {
    BorderStroke(1.dp, activeColor.copy(alpha = 0.35f))
  }
}

@Composable
fun SnowflakeIcon(color: Color) {
  Canvas(modifier = Modifier.size(20.dp)) {
    val centerX = size.width / 2f
    val centerY = size.height / 2f
    val radius = size.width * 0.45f
    val strokeWidth = 2f.dp.toPx()

    // Draw 6 branches
    for (i in 0 until 6) {
      val angleRad = (i * 60) * (Math.PI / 180f)
      val endX = centerX + radius * cos(angleRad).toFloat()
      val endY = centerY + radius * sin(angleRad).toFloat()

      drawLine(
        color = color,
        start = Offset(centerX, centerY),
        end = Offset(endX, endY),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round
      )

      // Mini branch spurs
      val spurDist = radius * 0.55f
      val spurStartX = centerX + spurDist * cos(angleRad).toFloat()
      val spurStartY = centerY + spurDist * sin(angleRad).toFloat()

      val spurLength = radius * 0.3f
      val leftAngle = angleRad + 45 * (Math.PI / 180f)
      val rightAngle = angleRad - 45 * (Math.PI / 180f)

      drawLine(
        color = color,
        start = Offset(spurStartX, spurStartY),
        end = Offset(
          spurStartX + spurLength * cos(leftAngle).toFloat(),
          spurStartY + spurLength * sin(leftAngle).toFloat()
        ),
        strokeWidth = strokeWidth * 0.8f,
        cap = StrokeCap.Round
      )
      drawLine(
        color = color,
        start = Offset(spurStartX, spurStartY),
        end = Offset(
          spurStartX + spurLength * cos(rightAngle).toFloat(),
          spurStartY + spurLength * sin(rightAngle).toFloat()
        ),
        strokeWidth = strokeWidth * 0.8f,
        cap = StrokeCap.Round
      )
    }
  }
}

@Composable
fun BalloonIcon(color: Color) {
  Canvas(modifier = Modifier.size(20.dp)) {
    val centerX = size.width / 2f
    val centerY = size.height * 0.42f
    val width = size.width * 0.55f
    val height = size.height * 0.65f
    val stringLen = size.height * 0.35f

    val knotY = centerY + height / 2f
    val endY = knotY + stringLen

    // String
    drawLine(
      color = color.copy(alpha = 0.5f),
      start = Offset(centerX, knotY),
      end = Offset(centerX, endY),
      strokeWidth = 1.5f.dp.toPx()
    )

    // Knot
    val knotPath = Path().apply {
      moveTo(centerX, knotY)
      lineTo(centerX - 3f, knotY + 4f)
      lineTo(centerX + 3f, knotY + 4f)
      close()
    }
    drawPath(path = knotPath, color = color)

    // Balloon body oval
    drawOval(
      color = color,
      topLeft = Offset(centerX - width / 2f, centerY - height / 2f),
      size = Size(width, height)
    )
  }
}

// Custom Snowflake drawing logic inside the 2D canvas space
fun DrawScope.drawSnowflake(
  centerX: Float,
  centerY: Float,
  size: Float,
  rotation: Float,
  color: Color,
  alpha: Float
) {
  val radius = size / 2f
  val strokeWidth = 2f.dp.toPx()
  val branchColor = color.copy(alpha = alpha)

  for (i in 0 until 6) {
    val angleRad = (i * 60 + rotation) * (Math.PI / 180f)
    val endX = centerX + radius * cos(angleRad).toFloat()
    val endY = centerY + radius * sin(angleRad).toFloat()

    drawLine(
      color = branchColor,
      start = Offset(centerX, centerY),
      end = Offset(endX, endY),
      strokeWidth = strokeWidth,
      cap = StrokeCap.Round
    )

    // Beautiful corporate-quality geometric spurs
    val spurDist = radius * 0.6f
    val spurStartX = centerX + spurDist * cos(angleRad).toFloat()
    val spurStartY = centerY + spurDist * sin(angleRad).toFloat()

    val spurLength = radius * 0.25f
    val leftSpurAngleRad = angleRad + 45f * (Math.PI / 180f)
    val rightSpurAngleRad = angleRad - 45f * (Math.PI / 180f)

    drawLine(
      color = branchColor,
      start = Offset(spurStartX, spurStartY),
      end = Offset(
        spurStartX + spurLength * cos(leftSpurAngleRad).toFloat(),
        spurStartY + spurLength * sin(leftSpurAngleRad).toFloat()
      ),
      strokeWidth = strokeWidth * 0.8f,
      cap = StrokeCap.Round
    )

    drawLine(
      color = branchColor,
      start = Offset(spurStartX, spurStartY),
      end = Offset(
        spurStartX + spurLength * cos(rightSpurAngleRad).toFloat(),
        spurStartY + spurLength * sin(rightSpurAngleRad).toFloat()
      ),
      strokeWidth = strokeWidth * 0.8f,
      cap = StrokeCap.Round
    )
  }
}

// Custom Balloon drawing logic with egg curves, glossy highlights, knot & curved strings
fun DrawScope.drawBalloon(
  centerX: Float,
  centerY: Float,
  width: Float,
  height: Float,
  color: Color,
  stringLength: Float,
  driftOffset: Float
) {
  val knotY = centerY + height / 2f

  // 1. Bezier curved string using cubicTo to ensure a luxurious organic drift wiggle
  val stringPath = Path().apply {
    moveTo(centerX, knotY + 4f)
    val controlX1 = centerX + sin(driftOffset * 2f) * 15f
    val controlY1 = knotY + stringLength * 0.33f
    val controlX2 = centerX - sin(driftOffset * 2f) * 15f
    val controlY2 = knotY + stringLength * 0.66f
    val endX = centerX + sin(driftOffset) * 5f
    val endY = knotY + stringLength
    cubicTo(controlX1, controlY1, controlX2, controlY2, endX, endY)
  }
  drawPath(
    path = stringPath,
    color = Color.White.copy(alpha = 0.35f),
    style = Stroke(width = 1.5f.dp.toPx())
  )

  // 2. Small triangular plastic knot base
  val knotPath = Path().apply {
    moveTo(centerX, knotY)
    lineTo(centerX - 6f, knotY + 6f)
    lineTo(centerX + 6f, knotY + 6f)
    close()
  }
  drawPath(
    path = knotPath,
    color = color
  )

  // 3. Egg-shaped balloon envelope
  drawOval(
    color = color,
    topLeft = Offset(centerX - width / 2f, centerY - height / 2f),
    size = Size(width, height)
  )

  // 4. Gloss shine overlay reflection for spectacular premium touch
  drawOval(
    color = Color.White.copy(alpha = 0.22f),
    topLeft = Offset(centerX - width * 0.25f, centerY - height * 0.32f),
    size = Size(width * 0.22f, height * 0.3f)
  )
}

// Legacy Composable maintained to keep all existing unit and screenshot tests perfectly compilation-safe
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
  Box(
    modifier = modifier
      .fillMaxWidth()
      .padding(16.dp),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = "Festive Effects - Greeting: Hello $name!",
      color = Color.White,
      fontSize = 16.sp
    )
  }
}

@Preview(showBackground = true)
@Composable
fun FestivePreview() {
  MyApplicationTheme {
    FestiveEffectsDashboard()
  }
}

