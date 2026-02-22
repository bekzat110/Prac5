import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Main {

    public static void main(String[] args) {

        // 1) SINGLETON ТЕСТ
        System.out.println("=== SINGLETON ТЕСТ ===");

        ConfigurationManager config1 = ConfigurationManager.getInstance();
        ConfigurationManager config2 = ConfigurationManager.getInstance();

        // Екі объект бірдей ме?
        System.out.println("Бір объект пе? " + (config1 == config2));

        config1.setSetting("Тіл", "Қазақша");
        System.out.println("Тіл: " + config2.getSetting("Тіл"));

        // 2) BUILDER ТЕСТ
        System.out.println("\n=== BUILDER ТЕСТ ===");

        ReportDirector director = new ReportDirector();

        // Мәтіндік есеп
        IReportBuilder textBuilder = new TextReportBuilder();
        director.constructReport(textBuilder,
                "Апталық есеп",
                "Осы аптада жоспар толық орындалды.",
                "Авторы: Аида");

        Report textReport = textBuilder.getReport();
        System.out.println(textReport.show());

        // HTML есеп
        IReportBuilder htmlBuilder = new HtmlReportBuilder();
        director.constructReport(htmlBuilder,
                "HTML есеп",
                "Түсім 10% артты.",
                "System");

        Report htmlReport = htmlBuilder.getReport();
        System.out.println(htmlReport.show());

        // 3) PROTOTYPE ТЕСТ
        System.out.println("\n=== PROTOTYPE ТЕСТ ===");

        Order originalOrder = new Order();
        originalOrder.addProduct(new Product("Тінтуір", 5000, 1));
        originalOrder.setDeliveryCost(1000);
        originalOrder.setPaymentMethod("Карта");

        // Көшіру
        Order copiedOrder = originalOrder.clone();
        copiedOrder.setPaymentMethod("Қолма-қол");

        System.out.println("Түпнұсқа төлем: " + originalOrder.getPaymentMethod());
        System.out.println("Көшірме төлем: " + copiedOrder.getPaymentMethod());
    }
}

// 1️⃣ SINGLETON (Одиночка)

class ConfigurationManager {

    // Бір ғана экземпляр
    private static ConfigurationManager instance;

    // Настройкалар сақталатын Map
    private Map<String, String> settings;

    // Private конструктор
    private ConfigurationManager() {
        settings = new ConcurrentHashMap<>();
    }

    // Объектіні алу әдісі
    public static synchronized ConfigurationManager getInstance() {
        if (instance == null) {
            instance = new ConfigurationManager();
        }
        return instance;
    }

    // Настройка қосу
    public void setSetting(String key, String value) {
        settings.put(key, value);
    }

    // Настройканы алу
    public String getSetting(String key) {
        if (!settings.containsKey(key)) {
            throw new RuntimeException("Мұндай параметр жоқ!");
        }
        return settings.get(key);
    }
}

// 2️⃣ BUILDER (Строитель)

// Интерфейс
interface IReportBuilder {
    void setHeader(String header);
    void setContent(String content);
    void setFooter(String footer);
    Report getReport();
}

// Нәтиже объектісі
class Report {
    private String header;
    private String content;
    private String footer;

    public void setHeader(String header) { this.header = header; }
    public void setContent(String content) { this.content = content; }
    public void setFooter(String footer) { this.footer = footer; }

    public String show() {
        return header + "\n" + content + "\n" + footer;
    }
}

// Мәтіндік есеп құрастырушы
class TextReportBuilder implements IReportBuilder {

    private Report report = new Report();

    public void setHeader(String header) {
        report.setHeader(header);
    }

    public void setContent(String content) {
        report.setContent(content);
    }

    public void setFooter(String footer) {
        report.setFooter(footer);
    }

    public Report getReport() {
        return report;
    }
}

// HTML есеп құрастырушы
class HtmlReportBuilder implements IReportBuilder {

    private Report report = new Report();

    public void setHeader(String header) {
        report.setHeader("<h1>" + header + "</h1>");
    }

    public void setContent(String content) {
        report.setContent("<p>" + content + "</p>");
    }

    public void setFooter(String footer) {
        report.setFooter("<small>" + footer + "</small>");
    }

    public Report getReport() {
        return report;
    }
}

// Басқарушы класс
class ReportDirector {

    public void constructReport(IReportBuilder builder,
                                String header,
                                String content,
                                String footer) {

        builder.setHeader(header);
        builder.setContent(content);
        builder.setFooter(footer);
    }
}

// 3️⃣ PROTOTYPE (Прототип)

// Тауар
class Product implements Cloneable {

    private String name;
    private int price;
    private int quantity;

    public Product(String name, int price, int quantity) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    public int getTotal() {
        return price * quantity;
    }

    @Override
    public Product clone() {
        try {
            return (Product) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Көшіру мүмкін болмады!");
        }
    }
}

// Тапсырыс
class Order implements Cloneable {

    private List<Product> products = new ArrayList<>();
    private int deliveryCost;
    private String paymentMethod;

    public void addProduct(Product product) {
        products.add(product);
    }

    public void setDeliveryCost(int cost) {
        this.deliveryCost = cost;
    }

    public void setPaymentMethod(String method) {
        this.paymentMethod = method;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    @Override
    public Order clone() {
        try {
            Order copy = (Order) super.clone();

            // Терең көшіру
            copy.products = new ArrayList<>();
            for (Product p : this.products) {
                copy.products.add(p.clone());
            }

            return copy;

        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Тапсырысты көшіру мүмкін болмады!");
        }
    }
}