package com.example.learnwordstrainer.ui.compose.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.learnwordstrainer.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    iconRes: Int,
    decorativeImageRes: Int,
    title: String,
    description: String
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .width(270.dp)
            .height(170.dp)
            .padding(vertical = 16.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = decorativeImageRes),
                contentDescription = null,
                alpha = 0.1f,
                modifier = Modifier
                    .size(150.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = 40.dp, y = 40.dp)
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Box(
                        modifier = Modifier.size(54.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = iconRes),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = description, fontSize = 14.sp, color = MaterialTheme.colorScheme.secondary)
            }
        }
    }
}

// Специфічні картки, що викликають універсальну
@Composable
fun AddWordCard(onClick: () -> Unit, modifier: Modifier = Modifier) {
    NavigationCard(
        onClick = onClick,
        modifier = modifier,
        iconRes = R.drawable.ic_add_new,
        decorativeImageRes = R.drawable.shape_plus_decor,
        title = stringResource(R.string.add_new_word_button),
        description = stringResource(R.string.add_card_description)
    )
}

@Composable
fun RepetitionCard(onClick: () -> Unit, modifier: Modifier = Modifier) {
    NavigationCard(
        onClick = onClick,
        modifier = modifier,
        iconRes = R.drawable.ic_repetition,
        decorativeImageRes = R.drawable.shape_repeat_decor,
        title = stringResource(R.string.repeat_mode),
        description = stringResource(R.string.repetition_card_description)
    )
}

@Composable
fun AllWordsCard(onClick: () -> Unit, modifier: Modifier = Modifier) {
    NavigationCard(
        onClick = onClick,
        modifier = modifier,
        iconRes = R.drawable.ic_all_words,
        decorativeImageRes = R.drawable.list_shape,
        title = stringResource(R.string.all_words_btn),
        description = stringResource(R.string.all_words_card_description)
    )
}

@Composable
fun PracticeCard(onClick: () -> Unit, modifier: Modifier = Modifier) {
    NavigationCard(
        onClick = onClick,
        modifier = modifier,
        iconRes = R.drawable.ic_practice,
        decorativeImageRes = R.drawable.shape_practice_decor,
        title = stringResource(R.string.practice_type),
        description = stringResource(R.string.practice_card_description)
    )
}