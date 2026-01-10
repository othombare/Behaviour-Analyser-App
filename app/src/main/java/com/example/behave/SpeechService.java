package com.example.behave;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class SpeechService extends Service {

    private SpeechRecognizer speechRecognizer;
    private Intent recognizerIntent;
    private PrefManager pref;
    private final Handler handler = new Handler();
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private boolean isListening = false;

    public static final String ACTION_SPEECH_BROADCAST = "com.example.behave.SPEECH_BROADCAST";
    public static final String EXTRA_SPEECH_TEXT = "extra_speech_text";

    private final RecognitionListener recognitionListener = new RecognitionListener() {
        @Override
        public void onReadyForSpeech(Bundle params) {
            isListening = true;
            Log.d("SpeechRecognizer", "onReadyForSpeech");
        }

        @Override
        public void onBeginningOfSpeech() {
            Log.d("SpeechRecognizer", "onBeginningOfSpeech");
        }

        @Override
        public void onEndOfSpeech() {
            isListening = false;
            Log.d("SpeechRecognizer", "onEndOfSpeech");
        }

        @Override
        public void onResults(Bundle results) {
            ArrayList<String> texts = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            if (texts != null && !texts.isEmpty()) {
                String detectedText = texts.get(0);
                broadcastSpeechText(detectedText);
                saveSpeechToFirestore(detectedText);
            }
            startListening(); // Continue listening
        }

        @Override
        public void onError(int error) {
            isListening = false;
            // Only restart on specific, non-fatal errors
            if (error == SpeechRecognizer.ERROR_NO_MATCH || error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT) {
                startListening();
            } else {
                logError(error);
            }
        }

        @Override
        public void onPartialResults(Bundle partialResults) {
            // Optional: handle partial results if needed
        }
        
        @Override public void onRmsChanged(float rmsdB) {}
        @Override public void onBufferReceived(byte[] buffer) {}
        @Override public void onEvent(int eventType, Bundle params) {}
    };

    @Override
    public void onCreate() {
        super.onCreate();
        pref = new PrefManager(this);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        createNotificationChannel();
        startForeground(1, getNotification("Detection Initializing..."));

        setupAuthStateListener();
        mAuth.addAuthStateListener(authStateListener);
    }

    private void setupAuthStateListener() {
        authStateListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null) {
                initializeRecognizer();
            } else {
                stopSelf();
            }
        };
    }

    private void initializeRecognizer() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            Log.e("SpeechRecognizer", "Recognition not available.");
            stopSelf();
            return;
        }
        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(recognitionListener);
        startListening();
    }

    private void startListening() {
        if (pref.isDetectionActive() && !isListening) {
            speechRecognizer.startListening(recognizerIntent);
        }
    }

    private void stopListening() {
        if (speechRecognizer != null) {
            speechRecognizer.stopListening();
            isListening = false;
        }
    }

    private void saveSpeechToFirestore(String text) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            return;
        }
        String userId = currentUser.getUid();
        Map<String, Object> speechData = new HashMap<>();
        speechData.put("text", text);
        speechData.put("time", com.google.firebase.Timestamp.now());

        db.collection("users").document(userId).collection("speech_logs")
                .add(speechData)
                .addOnSuccessListener(documentReference -> Log.d("Firestore", "Speech saved."))
                .addOnFailureListener(e -> Log.e("Firestore", "Error saving speech", e));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
        if (mAuth != null && authStateListener != null) {
            mAuth.removeAuthStateListener(authStateListener);
        }
    }

    // Other methods: broadcastSpeechText, getNotification, createNotificationChannel, onBind

    private void broadcastSpeechText(String text) {
        Intent intent = new Intent(ACTION_SPEECH_BROADCAST);
        intent.putExtra(EXTRA_SPEECH_TEXT, text);
        sendBroadcast(intent);
    }

    private Notification getNotification(String content) {
        return new NotificationCompat.Builder(this, "speech_channel")
                .setContentTitle("BEHAVE")
                .setContentText(content)
                .setSmallIcon(android.R.drawable.ic_btn_speak_now)
                .setOngoing(true)
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("speech_channel", "Speech Detection", NotificationManager.IMPORTANCE_LOW);
            if (getSystemService(NotificationManager.class) != null) {
                getSystemService(NotificationManager.class).createNotificationChannel(channel);
            }
        }
    }
    
    private void logError(int error) {
        String errorMessage;
        switch (error) {
            case SpeechRecognizer.ERROR_AUDIO:
                errorMessage = "Audio recording error";
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                errorMessage = "Client side error";
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                errorMessage = "Insufficient permissions";
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                errorMessage = "Network error";
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                errorMessage = "Network timeout";
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                errorMessage = "Recognizer busy";
                break;
            case SpeechRecognizer.ERROR_SERVER:
                errorMessage = "Error from server";
                break;
            default:
                errorMessage = "An unknown error occurred.";
                break;
        }
        Log.e("SpeechRecognizer", "Fatal Error: " + errorMessage);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
