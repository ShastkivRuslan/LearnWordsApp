package com.example.learnwordstrainer.ui.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.airbnb.lottie.LottieAnimationView;
import com.example.learnwordstrainer.R;
import com.example.learnwordstrainer.databinding.ActivityPracticeBinding;
import com.example.learnwordstrainer.model.ExampleData;
import com.example.learnwordstrainer.utils.TypewriterEffect;
import com.example.learnwordstrainer.viewmodels.PracticeViewModel;
import com.google.android.material.card.MaterialCardView;

import java.util.List;
import java.util.Locale;

public class PracticeActivity extends AppCompatActivity {

    private static final long ANIMATION_DURATION = 300L;
    private static final float MAIN_CARD_SWIPE_DISTANCE = 300f;
    private static final float CARD_STACK_DEFAULT_SCALE = 1.0f;
    private static final float CARD_STACK_HIDDEN_SCALE = 0.85f;
    private static final float CARD_STACK_HIDDEN_ALPHA = 0.05f;

    private ActivityPracticeBinding binding;
    private PracticeViewModel viewModel;
    private TextToSpeech textToSpeech;
    private String currentWord = "";

    private LottieAnimationView typingAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPracticeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(PracticeViewModel.class);
        typingAnimation = binding.typingAnimation;

        initializeTextToSpeech();
        setupObservers();
        setupListeners();

