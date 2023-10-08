package com.minm.signlanguage

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.minm.signlanguage.composableactivities.Ar
import com.minm.signlanguage.ui.theme.SignLanguageTheme



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SignLanguageTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SignLanguageApp()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun SignLanguageApp() {
    val databaseUrl = LocalContext.current.getString(R.string.firebase_database_url)
    val database = FirebaseDatabase.getInstance(databaseUrl)

    var textInputState by remember { mutableStateOf(TextFieldValue()) }
    val databaseReference = database.getReference("messages")
    val keyboardController = LocalSoftwareKeyboardController.current
    val context = LocalContext.current


    Column {
        TextField(
            value = textInputState,
            onValueChange = { textInputState = it },
            label = { Text("Type something here...") },
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    keyboardController?.hide()
                    databaseReference.push().setValue("Kotlin: ${textInputState.text}")
                    textInputState = TextFieldValue("")
                }
            )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(onClick = {
                databaseReference.push().setValue("Kotlin: ${textInputState.text}")
                textInputState = TextFieldValue("")
            }) {
                Text("Send")
            }
            Button(onClick = {
                // To clear the chat
                databaseReference.setValue(null)
            }) {
                Text("Clear Chat")
            }
        }
        Button(onClick = {
            val intent = Intent(context, Ar::class.java)
            context.startActivity(intent)
        }) {
            Text("Open AR Activity")
        }




        ChatMessages(databaseReference)
    }
}

@Composable
fun ChatMessages(databaseReference: DatabaseReference) {
    var chatMessages by remember { mutableStateOf(listOf<String>()) }

    // Realtime updates of chat messages
    DisposableEffect(databaseReference) {
        val chatListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messages = snapshot.children.mapNotNull { it.getValue(String::class.java) }
                chatMessages = messages
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error if needed
            }
        }

        databaseReference.addValueEventListener(chatListener)

        onDispose {
            databaseReference.removeEventListener(chatListener)
        }
    }

    LazyColumn {
        itemsIndexed(chatMessages) { index, message ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                horizontalArrangement = if (message.startsWith("Kotlin: ")) Arrangement.Start else Arrangement.End
            ) {
                Card {
                    Surface(
                        color = if (message.startsWith("Kotlin: ")) Color.Blue.copy(alpha = 0.2f) else Color.Red.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = message.removePrefix("Kotlin: ").removePrefix("React: "),
                            color = if (message.startsWith("Kotlin: ")) Color.Blue else Color.Red,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SignLanguageAppPreview() {
    SignLanguageTheme {
        SignLanguageApp()
    }
}
