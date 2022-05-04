import java.sql.*;

public class test {
    static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://10.19.126.209:3306/pypi";

    static final String USER = "root";
    static final String PASS = "catlab1a509";
    public static void main(String[] args) {
        Connection conn = null;
        Statement stmt = null;
        try {
            Class.forName(JDBC_DRIVER);
            System.out.println("Connecting to database...");
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
            System.out.println("Creating statement...");
            stmt = conn.createStatement();
            String sql;
            sql = "SELECT url FROM demo where url = MIT";
            ResultSet rs = stmt.executeQuery(sql);
            stmt.execute(sql);

            String url = rs.getString("url");
            int a = rs.getInt(1);
//            while (rs.next()) {
//                String url = rs.getString("url");
//                System.out.println(url);
//            }
            if (url.equals("MIT")) {
                System.out.println("Success");
            }else {
                System.out.println("Fail");
            }
            rs.close();
            stmt.close();
            conn.close();
        }catch(SQLException se){
            // 处理 JDBC 错误
            se.printStackTrace();
        }catch(Exception e){
            // 处理 Class.forName 错误
            e.printStackTrace();
        }finally{
            // 关闭资源
            try{
                if(stmt!=null) stmt.close();
            }catch(SQLException se2){
            }// 什么都不做
            try{
                if(conn!=null) conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }
        }
    }
}
