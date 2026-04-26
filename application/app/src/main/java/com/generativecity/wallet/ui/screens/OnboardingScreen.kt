package com.generativecity.wallet.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.generativecity.wallet.data.remote.UserPreferencesDto

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun OnboardingScreen(
    onComplete: (UserPreferencesDto) -> Unit,
    isLoading: Boolean = false
) {
    var step by remember { mutableStateOf(1) }
    val totalSteps = 4

    // Step 1: Interests
    var interests by remember { mutableStateOf(setOf<String>()) }
    // Step 2: Budget & Time
    var budget by remember { mutableStateOf("MEDIUM") }
    var activeTimes by remember { mutableStateOf(setOf("AFTERNOON", "EVENING")) }
    // Step 3: Patterns & Habits
    var frequency by remember { mutableStateOf("WEEKLY") }
    var isSpontaneous by remember { mutableStateOf(true) }
    var locationHabits by remember { mutableStateOf(setOf("DOWNTOWN")) }
    // Step 4: Environment & Discovery
    var environment by remember { mutableStateOf("BOTH") }
    var discoveryMode by remember { mutableStateOf("POPULAR") }

    val interestOptions = listOf("Coffee", "Dining", "Fitness", "Tech", "Art", "Nightlife", "Wellness", "Shopping")
    val timeOptions = mapOf("MORNING" to "Morning", "AFTERNOON" to "Afternoon", "EVENING" to "Evening", "NIGHT" to "Night")
    val locationOptions = listOf("DOWNTOWN", "SUBURBS", "MALLS", "PARKS", "WATERFRONT")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFFF8FAFC), Color(0xFFF1F5F9))))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Spacer(modifier = Modifier.height(48.dp))
            
            // Progress Indicator
            LinearProgressIndicator(
                progress = step.toFloat() / totalSteps,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = Color(0xFF0052FF),
                trackColor = Color(0xFFE2E8F0)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Step $step of $totalSteps",
                style = MaterialTheme.typography.labelLarge,
                color = Color(0xFF64748B)
            )
            
            Spacer(modifier = Modifier.height(32.dp))

            Box(modifier = Modifier.weight(1f)) {
                AnimatedContent(
                    targetState = step,
                    transitionSpec = {
                        if (targetState > initialState) {
                            (slideInHorizontally { it } + fadeIn()) togetherWith (slideOutHorizontally { -it } + fadeOut())
                        } else {
                            (slideInHorizontally { -it } + fadeIn()) togetherWith (slideOutHorizontally { it } + fadeOut())
                        }.using(SizeTransform(clip = false))
                    },
                    label = "OnboardingStep"
                ) { currentStep ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        when (currentStep) {
                            1 -> InterestsStep(interests, onToggle = { 
                                interests = if (interests.contains(it)) interests - it else interests + it 
                            }, options = interestOptions)
                            2 -> BudgetAndTimeStep(
                                budget = budget,
                                onBudgetChange = { budget = it },
                                activeTimes = activeTimes,
                                onToggleTime = { 
                                    activeTimes = if (activeTimes.contains(it)) activeTimes - it else activeTimes + it 
                                },
                                timeOptions = timeOptions
                            )
                            3 -> PatternsStep(
                                frequency = frequency,
                                onFrequencyChange = { frequency = it },
                                isSpontaneous = isSpontaneous,
                                onSpontaneousChange = { isSpontaneous = it },
                                locationHabits = locationHabits,
                                onToggleLocation = {
                                    locationHabits = if (locationHabits.contains(it)) locationHabits - it else locationHabits + it
                                },
                                locationOptions = locationOptions
                            )
                            4 -> PreferenceStep(
                                environment = environment,
                                onEnvChange = { environment = it },
                                discovery = discoveryMode,
                                onDiscChange = { discoveryMode = it }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (step > 1) {
                    TextButton(onClick = { step-- }) {
                        Text("Back", color = Color(0xFF64748B))
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                Button(
                    onClick = {
                        if (step < totalSteps) {
                            step++
                        } else {
                            onComplete(
                                UserPreferencesDto(
                                    interests = interests.toList(),
                                    budget_range = budget,
                                    active_times = activeTimes.toList(),
                                    behavior_frequency = frequency,
                                    is_spontaneous = isSpontaneous,
                                    location_habits = locationHabits.toList(),
                                    environment_preference = environment,
                                    discovery_mode = discoveryMode
                                )
                            )
                        }
                    },
                    modifier = Modifier
                        .height(56.dp)
                        .width(if (step == totalSteps) 160.dp else 120.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0052FF)),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text(if (step == totalSteps) "Get Started" else "Next", fontWeight = FontWeight.Bold)
                        if (step < totalSteps) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Default.ArrowForward, null, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun InterestsStep(selected: Set<String>, onToggle: (String) -> Unit, options: List<String>) {
    Text("What are you interested in?", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
    Text("Select all that apply to help us tailor your offers.", color = Color(0xFF64748B))
    Spacer(modifier = Modifier.height(24.dp))
    
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        options.forEach { interest ->
            FilterChip(
                selected = selected.contains(interest),
                onClick = { onToggle(interest) },
                label = { Text(interest, modifier = Modifier.padding(8.dp)) },
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}

@Composable
fun BudgetAndTimeStep(
    budget: String,
    onBudgetChange: (String) -> Unit,
    activeTimes: Set<String>,
    onToggleTime: (String) -> Unit,
    timeOptions: Map<String, String>
) {
    Text("Budget & Timing", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
    Spacer(modifier = Modifier.height(24.dp))

    Text("Average daily budget for fun?", fontWeight = FontWeight.Bold)
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf("LOW" to "$", "MEDIUM" to "$$", "HIGH" to "$$$").forEach { (valStr, label) ->
            ChoiceCard(
                modifier = Modifier.weight(1f),
                label = label,
                isSelected = budget == valStr,
                onClick = { onBudgetChange(valStr) }
            )
        }
    }

    Spacer(modifier = Modifier.height(24.dp))
    Text("When are you usually out?", fontWeight = FontWeight.Bold)
    FlowRow(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        timeOptions.forEach { (key, label) ->
            FilterChip(
                selected = activeTimes.contains(key),
                onClick = { onToggleTime(key) },
                label = { Text(label) }
            )
        }
    }
}

@Composable
fun PatternsStep(
    frequency: String,
    onFrequencyChange: (String) -> Unit,
    isSpontaneous: Boolean,
    onSpontaneousChange: (Boolean) -> Unit,
    locationHabits: Set<String>,
    onToggleLocation: (String) -> Unit,
    locationOptions: List<String>
) {
    Text("Your Lifestyle", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
    Spacer(modifier = Modifier.height(24.dp))

    Text("How often do you go out?", fontWeight = FontWeight.Bold)
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf("DAILY" to "Daily", "WEEKLY" to "Weekly", "RARELY" to "Rarely").forEach { (valStr, label) ->
            ChoiceCard(
                modifier = Modifier.weight(1f),
                label = label,
                isSelected = frequency == valStr,
                onClick = { onFrequencyChange(valStr) }
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))
    Text("Are you spontaneous?", fontWeight = FontWeight.Bold)
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ChoiceCard(modifier = Modifier.weight(1f), label = "Yes", isSelected = isSpontaneous, onClick = { onSpontaneousChange(true) })
        ChoiceCard(modifier = Modifier.weight(1f), label = "No", isSelected = !isSpontaneous, onClick = { onSpontaneousChange(false) })
    }

    Spacer(modifier = Modifier.height(16.dp))
    Text("Favorite hangout zones?", fontWeight = FontWeight.Bold)
    FlowRow(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        locationOptions.forEach { loc ->
            FilterChip(selected = locationHabits.contains(loc), onClick = { onToggleLocation(loc) }, label = { Text(loc) })
        }
    }
}

@Composable
fun PreferenceStep(
    environment: String,
    onEnvChange: (String) -> Unit,
    discovery: String,
    onDiscChange: (String) -> Unit
) {
    Text("Final Touches", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
    Spacer(modifier = Modifier.height(24.dp))

    Text("Environment preference?", fontWeight = FontWeight.Bold)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf("INDOOR" to "Indoor", "OUTDOOR" to "Outdoor", "BOTH" to "Both").forEach { (valStr, label) ->
            ChoiceCard(modifier = Modifier.fillMaxWidth(), label = label, isSelected = environment == valStr, onClick = { onEnvChange(valStr) })
        }
    }

    Spacer(modifier = Modifier.height(24.dp))
    Text("Discovery mode?", fontWeight = FontWeight.Bold)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf("POPULAR" to "Popular", "HIDDEN_GEMS" to "Hidden Gems").forEach { (valStr, label) ->
            ChoiceCard(modifier = Modifier.weight(1f), label = label, isSelected = discovery == valStr, onClick = { onDiscChange(valStr) })
        }
    }
}

@Composable
fun ChoiceCard(
    modifier: Modifier = Modifier,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(56.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) Color(0xFF0052FF) else Color.White,
        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
        shadowElevation = if (isSelected) 4.dp else 0.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(label, color = if (isSelected) Color.White else Color(0xFF0F172A), fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        verticalArrangement = verticalArrangement
    ) {
        content()
    }
}
