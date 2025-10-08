// SplashScreen.kt
package com.visss.expencetracker.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import com.visss.expencetracker.R

@Composable
fun SplashScreen(onSplashComplete: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(3000) // 3 seconds delay to show the beautiful screen
        onSplashComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF667eea),
                        Color(0xFF764ba2)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Background decorative elements
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Animated floating circles in background
            FloatingCircles()
        }

        // Main content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Logo/Icon with shadow
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(
                        color = Color.White.copy(alpha = 0.2f),
                        shape = MaterialTheme.shapes.extraLarge
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground), //  app logo
                    contentDescription = "App Logo",
                    modifier = Modifier.size(80.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // App Name with gradient text
            Text(
                text = buildAnnotatedString {
                    withStyle(
                        style = SpanStyle(
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 36.sp
                        )
                    ) {
                        append("Expe")
                    }
                    withStyle(
                        style = SpanStyle(
                            color = Color(0xFFFFD700),
                            fontWeight = FontWeight.Bold,
                            fontSize = 36.sp
                        )
                    ) {
                        append("nsory")
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Tagline
            Text(
                text = "Manage Your Expenses Smartly",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Loading indicator
            LoadingDots()
        }

        // Footer with version/developer info
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 32.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Text(
                text = "Track • Save • Grow",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun FloatingCircles() {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Large circle top right
        Box(
            modifier = Modifier
                .size(200.dp)
                .offset(x = 150.dp, y = (-50).dp)
                .background(
                    color = Color.White.copy(alpha = 0.1f),
                    shape = MaterialTheme.shapes.extraLarge
                )
        )

        // Medium circle bottom left
        Box(
            modifier = Modifier
                .size(150.dp)
                .offset(x = (-50).dp, y = 400.dp)
                .background(
                    color = Color.White.copy(alpha = 0.08f),
                    shape = MaterialTheme.shapes.extraLarge
                )
        )

        // Small circle center left
        Box(
            modifier = Modifier
                .size(80.dp)
                .offset(x = (-20).dp, y = 200.dp)
                .background(
                    color = Color.White.copy(alpha = 0.12f),
                    shape = MaterialTheme.shapes.extraLarge
                )
        )
    }
}

@Composable
fun LoadingDots() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(3) { index ->
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(
                        color = Color.White.copy(alpha = 0.6f),
                        shape = MaterialTheme.shapes.small
                    )
            )
        }
    }
}

// Alternative simpler version if you prefer minimal design
@Composable
fun MinimalSplashScreen(onSplashComplete: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(2000)
        onSplashComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF667eea),
                        Color(0xFF764ba2)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Logo with container
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(
                        color = Color.White.copy(alpha = 0.2f),
                        shape = MaterialTheme.shapes.large
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = "App Logo",
                    modifier = Modifier.size(60.dp)
                )
            }

            // App name
            Text(
                text = "EasyTracker",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )

            // Tagline
            Text(
                text = "Your Personal Finance Manager",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}