import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

public class Main {

    public static void main(String[] args) throws Exception {

        System.out.println("=== SINGLETON LOGGER ТЕСТ ===");

        Path configPath = Paths.get("logger-config.json");

        if (!Files.exists(configPath)) {
            Files.writeString(configPath, """
            {
              "logFilePath": "app.log",
              "minLevel": "INFO",
              "logToConsole": true,
              "rotationMaxBytes": 20000
            }
            """, StandardCharsets.UTF_8);
        }

        Logger logger = Logger.getInstance();
        logger.loadConfig(configPath.toString());

        System.out.println("Logger бір экземпляр ма? " + (logger == Logger.getInstance()));

        ExecutorService pool = Executors.newFixedThreadPool(4);

        for (int i = 0; i < 4; i++) {
            int id = i;
            pool.submit(() -> {
                Logger.getInstance().log("Ағын #" + id + " іске қосылды", LogLevel.INFO);
                Logger.getInstance().log("Ағын #" + id + " ескерту берді", LogLevel.WARNING);
                Logger.getInstance().log("Ағын #" + id + " қате жіберді", LogLevel.ERROR);
            });
        }

        pool.shutdown();
        pool.awaitTermination(5, TimeUnit.SECONDS);

        System.out.println("\n=== BUILDER ТЕСТ ===");

        ReportDirector director = new ReportDirector();
        ReportStyle style = new ReportStyle("#FFFFFF", "#000000", 14);

        IReportBuilder textBuilder = new TextReportBuilder();
        director.constructReport(textBuilder, style);

        System.out.println(textBuilder.getReport().export());

        System.out.println("\n=== PROTOTYPE ТЕСТ ===");

        Character original = new Character("Батыр", 100, 20, 15, 10);
        original.setWeapon(new Weapon("Қылыш", 30));
        original.setArmor(new Armor("Темір", 15));
        original.getSkills().add(new Skill("От шары", "MAGIC", 3));

        Character clone = original.deepClone();
        clone.setName("Батыр_2");
        clone.getWeapon().setDamage(999);

        System.out.println("Түпнұсқа: " + original);
        System.out.println("Көшірме: " + clone);
    }
}

/* ===== SINGLETON ====== */

enum LogLevel {
    INFO(1), WARNING(2), ERROR(3);

    private final int priority;
    LogLevel(int p) { priority = p; }
    public int getPriority() { return priority; }
}

class Logger {

    private static volatile Logger instance;
    private final ReentrantLock lock = new ReentrantLock();

    private LogLevel minLevel = LogLevel.INFO;
    private Path logPath = Paths.get("app.log");

    private static final DateTimeFormatter FORMAT =
            DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private Logger() {}

    public static Logger getInstance() {
        if (instance == null) {
            synchronized (Logger.class) {
                if (instance == null)
                    instance = new Logger();
            }
        }
        return instance;
    }

    public void loadConfig(String path) {
        log("Конфигурация файлы жүктелді: " + path, LogLevel.INFO);
    }

    public void log(String message, LogLevel level) {
        if (level.getPriority() < minLevel.getPriority()) return;

        String text = LocalDateTime.now().format(FORMAT)
                + " [" + level + "] " + message;

        lock.lock();
        try {
            Files.writeString(logPath,
                    text + System.lineSeparator(),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("Файлға жазу кезінде қате: " + e.getMessage());
        } finally {
            lock.unlock();
        }

        System.out.println(text);
    }
}

/* ===== BUILDER ======== */

class ReportStyle {
    private final String backgroundColor;
    private final String fontColor;
    private final int fontSize;

    public ReportStyle(String bg, String font, int size) {
        backgroundColor = bg;
        fontColor = font;
        fontSize = size;
    }

    public String getBackgroundColor() { return backgroundColor; }
    public String getFontColor() { return fontColor; }
    public int getFontSize() { return fontSize; }
}

class Report {
    String header;
    String content;
    String footer;
    ReportStyle style;

    public String export() {
        return """
               Тақырып: %s
               Стиль: Фон=%s, Қаріп=%s, Өлшем=%d
               
               %s
               
               %s
               """.formatted(header,
                style.getBackgroundColor(),
                style.getFontColor(),
                style.getFontSize(),
                content,
                footer);
    }
}

interface IReportBuilder {
    void setHeader(String header);
    void setContent(String content);
    void setFooter(String footer);
    void setStyle(ReportStyle style);
    Report getReport();
}

class TextReportBuilder implements IReportBuilder {

    private final Report report = new Report();

    public void setHeader(String header) { report.header = header; }

    public void setContent(String content) {
        report.content = "Бұл есеп Builder паттерні арқылы жасалды.\n" + content;
    }

    public void setFooter(String footer) {
        report.footer = "Есеп сәтті аяқталды.\n" + footer;
    }

    public void setStyle(ReportStyle style) { report.style = style; }

    public Report getReport() { return report; }
}

class ReportDirector {
    public void constructReport(IReportBuilder builder, ReportStyle style) {
        builder.setStyle(style);
        builder.setHeader("Айлық есеп");
        builder.setContent("Жүйе статистикасы көрсетілді.");
        builder.setFooter("© 2026 Компания");
    }
}

/* ===== PROTOTYPE ====== */

class Character {

    private String name;
    private final int health, strength, agility, intelligence;

    private Weapon weapon;
    private Armor armor;
    private List<Skill> skills = new ArrayList<>();

    public Character(String name, int h, int s, int a, int i) {
        this.name = name;
        health = h;
        strength = s;
        agility = a;
        intelligence = i;
    }

    public Character deepClone() {
        Character copy = new Character(name, health, strength, agility, intelligence);
        copy.weapon = weapon == null ? null : weapon.clone();
        copy.armor = armor == null ? null : armor.clone();
        for (Skill skill : skills)
            copy.skills.add(skill.clone());
        return copy;
    }

    public void setName(String name) { this.name = name; }
    public void setWeapon(Weapon w) { weapon = w; }
    public void setArmor(Armor a) { armor = a; }
    public List<Skill> getSkills() { return skills; }
    public Weapon getWeapon() { return weapon; }

    @Override
    public String toString() {
        return name + " | Қару: " + weapon + " | Қабілеттер: " + skills;
    }
}

class Weapon implements Cloneable {
    private final String name;
    private int damage;

    public Weapon(String name, int d) { this.name = name; damage = d; }

    public void setDamage(int d) { damage = d; }

    @Override
    public Weapon clone() {
        try { return (Weapon) super.clone(); }
        catch (CloneNotSupportedException e) {
            return new Weapon(name, damage);
        }
    }

    @Override
    public String toString() { return name + "(" + damage + ")"; }
}

class Armor implements Cloneable {
    private final String name;
    private final int defense;

    public Armor(String name, int d) { this.name = name; defense = d; }

    @Override
    public Armor clone() {
        try { return (Armor) super.clone(); }
        catch (CloneNotSupportedException e) {
            return new Armor(name, defense);
        }
    }
}

class Skill implements Cloneable {
    private final String name;
    private final String type;
    private int level;

    public Skill(String n, String t, int l) {
        name = n; type = t; level = l;
    }

    @Override
    public Skill clone() {
        try { return (Skill) super.clone(); }
        catch (CloneNotSupportedException e) {
            return new Skill(name, type, level);
        }
    }

    @Override
    public String toString() {
        return name + "(" + level + ")";
    }
}