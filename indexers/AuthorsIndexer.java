import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.common.SolrInputDocument;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class AuthorsIndexer {

  private static final String urlString = "http://127.0.0.1:8983/solr/hn_authors_core/";
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

    Map<Integer, Author> authorsDict = getAuthorsData(folder);

    indexMap(authorsDict);
    System.out.println("Documents indexed");

  }

  public static void indexMap(Map<Integer, Author> authorsDict) throws IOException,
      SolrServerException {
    List<SolrInputDocument> documentsList = new ArrayList<>();
    SolrInputDocument document = null;

    for (Entry<Integer, Author> entry :
        authorsDict.entrySet()) {
      Author author = entry.getValue();

      document = new SolrInputDocument();

      document.addField("author", author.getAuthor());
      document.addField("totalVotes", author.getTotalVotes());
      document.addField("totalPosts", author.getNumberOfPosts());
      document.addField("h-index", author.getHIndex());

      documentsList.add(document);
    }

    solr.add(documentsList);
    solr.commit();

  }

  public static Map<Integer, Author> getAuthorsData(File f) {
    JSONParser jsonParser = new JSONParser();
    Map<Integer, Author> dict = new HashMap<>();

    try (BufferedReader br = new BufferedReader(new FileReader(f))) {
      String line;
      String author;
      int popularityPoints;
      while ((line = br.readLine()) != null) {
        Object obj = jsonParser.parse(line);
        JSONObject storyObj = (JSONObject) obj;

        try {
          author = (String) storyObj.get("by");
          popularityPoints = Integer.parseInt((String) storyObj.get("score"));

          Author authorObj = dict.get(author.hashCode());

          if (authorObj != null) {
            authorObj.addPost(popularityPoints);
          } else {
            dict.put(author.hashCode(), new Author(author, popularityPoints));
          }

        } catch (Exception e) {
          System.err.println(e.getMessage());
        }


      }

      return dict;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return dict;
  }

  private static class Author {

    private String author;
    private List<Integer> posts;

    public Author(String author, int post) {
      this.author = author;
      this.posts = new ArrayList<>();
      this.posts.add(post);
    }

    public void addPost(int post) {
      posts.add(post);
    }

    public int getNumberOfPosts() {
      return this.posts.size();
    }

    public int getTotalVotes() {
      return this.posts.stream().mapToInt(Integer::intValue).sum();
    }

    public String getAuthor() {
      return author;
    }

    public int getHIndex() {
      Collections.sort(posts);

      int result = 0;
      for (int i = posts.size() - 1; i >= 0; i--) {
        int cnt = posts.size() - i;
        if (posts.get(i) >= cnt) {
          result = cnt;
        } else {
          break;
        }
      }

      return result;
    }

  }

}
