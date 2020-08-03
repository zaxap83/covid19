package com.zaxap83;

import java.net.*;
import java.io.*;
import java.util.Date;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {

    public static void main(String[] args) throws IOException {

        System.out.print("\033[H\033[2J");
        System.out.flush();

        String country = "Ukraine";

        if( args.length == 2 && ( args[0].equals( "-country" ) || args[0].equals( "-c" ) ) ) {
            country = args[1];
        }

        Boolean URLExist = false;
        List<List<String>> regions = new ArrayList<>();
        List<List<String>> regionsPrevious = new ArrayList<>();

        Date currentDate = new Date();
        Calendar cal = Calendar.getInstance( TimeZone.getTimeZone("Europe/London") );

        cal.setTime( currentDate );

        String CSVUrlString = generateUrl( cal, 0 );
        URL CSVUrl = new URL( CSVUrlString );

        while( !doesURLExist( CSVUrl ) ) {
            CSVUrlString = generateUrl( cal, 1 );
            CSVUrl = new URL( CSVUrlString );
        }

        int statYear = cal.get( Calendar.YEAR );
        int statMonth = cal.get( Calendar.MONTH ) + 1;
        int statDay = cal.get( Calendar.DAY_OF_MONTH );

        String statMonthStr = String.format("%02d", statMonth);
        String statDayStr = String.format("%02d", statDay);

        System.out.println( "COVID19 statistic for " + country + ", " + statDayStr + "." + statMonthStr + "." + statYear );

        String CSVUrlStringPrevious = generateUrl( cal, 1 );
        URL CSVUrlPrevious = new URL( CSVUrlStringPrevious );

        regionsPrevious = getCSVList( CSVUrlPrevious, country );
        regions = getCSVList( CSVUrl, country );

        PrintResultTable( regions, regionsPrevious );
    }

    public static List<List<String>> getCSVList( URL CSVUrl, String country ) throws IOException {

        BufferedReader br = null;
        String cvsSplitBy = ",";
        String line = "";

        List<List<String>> regionsList = new ArrayList<>();

        try {

            br = new BufferedReader(new InputStreamReader( CSVUrl.openStream() ));

            while ((line = br.readLine()) != null) {

                String[] region = line.split( cvsSplitBy );

                if( region[3].equals( country ) ) {
                    regionsList.add( Arrays.asList( region ) );
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return regionsList;
    }

    public static void PrintResultTable ( List<List<String>> strings, List<List<String>> previous ) {

        if( strings.size() == 0 ) {
            System.exit(1);
        }

        int Confirmed = 0;
        int Deaths = 0;
        int Recovered = 0;
        int Active = 0;

        int ConfirmedPrevious = 0;
        int DeathsPrevious = 0;
        int RecoveredPrevious = 0;
        int ActivePrevious = 0;

        boolean countPrevious = false;

        if( previous.size() > 0 ) {
            countPrevious = true;
        }

        String leftAlignFormat = "| %-32s | %-19s | %-15s | %-11s | %-14s | %-15s|%n";

        System.out.format("+----------------------------------+---------------------+-----------------+-------------+----------------+----------------+%n");
        System.out.format("| Country Region                   | Last Update         | Confirmed       | Deaths      | Recovered      | Active         |%n");
        System.out.format("+----------------------------------+---------------------+-----------------+-------------+----------------+----------------+%n");

        int i = 0;

        for ( List<?> strToPrint : strings ) {

            int ConfirmedValue = Integer.parseInt( (String) strToPrint.get(7) );
            int DeathsValue = Integer.parseInt( (String) strToPrint.get(8) );
            int RecoveredValue = Integer.parseInt( (String) strToPrint.get(9) );
            int ActiveValue = Integer.parseInt( (String) strToPrint.get(10) );

            String ConfirmedText = String.valueOf( ConfirmedValue );
            String DeathsText = String.valueOf( DeathsValue );
            String RecoveredText = String.valueOf( RecoveredValue );
            String ActiveText = String.valueOf( ActiveValue );

            if( countPrevious ) {

                List<String> previousLine = previous.get( i );

                if( previousLine.size() > 0 ) {

                    int ConfirmedPreviousValue = Integer.parseInt( previousLine.get(7) );
                    int DeathsPreviousValue = Integer.parseInt( previousLine.get(8) );
                    int RecoveredPreviousValue = Integer.parseInt( previousLine.get(9) );
                    int ActivePreviousValue = Integer.parseInt( previousLine.get(10) );

                    ConfirmedText = ConfirmedValue + " (" + ( ConfirmedValue - ConfirmedPreviousValue ) + ")";
                    DeathsText = DeathsValue + " (" + ( DeathsValue - DeathsPreviousValue ) + ")";
                    RecoveredText = RecoveredValue + " (" + ( RecoveredValue - RecoveredPreviousValue ) + ")";
                    ActiveText = ActiveValue + " (" + ( ActiveValue - ActivePreviousValue ) + ")";

                    ConfirmedPrevious += ConfirmedPreviousValue;
                    DeathsPrevious += DeathsPreviousValue;
                    RecoveredPrevious += RecoveredPreviousValue;
                    ActivePrevious += ActivePreviousValue;
                }
            }

            System.out.format( leftAlignFormat, strToPrint.get(2), strToPrint.get(4), ConfirmedText, DeathsText, RecoveredText, ActiveText );

            Confirmed += ConfirmedValue;
            Deaths += DeathsValue;
            Recovered += RecoveredValue;
            Active += ActiveValue;

            i++;
        }

        String newConfirmed = "";
        String newDeaths = "";
        String newRecovered = "";
        String newActive = "";

        if( ConfirmedPrevious > 0 ) {
            newConfirmed = " (" + ( Confirmed - ConfirmedPrevious ) + ")";
        }

        if( DeathsPrevious > 0 ) {
            newDeaths = " (" + ( Deaths - DeathsPrevious ) + ")";
        }

        if( RecoveredPrevious > 0 ) {
            newRecovered = " (" + ( Recovered - RecoveredPrevious ) + ")";
        }

        if( ActivePrevious > 0 ) {
            newActive = " (" + ( Active - ActivePrevious ) + ")";
        }

        System.out.format("+----------------------------------+---------------------+-----------------+-------------+----------------+----------------+%n");
        System.out.format( leftAlignFormat, "Total", "", Confirmed + newConfirmed, Deaths + newDeaths, Recovered + newRecovered, Active + newActive );
        System.out.format("+----------------------------------+---------------------+-----------------+-------------+----------------+----------------+%n");
        System.out.format("%n");

    }

    public static String generateUrl( Calendar cal, int dayMinus ) {

        cal.add(Calendar.DATE, -dayMinus);

        int year = cal.get( Calendar.YEAR );
        int month = cal.get( Calendar.MONTH ) + 1;
        int day = cal.get( Calendar.DAY_OF_MONTH );

        String monthStr = String.format("%02d", month);
        String dayStr = String.format("%02d", day);

        String CSVUrlString = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_daily_reports/"+monthStr+"-"+dayStr+"-"+year+".csv";

        return CSVUrlString;
    }

    public static boolean doesURLExist(URL url) throws IOException
    {
        HttpURLConnection.setFollowRedirects(false);

        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

        httpURLConnection.setRequestMethod("HEAD");

        httpURLConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 6.0; en-US; rv:1.9.1.2) Gecko/20090729 Firefox/3.5.2 (.NET CLR 3.5.30729)");
        int responseCode = httpURLConnection.getResponseCode();

        return responseCode == HttpURLConnection.HTTP_OK;
    }

    private static String getUrlContents(String theUrl)
    {
        StringBuilder content = new StringBuilder();

        // many of these calls can throw exceptions, so i've just
        // wrapped them all in one try/catch statement.
        try
        {
            // create a url object
            URL url = new URL(theUrl);

            // create a urlconnection object
            URLConnection urlConnection = url.openConnection();

            // wrap the urlconnection in a bufferedreader
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

            String line;

            // read from the urlconnection via the bufferedreader
            while ((line = bufferedReader.readLine()) != null)
            {
                content.append(line + "\n");
            }
            bufferedReader.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return content.toString();
    }
}
