package Utkarsh.net.LeetCodeRevs.Services;

import Utkarsh.net.LeetCodeRevs.DTO.LeetCodeProblem;
import Utkarsh.net.LeetCodeRevs.DTO.LeetCodeResponse;
import Utkarsh.net.LeetCodeRevs.Entity.Submission;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

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


    //to returning the titles and fetching from the API
    @Cacheable(value = "leetcodeTitles", key = "#username")
    public List<String> getQuestionTitles(String username) {
        System.out.println("Entered");
        String url = "https://alfa-leetcode-api.onrender.com/" + username + "/acSubmission?limit=20";

        LeetCodeResponse response = restTemplate.getForObject(url, LeetCodeResponse.class);

        if (response == null || response.getSubmission() == null) {
            return Collections.emptyList();
        }

        System.out.println("Question Titles");
        return response.getSubmission()
                .stream()
                .map(Submission::getTitle)
                .collect(Collectors.toList());
    }

    //to returning the link and fetching from the API
    @CachePut(value = "leetcodeLinks", key = "#titleSlug")
    public String fetchLeetcodeLink(String titleSlug) {
        titleSlug = titleSlug.trim().toLowerCase().replaceAll("\\s+", "-");
        String url = "https://alfa-leetcode-api.onrender.com/select?titleSlug=" + titleSlug;

        System.out.println(url + " " + "hahahhahhaha");

        LeetCodeResponse response = restTemplate.getForObject(url, LeetCodeResponse.class);
        return response != null ? response.getLink() : null; // Just the link, as requested
    }

    @Cacheable(value = "leetcodeTotalSubs", key = "#username")
    public List<Map<String, Object>> getRecentSubmissionTitleTimestampStatus(String username) {
        String url = "https://alfa-leetcode-api.onrender.com/" + username + "/submission";

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        List<Map<String, Object>> result = new ArrayList<>();

        try {
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode submissions = root.path("submission");

            if (submissions.isArray()) {
                for (JsonNode node : submissions) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("title", node.path("title").asText());
                    map.put("timestamp", node.path("timestamp").asText());
                    map.put("statusDisplay", node.path("statusDisplay").asText());
                    result.add(map);
                }
            }
        } catch (Exception e) {
            System.out.println("Failed");
            throw new RuntimeException("Failed to parse submissions", e);
        }

        return result;
    }
}