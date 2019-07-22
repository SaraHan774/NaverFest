package com.gahee.rss_v2;

import android.util.Log;

import com.gahee.rss_v2.data.time.tags.Item;
import com.gahee.rss_v2.data.wwf.model.WWFArticle;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class URLReader {

    public static void main(String[] args) {

        String link = "https://www.nasa.gov/press-release/nasa-administrator-to-talk-moon-landing-anniversary-moon-to-mars-plans";
        String link2 = "http://www.nasa.gov/ames/nisv-podcast-live-the-science-of-heat-shields";
        String link3 = "https://movieweb.com/a-beautiful-day-in-the-neighborhood-movie-photos-tom-hanks/";
        String link4 = "https://movieweb.com/top-gun-2-trailer-maverick-comic-con/";
        String time = "http://feedproxy.google.com/~r/time/entertainment/~3/5LL1KF0pCTg/";
        String nasa = "https://www.nasa.gov/multimedia/podcasting/twan_index.html";

        String nasa2 = "https://www.nasa.gov/nasa-edge/0111-m113";
//        String reading = readFromUrl(link);
//        System.out.println(reading);
//        PrintWriter printWriter = null;
//        try {
//            printWriter = new PrintWriter("sample_nasa_html.txt");
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//        printWriter.println(reading);
        fetchingWithJsoup(nasa2);
    }

    static String readFromUrl(String link){ //network connection이 null을 반환함.
        HttpsURLConnection urlConnection  = null;
        BufferedReader reader = null;
        String data = null;

        try{

            URL request = new URL(link);
            urlConnection = (HttpsURLConnection)request.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            int resCode = urlConnection.getResponseCode();
            System.out.println("res code: " + resCode);
            if(resCode != HttpsURLConnection.HTTP_OK)
                return null;

            InputStream inputStream = urlConnection.getInputStream();
            reader = new BufferedReader(new InputStreamReader(inputStream));

            StringBuilder builder = new StringBuilder();
            //buffer 로 바꿔보기
            String line;
            while((line = reader.readLine()) != null){ //disconnection boilerplate 제거하기!!!
                builder.append(line);
                builder.append("\n");
                if(builder.length() == 0){
                    return null;
                }
            }
            data = builder.toString();
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            if(urlConnection != null){ //안하면 null pointer exception
                urlConnection.disconnect();
            }
            if(reader != null){
                try{
                    reader.close();
                }catch(IOException e){
                    e.printStackTrace();
                }
            }

        }
        return data;
    }

    private static String fetchingWithJsoup(String link){
        Connection connection = Jsoup.connect(link);
        connection.maxBodySize(0);
        Document document = null;
        try {
            document = connection.get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Elements content = document.getAllElements();

        for(Element e : content){
            Elements p = e.getElementsByTag("iframe");
            for(Element tag : p){
                System.out.println(p);
            }
        }
        return "";
    }


    //extracting youtube links from Time article
    public static void getYoutubeLinks(List<Item> items){
        for(Item item: items){
            Document document = Jsoup.parse(item.getContentEncoded());
            Elements links = document.select("iframe");
            for(Element element : links){
                Log.d("youtube links : ", " ||| " + element.attr("src"));
            }
        }
    }

//    public static void getImagesFromWWFArticle(List<com.gahee.rss_v2.data.wwf.tags.Item> items){
//        for(com.gahee.rss_v2.data.wwf.tags.Item item: items){
//            Document document = Jsoup.parse(item.getContentEncoded());
//            Elements links = document.select("img");
//            for(Element element : links){
//                Log.d("image links : ", element.attr("src") + "\n");
//                String link = element.attr("src");
//                if(link.contains("jpg")){
//                   extractedMediaLinks.add(link);
//                }
//            }
//            //set the media link to article object
//        }
//    }

    public static void setWWFArticleImage(WWFArticle wwfArticle, com.gahee.rss_v2.data.wwf.tags.Item item){
        List<String> mediaLinks = new ArrayList<>();
        Document document = Jsoup.parse(item.getContentEncoded());
        Elements links = document.select("img");
        for(Element element : links){
            Log.d("image links : ", element.attr("src") + "\n");
            String link = element.attr("src");
            if(link.contains("jpg")){
                mediaLinks.add(link);
            }
        }
        wwfArticle.setExtractedMediaLinks(mediaLinks);
    }
}