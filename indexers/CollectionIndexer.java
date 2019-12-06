import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.common.SolrInputDocument;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 * Hello world!
 */
public class CollectionIndexer {

  private static final String urlString = "http://127.0.0.1:8983/solr/hncore/";
  private static final SolrClient solr = new HttpSolrClient.Builder(urlString).build();

  public static void main(String args[]) throws Exception {

    if (args.length != 1) {
      throw new IllegalArgumentException(
          "You must only indicate the path of the dataset.");
    }
    String datasetPath = args[0];

    System.out.println("Clearing previous collection.");
    // First deleting previous collection.
    // Deleting the documents from Solr
    solr.deleteByQuery("*");

    // Saving the document
    solr.commit();
    System.out.println("Documents deleted");

    File folder = new File(datasetPath);

    addDocumentsFromFile(folder);

    System.out.println("Documents indexed");

  }

  public static void addDocumentsFromFile(File f) throws IOException, SolrServerException {
    JSONParser jsonParser = new JSONParser();
    List<SolrInputDocument> documentsList = new ArrayList<>();
    SolrInputDocument document = null;
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss zzz");
    Date parsedDate;
    int documentsProcessed = 0;


    try (BufferedReader br = new BufferedReader(new FileReader(f))) {
      String line;

      while ((line = br.readLine()) != null) {
        document = new SolrInputDocument();
        //Read JSON file
        Object obj = jsonParser.parse(line);
        JSONObject storyObj = (JSONObject) obj;

        if (storyObj.get("id") != null) {
          String id = (String) storyObj.get("id");
          document.addField("id", id);
        }

        if (storyObj.get("text") != null) {
          String text = (String) storyObj.get("text");
          document.addField("text", text);
        }

        if (storyObj.get("title") != null) {
          String title = (String) storyObj.get("title");
          document.addField("title", title);
        }

        if (storyObj.get("url") != null) {
          String url = (String) storyObj.get("url");
          document.addField("url", url);
        }

        if (storyObj.get("by") != null) {
          String author = (String) storyObj.get("by");
          document.addField("author", author);
        }

        if (storyObj.get("timestamp") != null) {
          String timestamp = (String) storyObj.get("timestamp");
          parsedDate = dateFormat.parse(timestamp);
          document.addField("timestamp", parsedDate);
        }

        if (storyObj.get("score") != null) {
          Integer popularityPoints = Integer.parseInt((String) storyObj.get("score"));
          document.addField("popularity_points", popularityPoints);
        }

        JSONArray commentsList = (JSONArray) storyObj.get("f0_");
        for (Object commentObj :
            commentsList
        ) {
          JSONObject comment = (JSONObject) commentObj;
          document.addField("comments", comment.get("text"));
        }

        if (storyObj.get("descendants") != null) {
          Integer descendants = Integer.parseInt((String) storyObj.get("descendants"));
          document.addField("comments_number", descendants);
        }

        documentsList.add(document);

        documentsProcessed++;
        if (documentsProcessed == 10000) {
          solr.add(documentsList);
          solr.commit();
          documentsList.clear();
          documentsProcessed = 0;
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    solr.add(documentsList);
    solr.commit();
  }

}
