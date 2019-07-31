package com.gahee.rss_v2.remoteSource.imageLabel;

import android.os.AsyncTask;
import android.util.Log;

import com.gahee.rss_v2.data.wwf.model.WWFArticle;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class ImageLabeling {

    private static final String TAG = "ImageLabeling";

    public static String sendREST(String serverUrl, String jsonPostString) throws IllegalStateException{
        String inputLine = null;
        StringBuffer stringBuffer = new StringBuffer();

        try{
            Log.d(TAG, "sendREST: START");

            URL url = new URL(serverUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept-Charset", "UTF-8");
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(15000);

            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(jsonPostString.getBytes("UTF-8"));
            outputStream.flush();

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), "UTF-8")
            );
            while((inputLine = reader.readLine()) != null){
                stringBuffer.append(inputLine);
            }
            connection.disconnect();
            Log.d(TAG, "sendREST: END");

        }catch (Exception e){
            Log.d(TAG, "sendREST: error " + e.getMessage());
            e.printStackTrace();
        }
        return stringBuffer.toString();
    }

    private static class ImageLabelAsync extends AsyncTask<Void, Void, String> {
        private String serverUrl;
        private String jsonPostString;
        private WWFArticle wwfArticle;

        public ImageLabelAsync(String serverUrl, String jsonPostString, WWFArticle wwfArticle) {
            this.serverUrl = serverUrl;
            this.jsonPostString = jsonPostString;
            this.wwfArticle = wwfArticle;
        }

        @Override
        protected String doInBackground(Void... voids) {
            return sendREST(serverUrl, jsonPostString);
        }

        @Override
        protected void onPostExecute(String responseFromServer) {
            new HandleResultsAsync(wwfArticle).execute(responseFromServer);
        }

    }

    public static AsyncTask<Void, Void, String> generateImageLabelsFromServer(String serverUrl, String jsonPostString, WWFArticle wwfArticle){
        ImageLabelAsync imageLabelAsync = new ImageLabelAsync(serverUrl, jsonPostString, wwfArticle);
        return imageLabelAsync.execute();
    }


    public static class HandleResultsAsync extends AsyncTask<String, Void, ArrayList<String>>{
        private WWFArticle wwfArticle;
        private ArrayList<String> listOfImageLabelResults = new ArrayList<>();

        public HandleResultsAsync(WWFArticle wwfArticle){
            this.wwfArticle = wwfArticle;
        }

        @Override
        protected ArrayList<String> doInBackground(String... strings) {

            JSONObject jsonObject = null;
            try {
                jsonObject = new JSONObject(strings[0]);

                JSONObject jsonObject1 = jsonObject.getJSONObject("results");

                while(jsonObject1.keys().hasNext()){
                    String currentDynamicKey = jsonObject1.keys().next();
                    JSONArray currentJsonArray =  jsonObject1.getJSONArray(currentDynamicKey);
                    for(int i = 0; i < currentJsonArray.length(); i++){
                        JSONObject jsonObject2 = (JSONObject) currentJsonArray.get(i);
                        Double score = jsonObject2.getDouble("score");
                        String description = jsonObject2.getString("description");
                        if(score > 0.8){
                            listOfImageLabelResults.add(description);
                        }
                    }
                }
                wwfArticle.setImageLabelResponse(listOfImageLabelResults);
            } catch (JSONException e) {
                e.printStackTrace();
            }


            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<String> stringArrayList) {
            wwfArticle.setImageLabelResponse(stringArrayList);
        }
     }
    }


