package com.generativecity.wallet.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.generativecity.wallet.data.local.RewardInventoryEntity
import com.generativecity.wallet.data.repository.RewardRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

data class RouletteUiState(
    val canSpin: Boolean = true,
    val secondsUntilNextSpin: Long = 0,
    val lastResult: String = "Spin to try your luck",
    val isSpinning: Boolean = false,
    val finalDegree: Float = 0f,
    val inventory: RewardInventoryEntity? = null
)

class RouletteViewModel(
    private val rewardRepository: RewardRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(RouletteUiState())
    val uiState: StateFlow<RouletteUiState> = _uiState.asStateFlow()

    private var lastSpinAt: Long = 0L

    fun observeInventory(userId: Int) {
        viewModelScope.launch {
            rewardRepository.observeInventory(userId).collect { inv ->
                _uiState.update { it.copy(inventory = inv) }
            }
        }
    }

    fun startTimer() {
        viewModelScope.launch {
            while (true) {
                val now = System.currentTimeMillis()
                val nextSpinTime = getNextAllowedSpinTime()
                val remaining = (nextSpinTime - now).coerceAtLeast(0)
                _uiState.update {
                    it.copy(
                        canSpin = remaining == 0L && !it.isSpinning,
                        secondsUntilNextSpin = remaining / 1000
                    )
                }
                delay(1_000)
            }
        }
    }

    private fun getNextAllowedSpinTime(): Long {
        if (lastSpinAt == 0L) return 0L
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = lastSpinAt
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        return calendar.timeInMillis
    }

    fun spin(userId: Int) {
        if (!_uiState.value.canSpin || _uiState.value.isSpinning) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSpinning = true) }
            
            // 50/50 win probability
            val isWin = kotlin.random.Random.nextBoolean()
            val rewardIndex = if (isWin) kotlin.random.Random.nextInt(4) else 4 // 4 is "Nothing"
            
            // Calculate degrees to land on the specific segment
            // Segments: 0: 2x Coin, 1: Coupon, 2: Freezer, 3: Time, 4: Nothing (spans half the wheel in 50/50 but let's keep it simple)
            // Let's divide wheel into 8: [2x, Nothing, Coupon, Nothing, Freezer, Nothing, Time, Nothing]
            // This makes it 50/50 visually and logically.
            val segments = listOf("2x Coin", "Nothing", "Coupon", "Nothing", "Freezer", "Nothing", "Time", "Nothing")
            val targetSegment = if (isWin) {
                listOf(0, 2, 4, 6).random()
            } else {
                listOf(1, 3, 5, 7).random()
            }
            
            val segmentDeg = 360f / 8f
            val baseRotation = 360f * 5 // 5 full spins
            val targetRotation = 360f - (targetSegment * segmentDeg) - (segmentDeg / 2) + baseRotation
            
            _uiState.update { it.copy(finalDegree = targetRotation) }
            
            delay(3000) // Match animation duration
            
            val result = segments[targetSegment]
            val finalResultText = if (result == "Nothing") {
                "Better luck tomorrow! Come back in 24h."
            } else {
                applyReward(userId, result)
            }

            lastSpinAt = System.currentTimeMillis()
            _uiState.update { 
                it.copy(
                    isSpinning = false, 
                    lastResult = finalResultText,
                    canSpin = false
                ) 
            }
        }
    }

    private suspend fun applyReward(userId: Int, rewardType: String): String {
        val inventory = rewardRepository.getInventory(userId) ?: RewardInventoryEntity(userId = userId)
        val updated = when (rewardType) {
            "2x Coin" -> inventory.copy(doubleOrNothingCount = inventory.doubleOrNothingCount + 1)
            "Coupon" -> inventory.copy(freeCouponCount = inventory.freeCouponCount + 1)
            "Time" -> inventory.copy(timeExtensionCount = inventory.timeExtensionCount + 1)
            "Freezer" -> inventory.copy(streakFreezerCount = inventory.streakFreezerCount + 1)
            else -> inventory
        }
        rewardRepository.updateInventory(updated)
        return "You won: $rewardType! It's added to your inventory."
    }
}
