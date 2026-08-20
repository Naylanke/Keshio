package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BudgetStatus
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusGreenBg
import com.example.ui.theme.StatusGreenBorder
import com.example.ui.theme.StatusOrange
import com.example.ui.theme.StatusOrangeBg
import com.example.ui.theme.StatusOrangeBorder
import com.example.ui.theme.StatusRed
import com.example.ui.theme.StatusRedBg
import com.example.ui.theme.StatusRedBorder

@Composable
fun SpendingStatusBadge(
    status: BudgetStatus,
    modifier: Modifier = Modifier
) {
    val (statusColor, statusBg, statusBorder) = when (status) {
        BudgetStatus.SAFE -> Triple(StatusGreen, StatusGreenBg, StatusGreenBorder)
        BudgetStatus.GETTING_CLOSE -> Triple(StatusOrange, StatusOrangeBg, StatusOrangeBorder)
        BudgetStatus.OVER_BUDGET -> Triple(StatusRed, StatusRedBg, StatusRedBorder)
    }

    Row(
        modifier = modifier
            .testTag("spending_status_badge")
            .clip(RoundedCornerShape(20.dp))
            .background(statusBg)
            .border(1.dp, statusBorder, RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(statusColor)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "${status.emoji} ${status.label}",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp
            ),
            color = statusColor
        )
    }
}
