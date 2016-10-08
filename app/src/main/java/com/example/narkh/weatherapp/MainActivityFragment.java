package com.example.narkh.weatherapp;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    String day;
    int d;

    private ArrayAdapter<String> ForcastAdapter;

    public MainActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);


    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecastfragment, menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //Refresh Button
        if (id == R.id.action_refresh) {
            FetchWeatherTask task = new FetchWeatherTask();
            task.execute("Mumbai");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);


        String[] listArray = {
                "Today - Sunny ",
                "Tomarrow - Foggy",
                "Friday - Sunny",
                "Saturday - Rain",
                "Sunaday - Snow",
                "Monday - Asteroid",
                "Tuesday - Too Hot",
                "Wednessday - Rainy",
                "Thursday - Snow",
                "Friday - Sunny",
                "Saturday - Rain",
                "Sunaday - Snow",
                "Monday - Asteroid",
                "Tuesday - Too Hot",
                "Wednessday - Rainy",
                "Thursday - Snow",
                "Friday - Sunny",
                "Saturday - Rain",
                "Sunaday - Snow",
                "Monday - Asteroid",
                "Tuesday - Too Hot",
                "Wednessday - Rainy",
                "Thursday - Snow",
                "Friday - Sunny",
                "Saturday - Rain",
                "Sunaday - Snow",
                "Monday - Asteroid",
                "Tuesday - Too Hot",
                "Wednessday - Rainy",
                "Thursday - Snow"

        };
        List<String> WeekList = new ArrayList<String>(Arrays.asList(listArray));

        ForcastAdapter = new ArrayAdapter<String>(
                getActivity(),
                R.layout.list_item,
                R.id.list_item_textView,
                WeekList);

        ListView listView = (ListView) rootView.findViewById(R.id.list_View);
        listView.setAdapter(ForcastAdapter);

        //OnCLickListView
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String forecast = ForcastAdapter.getItem(position);

                // For Long Short Toast
                // Toast.makeText(getActivity(), forecast, Toast. LENGTH_SHORT).show();
                Intent intent = new Intent(getActivity(), DetailActivity.class).putExtra(Intent.EXTRA_TEXT, forecast);
                startActivity(intent);

                //Intent i =new Intent(getActivity(),TextOnMain.class);
                //startActivity(i);


            }
        });


        return rootView;
    }

    private void updateWeather() {
        FetchWeatherTask weatherTask = new FetchWeatherTask();
        String location = PreferenceManager.getDefaultSharedPreferences(getActivity())
                .getString(getString(R.string.l_key), getString(R.string.l_default));
        //*#*#*#*#*#*#**#*#*#*#*#*#*#*#*#*#*#*#*#*#**#CHANGE THIS IF USER WANNA GIVE ITS OWN LOCATION*#*#*#*##*#*#*#*#*#*#*#*#*#*#*#*#**#

        weatherTask.execute(location);
    }

    public void onStart() {
        super.onStart();
        updateWeather();
    }

    public class Utils {

        public void setListViewHeightBasedOnChildren(ListView listView) {
            ListAdapter listAdapter = listView.getAdapter();
            if (listAdapter == null) {
                // pre-condition
                return;
            }

            int totalHeight = 0;
            for (int i = 0; i < listAdapter.getCount(); i++) {
                View listItem = listAdapter.getView(i, null, listView);
                listItem.measure(0, 0);
                totalHeight += listItem.getMeasuredHeight();
            }

            ViewGroup.LayoutParams params = listView.getLayoutParams();
            params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
            listView.setLayoutParams(params);
            listView.requestLayout();
        }


    }

    public class FetchWeatherTask extends AsyncTask<String, Void, String[]> {
        private final String Log_tag = FetchWeatherTask.class.getSimpleName();
        int x = 1;


        public FetchWeatherTask() {
            super();
        }

        /* The date/time conversion code is going to be moved outside the asynctask later,
 * so for convenience we're breaking it out into its own method now.
 */
        private String getReadableDateString(long time) {
            // Because the API returns a unix timestamp (measured in seconds),
            // it must be converted to milliseconds in order to be converted to valid date.
            Date date = new Date(time * 1000);


            SimpleDateFormat format = new SimpleDateFormat("E, MMM d");
            return format.format(date).toString();
        }

        /**
         * Prepare the weather high/lows for presentation.
         */
       /* private String formatHighLows(double high, double low) {
            // For presentation, assume the user doesn't care about tenths of a degree.
            long roundedHigh = Math.round(high);
            long roundedLow = Math.round(low);

            String highLowStr = roundedHigh + "/" + roundedLow;
            return highLowStr;
        }*/

        /**
         * Take the String representing the complete forecast in JSON Format and
         * pull out the data we need to construct the Strings needed for the wireframes.
         * <p/>
         * Fortunately parsing is easy:  constructor takes the JSON string and converts it
         * into an Object hierarchy for us.
         */
        private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays)
                throws JSONException, UnsupportedOperationException, IOException {

            // These are the names of the JSON objects that need to be extracted.
            final String OWM_LIST = "list";
            final String OWM_WEATHER = "weather";
            final String OWM_TEMPERATURE = "temp";
            final String OWM_MAX = "temp_max";
            final String OWM_MIN = "temp_min";
            final String OWM_DATETIME = "dt";
            final String OWM_MAIN = "main";
            final String OWM_DESCRIPTION = "description";
            final String OWM_ICONID = "icon";


            JSONObject forecastJson = new JSONObject(forecastJsonStr);
            JSONArray list = forecastJson.getJSONArray(OWM_LIST);

            String[] resultStrs = new String[numDays];
            for (int i = 0; i < list.length(); i++) {
                // For now, using the format "Day, description, hi/low"
                int n = 0;

                String description;
                String highAndLow;
                String iconId;

                // Get the JSON object representing the day
                JSONObject c = list.getJSONObject(i);

                // The date/time is returned as a long.  We need to convert that
                // into something human-readable, since most people won't read "1400356800" as
                // "this saturday".
                JSONObject main = c.getJSONObject(OWM_MAIN);

                long dateTime = c.getLong(OWM_DATETIME);
                day = getReadableDateString(dateTime);
                String tp = main.getString(OWM_TEMPERATURE);
                Date date = new Date(dateTime * 1000);
                int h = date.getHours();
                int m = date.getMinutes();
                int s = date.getSeconds();
                d = date.getDate();


                // description is in a child array called "weather", which is 1 element long.
                JSONObject weatherObject = c.getJSONArray(OWM_WEATHER).getJSONObject(0);
                description = weatherObject.getString(OWM_DESCRIPTION);
                iconId = weatherObject.getString(OWM_ICONID);


                Log.v(Log_tag, "IconID : " + iconId + ".png");


                // Temperatures are in a child object called "temp".  Try not to name variables
                // "temp" when working with temperature.  It confuses everybody.

                String high = main.getString(OWM_MAX);
                String low = main.getString(OWM_MIN);
                String png = ".png";


                // highAndLow = formatHighLows(high, low);
                resultStrs[i] = " " + day + " -(" + h + ":" + m + ":" + s + ") - " + description + " - " + high + " / " + low;

                String img = "http://openweathermap.org/img/w/";

                Uri builtUri = Uri.parse(img).buildUpon()
                        .appendPath(iconId)
                        .build();

                HttpURLConnection con = (HttpURLConnection) (new URL(img + iconId + ".png")).openConnection();


                Log.v(Log_tag, "img : " + con);                                  // URL of ICON
                if (i == 0) {

                }

            }

            for (String s : resultStrs) {
                Log.v(Log_tag, "Forecast Entry : " + s);

            }


            return resultStrs;
        }


        @Override
        protected String[] doInBackground(String... params) {


            // These two need to be declared outside the try/catch
// so that they can be closed in the finally block.
            if (params.length == 0)

            {
                Log.v(Log_tag, "Forecast String :*************");
                return null;
            }


            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;


// Will contain the raw JSON response as a string.
            String forecastJsonStr = null;

            String format = "json";
            String units = "metric";
            int numDays = 30;
            String api = "e97c80b36eea355c51ab288c82dff58d";


            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are avaiable at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                // Log.v(Log_tag, "Forecast String :222222222222222");
                //URL url = new URL("http://api.openweathermap.org/data/2.5/forecast/city?id=1275339&APPID=e97c80b36eea355c51ab288c82dff58d");

                final String Forecast_Base = "http://api.openweathermap.org/data/2.5/forecast/city?";

                final String query = "q";
                final String formatP = "mode";
                final String APIkey = "appid";
                final String unitsP = "units";
                final String numDaysP = "cnt";


                Uri builtUri = Uri.parse(Forecast_Base).buildUpon()
                        .appendQueryParameter(query, params[0])
                        .appendQueryParameter(APIkey, api)
                        .appendQueryParameter(formatP, format)
                        .appendQueryParameter(unitsP, units)
                        .appendQueryParameter(numDaysP, Integer.toString(numDays))
                        .build();

                URL url = new URL(builtUri.toString());


                Log.v(Log_tag, "Built Uri : " + builtUri.toString());

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
               /* urlConnection.setRequestProperty("API_key", "e97c80b36eea355c51ab288c82dff58d");
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);*/
                urlConnection.connect();
                //Log.v(Log_tag, "Forecast String :333333333333333333");

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                forecastJsonStr = buffer.toString();
                // Log.v(Log_tag, "Forecast String :44444444444444444");

                Log.v(Log_tag, "Forecast String : " + forecastJsonStr);             //ALL DATA

            } catch (MalformedURLException e) {
                Log.e(Log_tag, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } catch (ProtocolException e) {
                Log.e(Log_tag, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } catch (IOException e) {
                Log.e(Log_tag, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                    Log.v(Log_tag, "Forecast String :55555555555555");
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(Log_tag, "Error closing stream", e);
                    }
                }
            }


            try {
                return getWeatherDataFromJson(forecastJsonStr, numDays);
            } catch (JSONException e) {
                Log.e(Log_tag, e.getMessage(), e);
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


            return null;
        }

        @Override
        protected void onPostExecute(String[] result) {
            if (result != null) {
                for (String dayForcast : result) {
                    if (x == 1) {
                        ForcastAdapter.clear();
                    }
                    x++;
                    ForcastAdapter.add(dayForcast);

                }
            }

        }
    }


}

