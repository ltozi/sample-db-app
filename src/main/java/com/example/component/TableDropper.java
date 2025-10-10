package com.example.component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import com.example.service.ExampleRecordService;

import java.io.Console;
import java.util.Scanner;

@Component
public class TableDropper {

    private static final Logger logger = LoggerFactory.getLogger(TableDropper.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ApplicationContext applicationContext;
    
    @Autowired
    private ExampleRecordService exampleRecordService;

    @Value("${app.drop-table-and-exit:false}")
    private boolean dropTableAndExit;

    @Value("${app.force-drop:false}")
    private boolean forceDrop;

    @EventListener(ApplicationReadyEvent.class)
    public void handleApplicationReady() {
        if (dropTableAndExit) {
            logger.info("Drop table flag detected. Processing table drop request...");
            
            if (forceDrop || confirmTableDrop()) {
                // Disable scheduled tasks first to prevent errors during shutdown
                logger.info("Disabling scheduled tasks before dropping table...");
                exampleRecordService.disableScheduledTasks();
                
                // Give a moment for any running scheduled tasks to finish
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                
                dropExampleRecordsTable();
            } else {
                logger.info("Table drop cancelled by user. Exiting...");
            }
            
            exitApplication();
        }
    }

    private boolean confirmTableDrop() {
        try {
            // First, check if table exists and show record count
            showTableInfo();
            
            System.out.println("\n" + "=".repeat(60));
            System.out.println("⚠️  WARNING: You are about to DROP the 'example_records' table!");
            System.out.println("This action is IRREVERSIBLE and will delete ALL data.");
            System.out.println("Database: " + getDatabaseInfo());
            System.out.println("=".repeat(60));
            
            // Try to use Console first (works in real terminals)
            Console console = System.console();
            if (console != null) {
                String response = console.readLine("\nType 'DROP TABLE' to confirm (case sensitive): ");
                return "DROP TABLE".equals(response);
            } else {
                // Fallback to Scanner for IDEs and other environments
                System.out.print("\nType 'DROP TABLE' to confirm (case sensitive): ");
                Scanner scanner = new Scanner(System.in);
                String response = scanner.nextLine();
                return "DROP TABLE".equals(response);
            }
            
        } catch (Exception e) {
            logger.error("Error during confirmation: {}", e.getMessage());
            return false;
        }
    }

    private void showTableInfo() {
        try {
            // Check if table exists
            String checkTableQuery = """
                SELECT EXISTS (
                    SELECT FROM information_schema.tables 
                    WHERE table_schema = 'public' 
                    AND table_name = 'example_records'
                )
                """;
            
            Boolean tableExists = jdbcTemplate.queryForObject(checkTableQuery, Boolean.class);
            
            if (Boolean.TRUE.equals(tableExists)) {
                // Get record count
                String countQuery = "SELECT COUNT(*) FROM example_records";
                Long recordCount = jdbcTemplate.queryForObject(countQuery, Long.class);
                
                System.out.println("\n📊 Table Information:");
                System.out.println("   Table: example_records");
                System.out.println("   Status: EXISTS");
                System.out.println("   Records: " + recordCount);
                
                // Show sample records if any exist
                if (recordCount != null && recordCount > 0) {
                    String sampleQuery = "SELECT name, value, created_at FROM example_records ORDER BY created_at DESC LIMIT 3";
                    System.out.println("\n📝 Recent records (last 3):");
                    jdbcTemplate.query(sampleQuery, rs -> {
                        System.out.printf("   - %s | %s | %s%n", 
                            rs.getString("name"),
                            rs.getString("value"),
                            rs.getTimestamp("created_at")
                        );
                    });
                }
            } else {
                System.out.println("\n📊 Table Information:");
                System.out.println("   Table: example_records");
                System.out.println("   Status: DOES NOT EXIST");
            }
            
        } catch (Exception e) {
            System.out.println("\n❌ Could not retrieve table information: " + e.getMessage());
        }
    }

    private String getDatabaseInfo() {
        try {
            String query = "SELECT current_database() as db_name, inet_server_addr() as host, inet_server_port() as port";
            return jdbcTemplate.queryForObject(query, (rs, rowNum) -> 
                String.format("%s@%s:%d", 
                    rs.getString("db_name"),
                    rs.getString("host"),
                    rs.getInt("port")
                )
            );
        } catch (Exception e) {
            return "Unknown database";
        }
    }

    private void dropExampleRecordsTable() {
        try {
            // Check if the table exists first
            String checkTableQuery = """
                SELECT EXISTS (
                    SELECT FROM information_schema.tables 
                    WHERE table_schema = 'public' 
                    AND table_name = 'example_records'
                )
                """;
            
            Boolean tableExists = jdbcTemplate.queryForObject(checkTableQuery, Boolean.class);
            
            if (Boolean.TRUE.equals(tableExists)) {
                System.out.println("\n🗑️  Dropping table...");
                String dropTableQuery = "DROP TABLE IF EXISTS example_records CASCADE";
                jdbcTemplate.execute(dropTableQuery);
                System.out.println("✅ Successfully dropped example_records table");
                logger.info("Successfully dropped example_records table");
            } else {
                System.out.println("\nℹ️  Table example_records does not exist, nothing to drop");
                logger.info("Table example_records does not exist, nothing to drop");
            }
            
        } catch (Exception e) {
            System.err.println("\n❌ Failed to drop example_records table: " + e.getMessage());
            logger.error("Failed to drop example_records table: {}", e.getMessage(), e);
            System.exit(1); // Exit with error code if drop fails
        }
    }

    private void exitApplication() {
        System.out.println("\n👋 Shutting down application...");
        logger.info("Table drop operation completed. Shutting down application...");
        
        // Use Spring's graceful shutdown instead of System.exit()
        SpringApplication.exit(applicationContext, () -> 0);
    }
}