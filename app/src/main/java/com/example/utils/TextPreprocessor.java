package com.example.utils;
public class TextPreprocessor {

    public static String cleanText(String input) {
        input = input.toLowerCase();
        input = input.replaceAll("[^a-z ]", "");
        input = input.trim();
        return input;
    }
}
