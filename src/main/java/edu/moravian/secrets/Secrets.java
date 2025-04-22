package edu.moravian.secrets;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;
import software.amazon.awssdk.services.secretsmanager.model.SecretsManagerException;

public class Secrets {

    public String getSecret(String secretName, String secretKey) throws SecretsException
    {
        Region region = Region.of("us-east-1");

        SecretsManagerClient client = SecretsManagerClient.builder()
                .region(region)
                .build();

        GetSecretValueRequest getSecretValueRequest = GetSecretValueRequest.builder()
                .secretId(secretName)
                .build();

        GetSecretValueResponse getSecretValueResponse;

        try {
            getSecretValueResponse = client.getSecretValue(getSecretValueRequest);

            String secretEntry = getSecretValueResponse.secretString();

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(secretEntry);
            JsonNode jsonNode = root.get(secretKey);
            if(jsonNode == null)
                throw new SecretsException("Key not found in secret: " + secretKey);
            return jsonNode.asText();
        } catch (SecretsManagerException | JacksonException | SdkClientException e) {
            throw new SecretsException("Error when retrieving secret: " + e.getMessage());
        }
    }
}
