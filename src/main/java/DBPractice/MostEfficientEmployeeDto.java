package DBPractice;

public class MostEfficientEmployeeDto {

    private String productName;
    private String employeeName;
    private int ordersCount;

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public int getOrdersCount() {
        return ordersCount;
    }

    public void setOrdersCount(int ordersCount) {
        this.ordersCount = ordersCount;
    }

    @Override
    public String toString() {
        return "MostEfficientEmployeeDto{" +
                "productName='" + productName + '\'' +
                ", employeeName='" + employeeName + '\'' +
                ", ordersCount=" + ordersCount +
                '}';
    }
}
