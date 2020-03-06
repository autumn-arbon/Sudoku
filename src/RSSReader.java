import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;



public class RSSReader {
    public static void main(String[] args) throws Exception{
        System.out.println(readRSS("http://rss.cnn.com/rss/cnn_topstories.rss"));
        System.out.println((readWeatherRSS("https://w1.weather.gov/xml/current_obs/KLGU.rss")));
    }

    public static String readRSS(String urlAddress) throws Exception {
        URL rssURL = new URL(urlAddress);
        BufferedReader in = new BufferedReader(new InputStreamReader(rssURL.openStream()));
        String sourceCode = "";
        String titles = "";
        String line;
        while((line = in.readLine()) != null) {
            if(line.contains("title")) {
                titles = line;
                int index = titles.indexOf("[CDATA[");
                String crop = "";
                crop = titles.substring(index + 7, titles.indexOf("]]>"));
                sourceCode += crop + "     ***     ";
            }
        }
        in.close();
        return sourceCode;
    }

    public static String readWeatherRSS(String urlAddress) throws Exception {
        URL rssURL = new URL(urlAddress);
        BufferedReader in = new BufferedReader(new InputStreamReader(rssURL.openStream()));
        String sourceCode = "";
        String titles = "";
        String line;
        while((line = in.readLine()) != null) {
            if(line.contains("<title>") && line.contains("/title")){
                titles = line;
                int index = titles.indexOf("<title>");
                String crop = "";
                crop = titles.substring(index + 7, titles.indexOf("</title>"));
                sourceCode += crop + "     ***     ";
            }
            else if (line.contains("/title")) {
                titles = line;
                String crop = titles.substring(0, titles.indexOf("</title>"));
                sourceCode += crop + "     ***     ";
            }

        }
        in.close();
        return sourceCode;
    }
}
