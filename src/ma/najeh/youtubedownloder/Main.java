package ma.najeh.youtubedownloder;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.*;
import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main extends Application {
    private TextArea showTextArea ;

    @Override
    public void start(Stage primaryStage) throws Exception{
        Pane root1 = new HBox(),root2 = new VBox();
        root1.setPadding(new Insets(5,0,0,5)); root2.setPadding(new Insets(5,5,5,5));
        showTextArea=new TextArea();
        TextField urlTextField=new TextField("https://www.youtube.com/watch?v=2vopEwz7bK8");
        urlTextField.setMinWidth(300);
        Button sendBtn=new Button("Send");
        sendBtn.setOnAction(event -> {
            String url=urlTextField.getText();
            String pattern = "v=[-_a-zA-Z0-9]+";
            Pattern r = Pattern.compile(pattern);
            Matcher m = r.matcher(url);
            if (m.find()){
                String id=m.group();
                String videoUrl ="http://youtube.com/get_video_info?video_id="+id.substring(2,id.length());
                CloseableHttpClient httpclient = HttpClients.createDefault();
                HttpGet httpGet = new HttpGet(videoUrl);
                try {
                    CloseableHttpResponse response1 = httpclient.execute(httpGet);
                    try {
                        if (response1.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
                            HttpEntity entity1 = response1.getEntity();
                            BufferedReader br = new BufferedReader(new InputStreamReader(entity1.getContent()));
                            StringBuilder sb = new StringBuilder();
                            String line;
                            while ((line = br.readLine()) != null) {
                                sb.append(line);
                            }
                            String [] streams=splitQuery(sb.toString()).get("url_encoded_fmt_stream_map").split(",");
                            for (int i=0;i<streams.length;i++){
                                Map<String , String> currentHashMapvideo= splitQuery(streams[i]);
                                if (currentHashMapvideo.get("quality").equals(Video.Quality.HD) && currentHashMapvideo.get("type").contains(Video.Format.MP4)){
                                    downloadVideo(currentHashMapvideo);
                                    break;
                                }

                            }

                        }
                    }finally {
                        response1.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else{
                showTextArea.setText("not a correct youtube url");
            }
        });
        root2.getChildren().addAll(urlTextField,sendBtn);
        root1.getChildren().addAll(root2,showTextArea);




        primaryStage.setTitle("Youtube Downloader");
        primaryStage.setScene(new Scene(root1, 700, 300));
        primaryStage.show();
    }

    private void downloadVideo(Map<String, String> currentHashMapvideo) {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(currentHashMapvideo.get("url"));
        try {
            CloseableHttpResponse response = httpclient.execute(httpGet);

            try {
                if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
                    InputStream inputStream=response.getEntity().getContent();
                    BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
                    // how to save a file at local computer

                }
            }finally {
                response.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Map<String, String> splitQuery(String url) throws UnsupportedEncodingException {
        Map<String, String> query_pairs = new LinkedHashMap<String, String>();
        String[] pairs = url.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            query_pairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"), URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
        }
        return query_pairs;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