        updateCurrentWord(viewModel.getNextWord());
    }

    private void setupObservers() {
        viewModel.getExamplesList().observe(this, this::updateExamplesWithAnimation);

        viewModel.getIsLoading().observe(this, isLoading -> {
            if (isLoading) {
                hideExampleContainers();
                showTypingAnimation();
            } else {
                hideTypingAnimation();
            }
        });

        viewModel.getErrorMessage().observe(this, errorMsg -> {
            if (errorMsg != null && !errorMsg.isEmpty()) {
                Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void hideExampleContainers() {
        binding.messageContainer1.setVisibility(View.GONE);
        binding.messageContainer2.setVisibility(View.GONE);
        binding.messageContainer3.setVisibility(View.GONE);
        binding.tvTranslation1.setText("");
        binding.tvTranslation2.setText("");
        binding.tvTranslation3.setText("");
    }

    private void showTypingAnimation() {
        typingAnimation.setVisibility(View.VISIBLE);
        typingAnimation.playAnimation();
    }

    private void hideTypingAnimation() {
        typingAnimation.setVisibility(View.GONE);
        typingAnimation.cancelAnimation();
    }

    private void setupListeners() {
        binding.btnListen.setOnClickListener(v -> {
            if (textToSpeech != null) {
                textToSpeech.speak(currentWord, TextToSpeech.QUEUE_FLUSH, null, null);
            }
        });

        binding.btnNextWord.setOnClickListener(v -> {
            // Отримуємо наступне слово з ViewModel
            String nextWord = viewModel.getNextWord();
            animateCardSwipe(nextWord);
        });
    }

    private void updateCurrentWord(String word) {
        if (word == null || word.isEmpty()) {
            Toast.makeText(this, "Немає доступних слів", Toast.LENGTH_SHORT).show();
            return;
        }

        currentWord = word;
        binding.tvWord.setText(word);
        // Запит до AI для отримання прикладів
        viewModel.getExamplesFromAI(word);
    }

    private void updateExamplesWithAnimation(List<ExampleData> examplesList) {
        if (examplesList == null || examplesList.isEmpty()) {
            Toast.makeText(this, "Не вдалося отримати приклади", Toast.LENGTH_SHORT).show();
            return;
        }

        // Забезпечуємо, що маємо принаймні 3 приклади для відображення
        while (examplesList.size() < 3) {
            examplesList.add(new ExampleData("", ""));
        }

        animateExample(
                binding.tvExample1,
                binding.tvTranslation1,
                examplesList.get(0),
                binding.messageContainer1,
                () -> animateExample(
                        binding.tvExample2,
                        binding.tvTranslation2,
                        examplesList.size() > 1 ? examplesList.get(1) : null,
                        binding.messageContainer2,
                        () -> animateExample(
                                binding.tvExample3,
                                binding.tvTranslation3,
                                examplesList.size() > 2 ? examplesList.get(2) : null,
                                binding.messageContainer3,
                                null
                        )
                )
        );
    }

    private void animateExample(
            TextView exampleTextView,
            TextView translationTextView,
            ExampleData exampleData,
            View container,
            Runnable onComplete
    ) {
        if (exampleTextView == null || translationTextView == null || container == null) {
            if (onComplete != null) onComplete.run();
            return;
        }

        if (exampleData == null || exampleData.getExampleText() == null || exampleData.getExampleText().isEmpty()) {
            container.setVisibility(View.GONE);
            if (onComplete != null) onComplete.run();
            return;
        }

        container.setVisibility(View.VISIBLE);

        // Анімація тексту прикладу
        TypewriterEffect.typeText(exampleTextView, exampleData.getExampleText(), 15, () -> {
            // Після завершення анімації прикладу анімуємо переклад
            String translation = exampleData.getTranslationText();
            if (translation != null && !translation.isEmpty()) {
                TypewriterEffect.typeText(translationTextView, translation, 15, onComplete);
            } else if (onComplete != null) {
                onComplete.run();
            }
        });
    }

    private void animateCardSwipe(String nextWord) {
        // Знаходимо картки
        MaterialCardView mainCard = findViewById(R.id.cardWord);
        MaterialCardView cardBehind1 = findViewById(R.id.cardStackBehind1);
        MaterialCardView cardBehind2 = findViewById(R.id.cardStackBehind2);
        MaterialCardView cardBehind3 = findViewById(R.id.cardStackBehind3);

        if (mainCard == null || cardBehind1 == null || cardBehind2 == null || cardBehind3 == null) return;

        // Початкові параметри
        float mainCardInitialY = mainCard.getTranslationY();
        float behind1InitialY = cardBehind1.getTranslationY();
        float behind2InitialY = cardBehind2.getTranslationY();
        float behind3InitialY = cardBehind3.getTranslationY();

        // Цільові параметри
        float mainCardTargetY = mainCardInitialY + MAIN_CARD_SWIPE_DISTANCE;
        float behind1TargetY = mainCardInitialY;
        float behind2TargetY = behind1InitialY;
        float behind3TargetY = behind2InitialY;

        // Зберігаємо початкові масштаби та альфа-прозорість
        float behind1InitialScale = cardBehind1.getScaleX();
        float behind2InitialScale = cardBehind2.getScaleX();
        float behind3InitialScale = cardBehind3.getScaleX();

        float behind1InitialAlpha = cardBehind1.getAlpha();
        float behind2InitialAlpha = cardBehind2.getAlpha();
        float behind3InitialAlpha = cardBehind3.getAlpha();

        // Цільові масштаби та альфа
        float behind1TargetScale = CARD_STACK_DEFAULT_SCALE;
        float behind2TargetScale = behind1InitialScale;
        float behind3TargetScale = behind2InitialScale;

        float behind1TargetAlpha = 1.0f;
        float behind2TargetAlpha = behind1InitialAlpha;
        float behind3TargetAlpha = behind2InitialAlpha;

        // Створюємо аніматори
        ValueAnimator mainCardAnimator = createValueAnimator(ANIMATION_DURATION, animation -> {
            float fraction = animation.getAnimatedFraction();
            mainCard.setTranslationY(lerp(mainCardInitialY, mainCardTargetY, fraction));
            mainCard.setAlpha(lerp(1.0f, 0.0f, fraction));
        });

        ValueAnimator behind1Animator = createValueAnimator(ANIMATION_DURATION, animation -> {
            float fraction = animation.getAnimatedFraction();
            cardBehind1.setTranslationY(lerp(behind1InitialY, behind1TargetY, fraction));
            float scale = lerp(behind1InitialScale, behind1TargetScale, fraction);
            cardBehind1.setScaleX(scale);
            cardBehind1.setScaleY(scale);
            cardBehind1.setAlpha(lerp(behind1InitialAlpha, behind1TargetAlpha, fraction));
        });

        ValueAnimator behind2Animator = createValueAnimator(ANIMATION_DURATION, animation -> {
            float fraction = animation.getAnimatedFraction();
            cardBehind2.setTranslationY(lerp(behind2InitialY, behind2TargetY, fraction));
            float scale = lerp(behind2InitialScale, behind2TargetScale, fraction);
            cardBehind2.setScaleX(scale);
            cardBehind2.setScaleY(scale);
            cardBehind2.setAlpha(lerp(behind2InitialAlpha, behind2TargetAlpha, fraction));
        });

        ValueAnimator behind3Animator = createValueAnimator(ANIMATION_DURATION, animation -> {
            float fraction = animation.getAnimatedFraction();
            cardBehind3.setTranslationY(lerp(behind3InitialY, behind3TargetY, fraction));
            float scale = lerp(behind3InitialScale, behind3TargetScale, fraction);
            cardBehind3.setScaleX(scale);
            cardBehind3.setScaleY(scale);
            cardBehind3.setAlpha(lerp(behind3InitialAlpha, behind3TargetAlpha, fraction));
        });

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(mainCardAnimator, behind1Animator, behind2Animator, behind3Animator);

        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // Після завершення анімації оновлюємо стан карток
                createNewCard();
                // Скидаємо стан верхньої картки
                mainCard.setAlpha(1.0f);
                mainCard.setTranslationY(mainCardInitialY);
                updateCurrentWord(nextWord);
                // Оновлюємо вміст верхньої картки
                hideExampleContainers();

            }
        });

        animatorSet.start();
    }

    private ValueAnimator createValueAnimator(long duration, ValueAnimator.AnimatorUpdateListener updateListener) {
        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(duration);
        animator.addUpdateListener(updateListener);
        return animator;
    }

    // Лінійна інтерполяція
    private float lerp(float start, float end, float fraction) {
        return start + (end - start) * fraction;
    }

    private void createNewCard() {
        MaterialCardView mainCard = findViewById(R.id.cardWord);
        MaterialCardView cardBehind1 = findViewById(R.id.cardStackBehind1);
        MaterialCardView cardBehind2 = findViewById(R.id.cardStackBehind2);
        MaterialCardView cardBehind3 = findViewById(R.id.cardStackBehind3);

        if (mainCard == null || cardBehind1 == null || cardBehind2 == null || cardBehind3 == null) return;

        // Запам'ятовуємо координати cardBehind3
        float behind3Y = cardBehind3.getTranslationY();
        float behind3Scale = cardBehind3.getScaleX();
        float behind3Alpha = cardBehind3.getAlpha();

        // Приховуємо верхню картку
        mainCard.setVisibility(View.INVISIBLE);

        // Зсув карток:
        //  mainCard <- cardBehind1
        //  cardBehind1 <- cardBehind2
        //  cardBehind2 <- cardBehind3
        //  cardBehind3 <- "нова" картка (початкові невидимі параметри)

        mainCard.setTranslationY(cardBehind1.getTranslationY());
        mainCard.setScaleX(CARD_STACK_DEFAULT_SCALE);
        mainCard.setScaleY(CARD_STACK_DEFAULT_SCALE);
        mainCard.setAlpha(1.0f);

        cardBehind1.setTranslationY(cardBehind2.getTranslationY());
        cardBehind1.setScaleX(cardBehind2.getScaleX());
        cardBehind1.setScaleY(cardBehind2.getScaleY());
        cardBehind1.setAlpha(cardBehind2.getAlpha());

        cardBehind2.setTranslationY(behind3Y);
        cardBehind2.setScaleX(behind3Scale);
        cardBehind2.setScaleY(behind3Scale);
        cardBehind2.setAlpha(behind3Alpha);

        // "Оновлюємо" cardBehind3, роблячи її фактично новою нижньою карткою
        cardBehind3.setTranslationY(behind3Y + 10);
        cardBehind3.setScaleX(CARD_STACK_HIDDEN_SCALE);
        cardBehind3.setScaleY(CARD_STACK_HIDDEN_SCALE);
        cardBehind3.setAlpha(CARD_STACK_HIDDEN_ALPHA);

        // Оновлюємо вміст для нової нижньої картки
        updateCardContent(cardBehind3);

        // Робимо верхню картку знову видимою
        mainCard.setVisibility(View.VISIBLE);
    }

    private void updateCardContent(MaterialCardView card) {
        if (card == null) return;

        TextView wordTextView = card.findViewById(R.id.tvWord);
        TextView transcriptionTextView = card.findViewById(R.id.tvTranscription);
        TextView partOfSpeechTextView = card.findViewById(R.id.tvPartOfSpeech);

        if (wordTextView == null || transcriptionTextView == null || partOfSpeechTextView == null) return;

        String newWord = viewModel.getNextWord();
        // В майбутньому потрібно отримувати ці дані з API або бази даних
        String newTranscription = "[nɛkst wɜːd]";
        String newPartOfSpeech = "noun";

        wordTextView.setText(newWord);
        transcriptionTextView.setText(newTranscription);
        partOfSpeechTextView.setText(newPartOfSpeech);
    }

    private void initializeTextToSpeech() {
        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.setLanguage(Locale.ENGLISH);
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
        binding = null;
        typingAnimation = null;
    }

    public void doSomething(String zasib){
        System.out.println("=Я аомила посуду ");
    }
}