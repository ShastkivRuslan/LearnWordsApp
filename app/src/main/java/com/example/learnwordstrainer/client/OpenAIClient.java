package com.example.learnwordstrainer.client;

import com.example.learnwordstrainer.BuildConfig;
import com.example.learnwordstrainer.integration.ChatCompletionRequest;
import com.example.learnwordstrainer.integration.ChatCompletionResponse;
import com.example.learnwordstrainer.integration.OpenAIAPI;

import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class OpenAIClient {
    private static final String BASE_URL = "https://api.openai.com/";

    private final OpenAIAPI service;

    public OpenAIClient() {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        service = retrofit.create(OpenAIAPI.class);
    }

    public void fetchExamplesFromGPT(String input, Callback<ChatCompletionResponse> callback) {
        List<ChatCompletionRequest.Message> messages = new ArrayList<>();

        messages.add(new ChatCompletionRequest.Message(
                "system",
                "You are a language assistant. Respond only with JSON in this format: " +
                        "{translation: string, transcription: string, partOfSpeech: string, examples: [{sentence: string, translation: string}]}"
        ));

        messages.add(new ChatCompletionRequest.Message(
                "user",
                String.format(
                        "Word or phrase: \"%s\"\n" +
                                "Return its part of speech, IPA transcription, translation to Ukrainian, and 3 simple English sentences that use this word or phrase in context with their Ukrainian translations.\n" +
                                "Respond only with valid JSON without any explanation.",
                        input)
        ));

        ChatCompletionRequest request = new ChatCompletionRequest();
        request.setModel("gpt-4o");
        request.setMessages(messages);
        request.setMaxTokens(250);
        request.setResponseFormat(new ChatCompletionRequest.ResponseFormat("json_object"));

        String authHeader = "Bearer " + BuildConfig.API_KEY;

        service.getChatCompletion(authHeader, request).enqueue(callback);
    }

}