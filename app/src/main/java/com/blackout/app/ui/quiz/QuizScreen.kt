package com.blackout.app.ui.quiz

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.blackout.app.R
import com.blackout.app.domain.model.Answer
import com.blackout.app.domain.model.AnswerOption
import com.blackout.app.domain.model.Question
import com.core.designsystem.components.AnimatedLinearProgressIndicator

@Composable
fun QuizRoute(onFinished: () -> Unit, onClose: () -> Unit, vm: QuizViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    LaunchedEffect(state.isFinished) { if (state.isFinished) onFinished() }
    QuizScreen(state, vm::onAnswer, vm::onNext, vm::onBack, onClose)
}

@Composable
fun QuizScreen(
    state: QuizUIState,
    onAnswer: (Answer) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit,
    onClose: () -> Unit,
) {
    BackHandler(enabled = state.currentIndex > 0, onBack = onBack)

    Scaffold(
        topBar = {
            Column {
                AnimatedLinearProgressIndicator(
                    progress = { state.progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding(),
                )
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (state.currentIndex > 0)
                        IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = onClose) { Icon(Icons.Filled.Close, null) }
                }
            }
        },
    ) { padding ->
        AnimatedContent(
            targetState = state.currentIndex,
            transitionSpec = {
                val fwd = targetState > initialState
                (slideInHorizontally { if (fwd) it else -it } + fadeIn()) togetherWith
                        (slideOutHorizontally { if (fwd) -it else it } + fadeOut())
            },
            modifier = Modifier
                .padding(padding)
                .imePadding(),
            label = "question",
        ) { index ->
            val q = state.questions.getOrNull(index) ?: return@AnimatedContent
            when (q) {
                is Question.Choice -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = stringResource(q.text),
                            style = MaterialTheme.typography.headlineSmall,
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        ChoiceOptions(
                            question = q,
                            selected = (state.answers[q.id] as? Answer.Choice)?.value,
                            onPick = { opt ->
                                onAnswer(Answer.Choice(opt.value, opt.points))
                                onNext()
                            },
                        )
                    }
                }

                is Question.FreeText -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = stringResource(q.text),
                            style = MaterialTheme.typography.headlineSmall,
                        )
                        TextAnswer(
                            text = (state.answers[q.id] as? Answer.Text)?.text.orEmpty(),
                            onChange = { onAnswer(Answer.Text(it)) },
                            onNext = onNext,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChoiceOptions(
    question: Question.Choice,
    selected: Int?,
    onPick: (AnswerOption) -> Unit
) {
    val iconOptions = question.options.all { it.icon != null }
    if (iconOptions) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            question.options.forEach { opt ->
                FilledIconToggleButton(
                    checked = opt.value == selected,
                    onCheckedChange = { onPick(opt) }
                ) {
                    Icon(opt.icon!!, null, Modifier.size(32.dp))
                }
            }
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            question.options.forEach { opt ->
                val label = stringResource(opt.label!!)
                if (opt.value == selected)
                    Button({ onPick(opt) }, Modifier.fillMaxWidth()) { Text(label) }
                else
                    OutlinedButton({ onPick(opt) }, Modifier.fillMaxWidth()) { Text(label) }
            }
        }
    }
}

@Composable
private fun TextAnswer(
    text: String,
    onChange: (String) -> Unit,
    onNext: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = onChange,
            modifier = Modifier.fillMaxWidth(),
            minLines = 4,
            maxLines = 8,
            placeholder = { Text("Type your answer here...") },
            shape = RoundedCornerShape(16.dp)
        )
        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(stringResource(R.string.btn_next))
        }
    }
}
