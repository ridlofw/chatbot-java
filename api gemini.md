token : AIzaSyDaEFUGBQp37woH46loG9v-aKqqH0Ha9lE


contoh penggunaan gemini API dijava:
package com.example;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;

public class GenerateTextFromTextInput {
  public static void main(String[] args) {
    Client client = new Client();

    GenerateContentResponse response =
        client.models.generateContent(
            "gemini-3.1-flash-lite",
            "Explain how AI works in a few words",
            null);

    System.out.println(response.text());
  }
}