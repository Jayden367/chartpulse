package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.AmberCaution
import com.example.ui.theme.AmberCautionContainer
import com.example.ui.theme.BullionGold
import com.example.ui.theme.BullionGoldContainer
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.SlateTextPrimary
import com.example.ui.theme.SlateTextSecondary

@Composable
fun SampleAiNoticeBanner(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(BullionGoldContainer.copy(alpha = 0.55f))
            .border(1.dp, BullionGold.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Rounded.AutoAwesome,
            contentDescription = "Sample AI preview notice",
            tint = BullionGold,
            modifier = Modifier.size(20.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.sample_ai_notice_title),
                style = MaterialTheme.typography.titleSmall,
                color = BullionGold
            )
            Text(
                text = stringResource(R.string.sample_ai_notice_body),
                style = MaterialTheme.typography.bodySmall,
                color = SlateTextSecondary
            )
        }
    }
}

@Composable
fun MarketCautionBanner(
    statusTitle: String,
    statusDetail: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(AmberCautionContainer.copy(alpha = 0.7f))
            .border(1.dp, AmberCaution.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = Icons.Rounded.WarningAmber,
            contentDescription = "Market status caution",
            tint = AmberCaution,
            modifier = Modifier.padding(top = 2.dp).size(20.dp)
        )
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = statusTitle,
                style = MaterialTheme.typography.titleSmall,
                color = AmberCaution
            )
            Text(
                text = statusDetail,
                style = MaterialTheme.typography.bodySmall,
                color = SlateTextPrimary.copy(alpha = 0.88f)
            )
        }
    }
}
