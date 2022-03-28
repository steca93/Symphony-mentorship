package JDBI;
import JDBI.DBConfig.DBConnection;
import JDBI.DBConfig.DBRestore;
import JDBI.Dtos.MostEfficientEmployeeDto;
import JDBI.Mappers.MostEfficientEmployeeMapper;
import org.jdbi.v3.core.Jdbi;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.List;

public class JDBIZadatak {

    public static void main(String[] args) throws FileNotFoundException, SQLException {

        Jdbi jdbi = new DBConnection().getConnection();

        int newCategoryId = insertNewCategory(jdbi, "Varied", "auto");
        int beveragesCategoryId = getCategoryId(jdbi, "Beverages");

        updateCategoriesInProductTable(jdbi, newCategoryId, beveragesCategoryId);

        printResults(jdbi, "Varied");

        DBRestore.restoreDB(jdbi.open().getConnection());
    }

    private static int insertNewCategory(Jdbi jdbi, String category_name, String category_description) {
        return jdbi.withHandle(handle ->
                handle.createQuery("INSERT INTO categories (category_id, category_name, description, picture) " +
                                "VALUES ((SELECT category_id FROM categories ORDER BY category_id DESC LIMIT 1) + 1, :category_name, :description, NULL) RETURNING category_id")
                        .bind("category_name", category_name)
                        .bind("description", category_description)
                        .mapTo(Integer.class)
                        .one()
        );
    }

    private static int getCategoryId(Jdbi jdbi, String category_name) {
        return jdbi.withHandle(handle ->
                handle.select("SELECT category_id FROM categories WHERE category_name = :category_name")
                        .bind("category_name", category_name)
                        .mapTo(Integer.class)
                        .one()
        );
    }

    private static void updateCategoriesInProductTable(Jdbi jdbi, int newCategoryId, int oldCategoryId) {
        jdbi.useHandle(handle ->
                handle.createUpdate("UPDATE products SET category_id = :new WHERE category_id = :old")
                        .bind("new", newCategoryId)
                        .bind("old", oldCategoryId)
                        .execute()
        );
    }

    private static List<MostEfficientEmployeeDto> getResults(Jdbi jdbi, String category_name) {
        List<MostEfficientEmployeeDto> results = jdbi.withHandle(handle ->
                handle.createQuery("SELECT product_name, CONCAT(first_name, ' ', last_name) as employee_name, ukupno_ordera FROM\n" +
                                "(SELECT product_id, employee_id, ukupno_ordera, ukupno_prodato, MAX(ukupno_prodato) OVER (PARTITION BY product_id) as max_prodato FROM\n" +
                                "(SELECT o.order_id, product_id, quantity, employee_id, COUNT(o.order_id) OVER (PARTITION BY product_id) as ukupno_ordera, \n" +
                                "SUM(quantity) OVER (PARTITION BY product_id, employee_id) as ukupno_prodato FROM order_details od \n" +
                                "LEFT JOIN orders o ON od.order_id = o.order_id\n" +
                                "WHERE od.product_id IN (SELECT product_id FROM products p LEFT JOIN categories c ON p.category_id = c.category_id WHERE c.category_name = :category_name)) as first_table\n" +
                                "GROUP BY product_id, employee_id, ukupno_ordera, ukupno_prodato\n" +
                                "ORDER BY product_id) as second_table\n" +
                                "LEFT JOIN products p ON second_table.product_id = p.product_id\n" +
                                "LEFT JOIN employees e ON second_table.employee_id = e.employee_id\n" +
                                "WHERE ukupno_prodato = max_prodato")
                        .bind("category_name", category_name)
                        .map(new MostEfficientEmployeeMapper())
                        .list()
        );
        return results;
    }

    private static void printResults(Jdbi jdbi, String category_name) {
        getResults(jdbi, category_name).stream().forEach(System.out::println);
    }
}
