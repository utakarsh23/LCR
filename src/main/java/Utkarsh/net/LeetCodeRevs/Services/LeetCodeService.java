package Utkarsh.net.LeetCodeRevs.Services;

import Utkarsh.net.LeetCodeRevs.DTO.LeetCodeProblem;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

//to fetch the questionData using GraphQL Query
@Service
public class LeetCodeService {

    @Autowired
    private final RestTemplate restTemplate;

    @Autowired
    private final ObjectMapper objectMapper;

    public LeetCodeService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    //using query to gather data from the provided link
    public LeetCodeProblem fetchProblemData(String fullUrl) {
        // Extract the titleSlug from the full URL
        String titleSlug = fullUrl.substring(fullUrl.lastIndexOf("/") + 1);

        // GraphQL URL remains the same
        String graphqlUrl = "https://leetcode.com/graphql";

        // GraphQL Query
        String query = """
        query selectProblem($titleSlug: String!) {
            question(titleSlug: $titleSlug) {
                questionId
                title
                content
                difficulty
                exampleTestcases
                topicTags {
                    name
                }
            }
        }
    """;

        // Request Body
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("query", query);
        requestBody.put("variables", Map.of("titleSlug", titleSlug));

        // Headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");

        // HTTP Request
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.exchange(graphqlUrl, HttpMethod.POST, requestEntity, String.class);

        try {
            JsonNode rootNode = objectMapper.readTree(response.getBody());
            JsonNode questionNode = rootNode.path("data").path("question");

            // Extract topic tags
            List<String> topics = new ArrayList<>();
            for (JsonNode tag : questionNode.path("topicTags")) {
                topics.add(tag.get("name").asText());
            }

            // Convert JSON to DTO
            LeetCodeProblem problem = new LeetCodeProblem();
            problem.setQuestionId(questionNode.get("questionId").asText());
            problem.setTitle(questionNode.get("title").asText());
            problem.setContent(questionNode.get("content").asText());
            problem.setDifficulty(questionNode.get("difficulty").asText());
            problem.setExampleTestcases(questionNode.get("exampleTestcases").asText());
            problem.setTopicTags(topics);
            problem.setUrl(fullUrl);  // Set the complete URL

            return problem;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse LeetCode response", e);
        }
    }
}