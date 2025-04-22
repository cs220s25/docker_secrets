package edu.moravian.secrets;

public class Main {
    public static void main(String[] args) {
        try {
            String secretName = "220_Discord_Token";
            String secretKey = "DISCORD_TOKEN";

            Secrets secrets = new Secrets();

            String secret = secrets.getSecret(secretName, secretKey);
            System.out.println(secret);
        }
        catch(SecretsException e)
        {
            System.out.println(e.getMessage());
        }
    }

}

