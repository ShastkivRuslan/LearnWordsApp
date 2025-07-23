package com.example.learnwordstrainer.ui.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview // <-- ВАЖЛИВИЙ ІМПОРТ
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.learnwordstrainer.R
import com.example.learnwordstrainer.ui.compose.components.AddWordCard
import com.example.learnwordstrainer.ui.compose.components.AllWordsCard
import com.example.learnwordstrainer.ui.compose.components.PracticeCard
import com.example.learnwordstrainer.ui.compose.components.RepetitionCard
import com.example.learnwordstrainer.ui.compose.components.*
import com.example.learnwordstrainer.viewmodels.MainViewModel

@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onSettingsClick: () -> Unit,
    onAddWordClick: () -> Unit,
    onRepetitionClick: () -> Unit,
    onAllWordsClick: () -> Unit,
    onPracticeClick: () -> Unit
) {
    val totalWords by viewModel.totalWordsCount.observeAsState(0)
    val learnedPercentage by viewModel.learnedPercentage.observeAsState(0)

    MainScreenLayout(
        totalWords = totalWords,
        learnedPercentage = learnedPercentage,
        onSettingsClick = onSettingsClick,
        onAddWordClick = onAddWordClick,
        onRepetitionClick = onRepetitionClick,
        onAllWordsClick = onAllWordsClick,
        onPracticeClick = onPracticeClick
    )
}

@Composable
fun MainScreenLayout(
    totalWords: Int,
    learnedPercentage: Int,
    onSettingsClick: () -> Unit,
    onAddWordClick: () -> Unit,
    onRepetitionClick: () -> Unit,
    onAllWordsClick: () -> Unit,
    onPracticeClick: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.background)
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                ) {
                    Header()
                    StatisticsCard(totalWords, learnedPercentage)
                    ActionSection(
                        title = stringResource(R.string.head_text),
                        onAddWordClick = onAddWordClick,
                        onRepetitionClick = onRepetitionClick,
                        onAllWordsClick = onAllWordsClick
                    )
                    PracticeSection(
                        title = "Практика",
                        onPracticeClick = onPracticeClick
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = onSettingsClick,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ) {
            Icon(Icons.Filled.Settings, contentDescription = stringResource(R.string.change_theme))
        }
    }
}

@Preview(
    name = "Головний екран",
    showBackground = true,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun MainScreenPreview() {
    MainScreenLayout(
        totalWords = 120,
        learnedPercentage = 45,
        onSettingsClick = {},
        onAddWordClick = {},
        onRepetitionClick = {},
        onAllWordsClick = {},
        onPracticeClick = {}
    )
}


@Composable
fun Header() {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = stringResource(R.string.app_name), fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = stringResource(R.string.description_head_text), fontSize = 16.sp, color = Color.White.copy(alpha = 0.7f))
    }
}

@Composable
fun StatisticsCard(totalWords: Int, learnedPercentage: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = MaterialTheme.shapes.large
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatItem(label = stringResource(R.string.count_words), value = totalWords.toString(), modifier = Modifier.weight(1f))
            StatItem(label = stringResource(R.string.learned), value = stringResource(R.string.percentage_format, learnedPercentage), modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun StatItem(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = label, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ActionSection(
    title: String,
    onAddWordClick: () -> Unit,
    onRepetitionClick: () -> Unit,
    onAllWordsClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = title, fontSize = 22.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.width(16.dp))
            Image(painter = painterResource(id = R.drawable.ic_right_chevron), contentDescription = null)
        }
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            AddWordCard(onClick = onAddWordClick, modifier = Modifier.padding(end = 16.dp))
            RepetitionCard(onClick = onRepetitionClick, modifier = Modifier.padding(end = 16.dp))
            AllWordsCard(onClick = onAllWordsClick)
        }
    }
}

@Composable
fun PracticeSection(title: String, onPracticeClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = title, fontSize = 22.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.width(16.dp))
            Image(painter = painterResource(id = R.drawable.ic_right_chevron), contentDescription = null)
        }
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            PracticeCard(onClick = onPracticeClick)
        }
    }
}