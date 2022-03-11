package DBPractice;

import org.apache.ibatis.jdbc.ScriptRunner;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Zadatak {

    public static void main(String[] args) throws SQLException, FileNotFoundException {

        try (Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/NorthWind", "postgres" ,"421401pizza")) {
            String variedCategory = "Varied";
            String beveragesCategory = "Beverages";

            int newCategoryId = insertNewCategory(connection, variedCategory, "varied category description");
            int beveragesCategoryId = getCategoryId(connection, beveragesCategory);
            updateCategoriesInProductsTable(connection, newCategoryId, beveragesCategoryId);
            ResultSet rs = getLastResults(connection, variedCategory);
            List<MostEfficientEmployeeDto> results = new ArrayList<>();
            while (rs.next()) {
                MostEfficientEmployeeDto r = new MostEfficientEmployeeDto();
                r.setProductName(rs.getString("product_name"));
                r.setEmployeeName(rs.getString("employee_name"));
                r.setOrdersCount(rs.getInt("ukupno_ordera"));
                results.add(r);
            }

            for (MostEfficientEmployeeDto r : results) {
                System.out.println(r.toString());
            }
            restoreDB(connection);
        }
    }

    private static int insertNewCategory(Connection connection, String categoryName, String categoryDesc) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO categories (category_id, category_name, description, picture) " +
                "VALUES ((SELECT category_id FROM categories ORDER BY category_id DESC LIMIT 1) + 1, ?, ?, NULL) RETURNING category_id");
        preparedStatement.setString(1, categoryName);
        preparedStatement.setString(2, categoryDesc);
        ResultSet rs = preparedStatement.executeQuery();
        return rs.next() ?  rs.getInt("category_id") :  null;
    }

    private static int getCategoryId(Connection connection, String categoryName) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT category_id FROM categories WHERE category_name = ?");
        preparedStatement.setString(1, categoryName);
        ResultSet rs = preparedStatement.executeQuery();
        return rs.next() ? rs.getInt("category_id") : null;
    }

    private static void updateCategoriesInProductsTable(Connection connection, int newCategory, int oldCategory) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("UPDATE products SET category_id = ? WHERE category_id = ?");
        preparedStatement.setInt(1, newCategory);
        preparedStatement.setInt(2, oldCategory);
        preparedStatement.execute();
    }

    private static ResultSet getLastResults(Connection connection, String categoryName) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT product_name, CONCAT(first_name, ' ', last_name) as employee_name, ukupno_ordera FROM\n" +
                "(SELECT product_id, employee_id, ukupno_ordera, ukupno_prodato, MAX(ukupno_prodato) OVER (PARTITION BY product_id) as max_prodato FROM\n" +
                "(SELECT o.order_id, product_id, quantity, employee_id, COUNT(o.order_id) OVER (PARTITION BY product_id) as ukupno_ordera, \n" +
                "SUM(quantity) OVER (PARTITION BY product_id, employee_id) as ukupno_prodato FROM order_details od \n" +
                "LEFT JOIN orders o ON od.order_id = o.order_id\n" +
                "WHERE od.product_id IN (SELECT product_id FROM products p LEFT JOIN categories c ON p.category_id = c.category_id WHERE c.category_name = ?)) as first_table\n" +
                "GROUP BY product_id, employee_id, ukupno_ordera, ukupno_prodato\n" +
                "ORDER BY product_id) as second_table\n" +
                "LEFT JOIN products p ON second_table.product_id = p.product_id\n" +
                "LEFT JOIN employees e ON second_table.employee_id = e.employee_id\n" +
                "WHERE ukupno_prodato = max_prodato");
        preparedStatement.setString(1, categoryName);
        return preparedStatement.executeQuery();
    }

    private static void restoreDBToPreviousState(Connection connection, int newCategory, int oldCategory) throws SQLException {
        // Updating products and returning category_id to old value
        PreparedStatement preparedStatement = connection.prepareStatement("UPDATE products SET category_id = ? WHERE category_id = ?");
        preparedStatement.setInt(1, oldCategory);
        preparedStatement.setInt(2, newCategory);
        preparedStatement.execute();

        // Deleting previously created category
        preparedStatement = connection.prepareStatement("DELETE FROM categories WHERE category_id = ?");
        preparedStatement.setInt(1, newCategory);
        preparedStatement.execute();
    }

    private static void restoreDB(Connection connection) throws FileNotFoundException {
        ScriptRunner sr = new ScriptRunner(connection);
        //Creating a reader object
        Reader reader = new BufferedReader(new FileReader("./src/main/resources/restoreNorthWindDB.sql"));
        //Running the script
        sr.setLogWriter(null);
        sr.runScript(reader);
    }
}
