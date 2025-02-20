package Utkarsh.net.LeetCodeRevs.Services;

import io.github.cdimascio.dotenv.Dotenv;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

//keep it in check for the solutions submitted by the user, and also remember to remove the controller after it
@Service
public class GeminiService {

    static Dotenv dotenv = Dotenv.load();
    private static final String API_KEY = dotenv.get("GEMINI_API_KEY");
    private static final String GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=" + API_KEY;

    public String askGemini(String userInput) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            // Creating JSON request body
            JSONObject requestBody = new JSONObject();
            requestBody.put("contents", new JSONObject[]{
                    new JSONObject().put("parts", new JSONObject[]{
                            new JSONObject().put("text",
                                    "1. Check if the following code input is logically correct and matches the solution to the question; don`t overanalyze it. Only focus on the logic of the code; ignore formatting, error handling, or any other test cases."+
                                    "2. If the code is logically incorrect, return 'Incorrect Solution' and do not provide any explanation; don`t return the corrected code or anyhting else."+
                                    "3. If the code is logically correct, return ' Correct Solution' and do nothing more"+
                                    "User Input: " + userInput
                            )
                    })
            });

            // Setting headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Creating HTTP request
            HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);

            // Sending POST request
            ResponseEntity<String> response = restTemplate.postForEntity(GEMINI_URL, request, String.class);

            // Parsing the JSON response
            JSONObject jsonResponse = new JSONObject(response.getBody());

            return jsonResponse.toString(); // Extract and format response as needed

        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}