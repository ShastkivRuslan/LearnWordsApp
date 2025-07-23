package com.example.learnwordstrainer.ui.activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.learnwordstrainer.R;
import com.example.learnwordstrainer.databinding.ActivityAddWordBinding;
import com.example.learnwordstrainer.viewmodels.AddWordViewModel;

import java.util.Objects;

public class AddWordActivity extends AppCompatActivity {

    private ActivityAddWordBinding binding; // View Binding for easier access to views
    private AddWordViewModel viewModel; // ViewModel to handle business logic

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize binding and set the content view
        binding = ActivityAddWordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize the ViewModel
        viewModel = new ViewModelProvider(this).get(AddWordViewModel.class);

        // Set up the ActionBar with a back button and title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Add New Word");
        }

        setupObservers(); // Observe LiveData changes
        setupListeners(); // Set up button listeners
    }

    // Set up LiveData observers
    private void setupObservers() {
        // Observe messages from ViewModel (like success or error)
        viewModel.getMessage().observe(this, message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                viewModel.clearMessage(); // Clear message after showing
            }
        });

        // Observe if a word was successfully added
        viewModel.getWordAdded().observe(this, added -> {
            if (added) {
                // Clear input fields
                binding.etEnglish.setText("");
                binding.etTranslation.setText("");
                clearFocus(); // Hide the keyboard
                viewModel.resetWordAdded(); // Reset the flag
            }
        });
    }

    // Set up listeners for UI elements
    private void setupListeners() {
        binding.btnAdd.setOnClickListener(view -> {
            // Get text from EditTexts, trim and lowercase
            String english = Objects.requireNonNull(binding.etEnglish.getText()).toString().trim().toLowerCase();
            String translation = Objects.requireNonNull(binding.etTranslation.getText()).toString().trim().toLowerCase();

            // Pass the words to ViewModel to add
            viewModel.addWord(english, translation);
        });
    }

    // Hide the keyboard and clear focus
    private void clearFocus() {
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            view.clearFocus();
        }
    }

    // Handle ActionBar back button click
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed(); // Navigate back
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Override finish() to add custom transition animation when closing
    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right); // Slide animation
    }
}