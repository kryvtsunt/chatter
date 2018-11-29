package edu.northeastern.ccs.im;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.*;
import java.io.IOException;

public class SlackMessage {
    public void slackMessage(String msg) throws IOException {
        String url = "https://hooks.slack.com/services/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";
        String payload="{\"username\": \"webhookbot\", \"text\": \""+ msg +"\", \"icon_emoji\": \":ghost:\"}";

        StringEntity entity = new StringEntity(payload,
                ContentType.APPLICATION_FORM_URLENCODED);

        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpPost request = new HttpPost(url);
        request.setEntity(entity);

        HttpResponse response = httpClient.execute(request);
        System.out.println(response.getStatusLine().getStatusCode());
    }
}