package com.test.paper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

public class Test {

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java -jar DestinationHashGenerator.jar <PRN Number> <Path to JSON File>");
            return;
        }

        String prnNumber = args[0].toLowerCase();
        String jsonFilePath = args[1];

        try {
            // Parse JSON and find destination value
            String destinationValue = findDestinationValue(jsonFilePath);
            if (destinationValue == null) {
                System.out.println("No 'destination' key found in the JSON file.");
                return;
            }

            // Generate random alphanumeric string
            String randomString = generateRandomString(8);

            // Concatenate PRN, destination, and random string
            String toHash = prnNumber + destinationValue + randomString;

            // Generate MD5 hash
            String hash = generateMD5Hash(toHash);

            // Output the result in the format <hash>;<random string>
            System.out.println(hash + ";" + randomString);

        } catch (IOException e) {
            System.out.println("Error reading the JSON file: " + e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Error generating MD5 hash: " + e.getMessage());
        }
    }

    private static String findDestinationValue(String jsonFilePath) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(new File(jsonFilePath));
        return traverseJson(rootNode);
    }

    private static String traverseJson(JsonNode node) {
        if (node.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                if (field.getKey().equals("destination")) {
                    return field.getValue().asText();
                }
                String result = traverseJson(field.getValue());
                if (result != null) return result;
            }
        } else if (node.isArray()) {
            for (JsonNode arrayItem : node) {
                String result = traverseJson(arrayItem);
                if (result != null) return result;
            }
        }
        return null;
    }

    private static String generateRandomString(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(characters.charAt(random.nextInt(characters.length())));
        }
        return sb.toString();
    }

    private static String generateMD5Hash(String input) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] digest = md.digest(input.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}

