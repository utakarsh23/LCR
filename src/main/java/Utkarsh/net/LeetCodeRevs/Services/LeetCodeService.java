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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Collectors;

//to fetch the questionData using GraphQL Query and api's which we have used
@Service
public class LeetCodeService {

    @Autowired
    private final RestTemplate restTemplate;

    @Autowired
    private final ObjectMapper objectMapper;


    public LeetCodeService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate; //restTemplates is used for making HTTP requests e.g., calling external APIs
        this.objectMapper = objectMapper; //objectMapper is used to convert between Java objects and JSON.
    }

    //using query to gather data from the provided link
    public LeetCodeProblem fetchProblemData(String fullUrl) {
        // Extract the titleSlug from the full URL
        //https://chatgpt.com/share/682e3f2a-27f4-800e-9226-c9b6973338e4 refer this for the explanation or graphQl thingy
        String titleSlug = fullUrl.substring(fullUrl.lastIndexOf("/") + 1); //LeetCode identifies questions by a “slug” in the URL (e.g., "two-sum").

        // GraphQL URL remains the same
        String graphqlUrl = "https://leetcode.com/graphql"; //graphQl url

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

        // Request Body to store it
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("query", query); //query -> stores the upper String
        requestBody.put("variables", Map.of("titleSlug", titleSlug)); //variable -> the question slug "two-sum"

        // Headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json"); //Setting the Content-Type header to tell the server that we are sending JSON.

        // HTTP Request
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers); //Wrapping the body and headers into a Spring HttpEntity object, ready to send.

        ResponseEntity<String> response = restTemplate.exchange(graphqlUrl, HttpMethod.POST, requestEntity, String.class);
        //this is where the network call is being made, .exchange((Sends the request to https://leetcode.com/graphql),Method: POST, Payload: GraphQL body, Returns : ResponseEntity<String> (raw JSON response))

        try {
            JsonNode rootNode = objectMapper.readTree(response.getBody()); //Use ObjectMapper to parse the JSON string into a JsonNode tree.
            JsonNode questionNode = rootNode.path("data").path("question"); //navigate down to: data → question (which contains the data we asked for).

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
        //sends get request to the url, and then     "LeetCodeResponse.class" this makes the coming response(maybe JSON) into the Java object(LeetCodeResponse)
        //RestTemplate can automatically convert JSON responses into Java objects if you specify the target class in methods like getForObject() or postForObject().

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
    @Cacheable(value = "leetcodeLinks", key = "#titleSlug")
    public String fetchLeetcodeLink(String titleSlug) {
        titleSlug = titleSlug.trim().toLowerCase().replaceAll("\\s+", "-");
        String url = "https://alfa-leetcode-api.onrender.com/select?titleSlug=" + titleSlug;

        System.out.println(url + " " + "hahahhahhaha");

        LeetCodeResponse response = restTemplate.getForObject(url, LeetCodeResponse.class);

        if (response != null) { //or now we are storing the cache to load later inside a txt file for practice n testing as the caches always gets deleted restarting
            updateCacheFile(titleSlug, response.getLink());
        }

        return response != null ? response.getLink() : null; // Just the link, as requested
    }

    //to get the total last 20 submission details including failed ones
    @Cacheable(value = "leetcodeTotalSubs", key = "#username")
    public List<Map<String, Object>> getRecentSubmissionTitleTimestampStatus(String username) {
        String url = "https://alfa-leetcode-api.onrender.com/" + username + "/submission";

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        //we are using getForEntity() in here which also gives us teh status codes, headers and all those stuffs,
        //we are not using it thou, both works the same, this one has some addi features nothing else, we can use it for more data from the request

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

    //updating the cache file(txt) with the caches to load even after restart, not mandatory but for testing purposes
    private void updateCacheFile(String titleSlug, String link) {
        Path path = Paths.get("cache_backup.txt");
        Map<String, String> cacheMap = new LinkedHashMap<>();

        try {
            // Step 1: Read existing entries (if file exists)
            if (Files.exists(path)) {
                List<String> lines = Files.readAllLines(path);
                for (String line : lines) {
                    String[] parts = line.split("=", 2);
                    if (parts.length == 2) {
                        cacheMap.put(parts[0].trim(), parts[1].trim());
                    }
                }
            }

            // Step 2: Update or add the new entry
            cacheMap.put(titleSlug, link);

            // Step 3: Write back the whole map (overwrite the file)
            List<String> updatedLines = cacheMap.entrySet().stream()
                    .map(entry -> entry.getKey() + "=" + entry.getValue())
                    .toList();
            Files.write(path, updatedLines, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        } catch (IOException e) {
            System.err.println("Failed to update cache file: " + e.getMessage());
        }
    }

}