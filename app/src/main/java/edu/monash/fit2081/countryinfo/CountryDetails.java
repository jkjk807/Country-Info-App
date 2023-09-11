package edu.monash.fit2081.countryinfo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.core.app.NavUtils;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import android.text.TextUtils;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.JsonReader;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.net.ssl.HttpsURLConnection;

public class CountryDetails extends AppCompatActivity {


    static TextView name;
    private TextView capital;
    private TextView code;
    private TextView population;
    private TextView area;
    private TextView currencies;
    private TextView languages;
    private TextView region;
    private ImageView logoView;
    private Button button;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_country_details);

        getSupportActionBar().setTitle(R.string.title_activity_country_details);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final String selectedCountry = getIntent().getStringExtra("country");

        name = findViewById(R.id.country_name);
        capital = findViewById(R.id.capital);
        code = findViewById(R.id.country_code);
        population = findViewById(R.id.population);
        area = findViewById(R.id.area);
        currencies=findViewById(R.id.currencies);
        languages=findViewById(R.id.languages);
        region= findViewById(R.id.region);

        logoView = findViewById(R.id.imageView);

        button=findViewById(R.id.button);

        button.setText("WIKI "+selectedCountry.toUpperCase());



        ExecutorService executor = Executors.newSingleThreadExecutor();
        //Executor handler = ContextCompat.getMainExecutor(this);
        Handler uiHandler=new Handler(Looper.getMainLooper());
        ExecutorService executor2 = Executors.newSingleThreadExecutor();
        //Executor handler = ContextCompat.getMainExecutor(this);
        Handler uiHandler2=new Handler(Looper.getMainLooper());



        executor.execute(() -> {
            //Background work here
            CountryInfo countryInfo = new CountryInfo();

            try {
                // Create URL
                URL webServiceEndPoint = new URL("https://restcountries.com/v2/name/" + selectedCountry); //

                // Create connection
                HttpsURLConnection myConnection = (HttpsURLConnection) webServiceEndPoint.openConnection();

                if (myConnection.getResponseCode() == 200) {
                    //JSON data has arrived successfully, now we need to open a stream to it and get a reader
                    InputStream responseBody = myConnection.getInputStream();
                    InputStreamReader responseBodyReader = new InputStreamReader(responseBody, "UTF-8");

                    //now use a JSON parser to decode data
                    JsonReader jsonReader = new JsonReader(responseBodyReader);
                    jsonReader.beginArray(); //consume arrays's opening JSON brace
                    String keyName;
                    // countryInfo = new CountryInfo(); //nested class (see below) to carry Country Data around in
                    boolean countryFound = false;
                    while (jsonReader.hasNext() && !countryFound) { //process array of objects
                        jsonReader.beginObject(); //consume object's opening JSON brace
                        while (jsonReader.hasNext()) {// process key/value pairs inside the current object
                            keyName = jsonReader.nextName();
                            if (keyName.equals("name")) {
                                countryInfo.setName(jsonReader.nextString());
                                if (countryInfo.getName().equalsIgnoreCase(selectedCountry)) {
                                    countryFound = true;
                                }
                            } else if (keyName.equals("alpha3Code")) {
                                countryInfo.setAlpha3Code(jsonReader.nextString());
                            } else if (keyName.equals("capital")) {
                                countryInfo.setCapital(jsonReader.nextString());
                            } else if (keyName.equals("population")) {
                                countryInfo.setPopulation(jsonReader.nextInt());
                            } else if (keyName.equals("area")) {
                                countryInfo.setArea(jsonReader.nextDouble());
                            } else if (keyName.equals("currencies")) {
                                jsonReader.beginArray();
                                while (jsonReader.hasNext()) {
                                    jsonReader.beginObject();
                                    while(jsonReader.hasNext()) {
                                        keyName = jsonReader.nextName();
                                        if (keyName.equals("name")) {
                                            countryInfo.CurrencyArray.add(jsonReader.nextString());
                                            //countryInfo.addCurrencies(jsonReader.nextString());
                                        } else {
                                            jsonReader.skipValue();
                                        }
                                    }
                                    jsonReader.endObject();
                                }
                                jsonReader.endArray();


                            }
                            else if (keyName.equals("languages")) {
                                jsonReader.beginArray();
                                while (jsonReader.hasNext()) {
                                    jsonReader.beginObject();
                                    while(jsonReader.hasNext()) {
                                        keyName = jsonReader.nextName();
                                        if (keyName.equals("name")) {
                                            countryInfo.LanguageArray.add(jsonReader.nextString());
                                            //countryInfo.addLanguages(jsonReader.nextString());
                                        } else {
                                            jsonReader.skipValue();
                                        }
                                    }
                                    jsonReader.endObject();
                                }
                                jsonReader.endArray();

                            } else if (keyName.equals("region")) {
                                countryInfo.setRegion(jsonReader.nextString());
                            } else {
                                jsonReader.skipValue();
                            }
                        }
                        jsonReader.endObject();
                    }
                    //jsonReader.endArray();
                    uiHandler.post(()->{
                        name.setText(countryInfo.getName());
                        capital.setText(countryInfo.getCapital());
                        code.setText(countryInfo.getAlpha3Code());
                        population.setText(Integer.toString(countryInfo.getPopulation()));
                        area.setText(Double.toString(countryInfo.getArea()));
                        currencies.setText(TextUtils.join(", ",countryInfo.CurrencyArray));
                        //currencies.setText(countryInfo.getCurrencies());
                        languages.setText(TextUtils.join(", ",countryInfo.LanguageArray));
                        //languages.setText(countryInfo.getLanguages());
                        region.setText(countryInfo.getRegion());
                    });


                } else {
                    Log.i("INFO", "Error:  No response");
                }

                // All your networking logic should be here
            } catch (Exception e) {
                Log.i("INFO", "Error " + e.toString());
            }

        });
        executor2.execute(() -> {
            String theDomain = "";

            //Background work here
            CountryInfo countryInfo = new CountryInfo();

            try {
                // Create URL
                URL webServiceEndPoint = new URL("https://restcountries.com/v2/name/" + selectedCountry); //

                // Create connection
                HttpsURLConnection myConnection = (HttpsURLConnection) webServiceEndPoint.openConnection();

                if (myConnection.getResponseCode() == 200) {
                    //JSON data has arrived successfully, now we need to open a stream to it and get a reader
                    InputStream responseBody = myConnection.getInputStream();
                    InputStreamReader responseBodyReader = new InputStreamReader(responseBody, "UTF-8");

                    //now use a JSON parser to decode data
                    JsonReader jsonReader = new JsonReader(responseBodyReader);
                    jsonReader.beginArray(); //consume arrays's opening JSON brace
                    String keyName;
                    // countryInfo = new CountryInfo(); //nested class (see below) to carry Country Data around in
                    boolean countryFound = false;
                    while (jsonReader.hasNext() && !countryFound) { //process array of objects
                        jsonReader.beginObject(); //consume object's opening JSON brace
                        while (jsonReader.hasNext()) {// process key/value pairs inside the current object
                            keyName = jsonReader.nextName();
                            if (keyName.equals("name")) {
                                countryInfo.setName(jsonReader.nextString());
                                if (countryInfo.getName().equalsIgnoreCase(selectedCountry)) {
                                    countryFound = true;
                                }
                            } else if (keyName.equals("alpha2Code")) {
                                theDomain= jsonReader.nextString();

                            } else {
                                jsonReader.skipValue();
                            }
                        }
                        jsonReader.endObject();
                    }
                    jsonReader.endArray();

                    String request = "https://flagcdn.com/144x108/" + theDomain.toLowerCase()+".png";

                    java.net.URL url = new java.net.URL(request);
                    HttpURLConnection connection = (HttpURLConnection) url
                            .openConnection();
                    connection.setDoInput(true);
                    connection.connect();
                    InputStream input = connection.getInputStream();

                    Bitmap myBitmap = BitmapFactory.decodeStream(input);
                    uiHandler2.post(()->{
                        logoView.setImageBitmap(myBitmap);

                    });


                } else {
                    Log.i("INFO", "Error:  No response");
                }

                // All your networking logic should be here
            } catch (Exception e) {
                Log.i("INFO", "Error " + e.toString());
            }

        });



    }
    public List<String> readStringsArray(JsonReader jsonreader) throws IOException {
        List<String> strings = new ArrayList<String>();

        jsonreader.beginArray();
        while (jsonreader.hasNext()) {
            strings.add(jsonreader.nextString());
        }
        jsonreader.endArray();
        return strings;
    }
    public void goToWiki(View v)
    {
        String country=getIntent().getStringExtra("country");
        Intent intent= new Intent(this,WebWiki.class);
        intent.putExtra("country", country);
        startActivity(intent);
    }



    public class CountryInfo {
        public String name;
        private String alpha3Code;
        private String capital;
        private int population;
        private double area;
        private String currencies="";
        private String languages="";
        private String region;
        private List<String> CurrencyArray = new ArrayList<String>();
        private List<String> LanguageArray = new ArrayList<String>();


        public List<String> getCurrencyArray() {
            return CurrencyArray;
        }

        public void setCurrencyArray(List<String> currencyArray) {
            CurrencyArray = currencyArray;
        }



        public String getCurrencies() {
            return currencies;
        }

        public void setCurrencies(String currencies) {
            this.currencies = currencies;
        }
        public void addCurrencies(String currencies){
            this.currencies+=currencies+",";
        }

        public void addLanguages(String languages){
            this.languages+=languages+",";
        }

        public String getLanguages() {
            return languages;
        }

        public void setLanguages(String languages) {
            this.languages = languages;
        }

        public String getRegion() {
            return region;
        }

        public void setRegion(String region) {
            this.region = region;
        }


        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getAlpha3Code() {
            return alpha3Code;
        }

        public void setAlpha3Code(String alpha3Code) {
            this.alpha3Code = alpha3Code;
        }

        public String getCapital() {
            return capital;
        }

        public void setCapital(String capital) {
            this.capital = capital;
        }

        public int getPopulation() {
            return population;
        }

        public void setPopulation(int population) {
            this.population = population;
        }

        public double getArea() {
            return area;
        }

        public void setArea(double area) {
            this.area = area;
        }
    }
}
