package com.minm.signlanguage

// Import necessary dependencies
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
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
import com.google.firebase.database.*
import com.minm.signlanguage.composableactivities.Ar
import com.minm.signlanguage.ui.theme.SignLanguageTheme

// Main Activity class
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

// The primary Composable function for the SignLanguageApp
@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun SignLanguageApp() {
    // Setup Firebase database
    val databaseUrl = LocalContext.current.getString(R.string.firebase_database_url)
    val database = FirebaseDatabase.getInstance(databaseUrl)

    // State management for input text field
    var textInputState by remember { mutableStateOf(TextFieldValue()) }
    val databaseReference = database.getReference("messages")

    // For handling keyboard actions
    val keyboardController = LocalSoftwareKeyboardController.current
    val context = LocalContext.current

    Column {
        // Text field for user input
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

        // Buttons for sending messages and clearing chat
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
                databaseReference.setValue(null)  // Clears the chat
            }) {
                Text("Clear Chat")
            }
        }

        // Button to open the AR functionality
        Button(onClick = {
            val intent = Intent(context, Ar::class.java)
            context.startActivity(intent)
        }) {
            Text("Open AR Activity")
        }

        // Displays the chat messages
        ChatMessages(databaseReference)
    }
}

// Composable function to display chat messages
@Composable
fun ChatMessages(databaseReference: DatabaseReference) {
    var chatMessages by remember { mutableStateOf(listOf<String>()) }

    // Listening to realtime updates from Firebase
    DisposableEffect(databaseReference) {
        val chatListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messages = snapshot.children.mapNotNull { it.getValue(String::class.java) }
                chatMessages = messages
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle the error here if needed
            }
        }

        databaseReference.addValueEventListener(chatListener)

        onDispose {
            databaseReference.removeEventListener(chatListener)
        }
    }

    // Displaying chat messages in a LazyColumn
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

// Preview for the Composable in Android Studio
@Preview(showBackground = true)
@Composable
fun SignLanguageAppPreview() {
    SignLanguageTheme {
        SignLanguageApp()
    }
}
