import java.awt.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.sql.Connection;
import java.sql.DriverManager;



public class visLauncher {
    public static ResultSet executeSQL(String arg) throws SQLException, ClassNotFoundException {
        // for testing my query graph executor with zql.html
        // String outputExecutor = zvMain.runZQLCompleteQuery(arg);
        Class.forName("org.postgresql.Driver");
        Connection c = DriverManager
                .getConnection("jdbc:postgresql://localhost:5432/postgres",
                        "postgres", "zenvisage");
        c.setAutoCommit(false);
        System.out.println("Opened database successfully");
        Statement stmt = c.createStatement();
        ResultSet rs = stmt.executeQuery( arg );
        ResultSetMetaData rsmd = rs.getMetaData();

        return rs;

    }
    public static String create2DGraph(String html, String sql) throws SQLException, ClassNotFoundException {
        ResultSet rs = executeSQL(sql);
        StringBuilder sb = new StringBuilder();
        while (rs.next()){
            sb.append("{x: "+rs.getInt(1)+", y: " + rs.getInt(2) +"},\n");
        }
        sb.setLength(sb.length()-2);
        return html.replaceAll("<2DGraphData>", sb.toString());

    }
    public static void main(String[] args) throws IOException, SQLException, ClassNotFoundException, URISyntaxException {
        String htmlStr = new String(Files.readAllBytes(Paths.get("src/index.html")), StandardCharsets.UTF_8);
        String html = create2DGraph(htmlStr, "select top 100 day, count from data58");
        PrintWriter pw = new PrintWriter("src/temp.html");
        pw.write(html);
        Desktop.getDesktop().browse(new URI("src/temp.html"));



        /*
              {x: '2014-06-11', y: 10},
      {x: '2014-06-12', y: 25},
      {x: '2014-06-13', y: 30},
      {x: '2014-06-14', y: 10},
      {x: '2014-06-15', y: 15},
      {x: '2014-06-16', y: 30}
         */

    }
}
