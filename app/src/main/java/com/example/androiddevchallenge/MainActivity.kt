/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.androiddevchallenge

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.androiddevchallenge.ui.theme.MyTheme
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.util.Timer
import java.util.TimerTask

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyTheme {
                MyApp()
            }
        }
    }
}

// Start building your app here!
@Composable
fun MyApp() {
    val counterState = rememberSaveable { mutableStateOf<Int?>(null) }
    val (counter, setCounter) = counterState
    val scope = rememberCoroutineScope()
    val tickState = remember { mutableStateOf<Job?>(null) }
    val (tickJob, setTickJob) = tickState

    Surface(color = MaterialTheme.colors.background) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            counter?.let { c ->
                Crossfade(targetState = c) {
                    Text(
                        it.toString(),
                        style = MaterialTheme.typography.h5,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
            } ?: Text(
                "Not started",
                style = MaterialTheme.typography.h5,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Button(
                onClick = {
                    if (tickJob != null) {
                        tickJob.cancel()
                        setTickJob(null)
                        return@Button
                    }
                    if (counter == null || counter == 0) {
                        setCounter(10)
                    }
                    setTickJob(
                        ticker(1000)
                            .onEach {
                                val current = counterState.component1()
                                current?.let { c ->
                                    setCounter(c - 1)
                                    if (c - 1 == 0) {
                                        val tj = tickState.component1()
                                        tj?.cancel()
                                        setTickJob(null)
                                    }
                                }
                            }
                            .launchIn(scope)
                    )
                }
            ) {
                Text(tickJob?.let { "Stop countdown" } ?: "Start countdown")
            }
        }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
fun ticker(intervalMs: Long) = callbackFlow {
    val timer = Timer()
    timer.scheduleAtFixedRate(
        timerTask {
            runCatching {
                offer(Unit)
            }
        },
        intervalMs, intervalMs
    )

    awaitClose { timer.cancel() }
}

inline fun timerTask(crossinline task: () -> Unit) = object : TimerTask() {
    override fun run() {
        task()
    }
}

@Preview("Light Theme", widthDp = 360, heightDp = 640)
@Composable
fun LightPreview() {
    MyTheme {
        MyApp()
    }
}

@Preview("Dark Theme", widthDp = 360, heightDp = 640)
@Composable
fun DarkPreview() {
    MyTheme(darkTheme = true) {
        MyApp()
    }
}
