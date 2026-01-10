package com.example.behave;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;

public class SpeechActivity extends AppCompatActivity {

    private static final String TAG = "SpeechActivity";
    private SpeechRecognizer speechRecognizer;
    private Button speechButton;
    private TextView welcomeText;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speech);

        getWindow().setStatusBarColor(
            ContextCompat.getColor(this, R.color.toolbar_blue)
        );

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        speechButton = findViewById(R.id.speech_button);
        welcomeText = findViewById(R.id.welcome_text);
        Button viewReportsButton = findViewById(R.id.view_reports_button);

        setupSpeechRecognizer();

        speechButton.setOnClickListener(v -> startListening());
        viewReportsButton.setOnClickListener(v -> {
            Intent intent = new Intent(SpeechActivity.this, ReportListActivity.class);
            startActivity(intent);
        });
    }

    private void setupSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                speechButton.setText("Listening...");
            }

            @Override
            public void onBeginningOfSpeech() {}

            @Override
            public void onRmsChanged(float rmsdB) {}

            @Override
            public void onBufferReceived(byte[] buffer) {}

            @Override
            public void onEndOfSpeech() {
                speechButton.setText("Start Speech");
            }

            @Override
            public void onError(int error) {
                speechButton.setText("Start Speech");
                Toast.makeText(SpeechActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    String spokenText = matches.get(0);
                    saveSpeechLog(spokenText);
                }
            }

            @Override
            public void onPartialResults(Bundle partialResults) {}

            @Override
            public void onEvent(int eventType, Bundle params) {}
        });
    }

    private void startListening() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now...");
        speechRecognizer.startListening(intent);
    }

    private void saveSpeechLog(String text) {
        String userId = mAuth.getCurrentUser().getUid();
        Map<String, Object> log = Map.of("text", text, "timestamp", System.currentTimeMillis());
        db.collection("users").document(userId).collection("speech_logs").add(log)
                .addOnSuccessListener(documentReference -> Toast.makeText(SpeechActivity.this, "Speech saved!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(SpeechActivity.this, "Failed to save speech.", Toast.LENGTH_SHORT).show());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
    }
}
