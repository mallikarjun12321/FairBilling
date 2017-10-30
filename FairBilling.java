import java.io.*;
import java.util.*;

public class FairBilling {
    private static Map<String, User> users = new HashMap<>();
    private static String firstEntryTime;
    private static String lastEntryTime;


    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Please provide filename to be processed as first argument");
        } else {
            populateEntries(args[0]);
            for (String username : users.keySet()) {
                users.get(username).compute();
            }
        }
    }

    public static class User{
        private final String username;
        private List<Entry> entries = new ArrayList<>();
        private int sessions;
        private int activeSessions;
        private int seconds;
        private String lastStartTime;

        User(String name){
            this.username = name;
        }

        void addEntry(Entry e) {
            entries.add(e);
        }

        void compute() {
            ListIterator<Entry> it = entries.listIterator();
            while (it.hasNext()) {
                Entry e = it.next();
                int usage = 0;
                if ("End".equals(e.command)) {
                    if (activeSessions == 0) { // calculating usage of sessions having no Start
                        usage  = computeUsage(firstEntryTime, e.getTime(), ++activeSessions);
                    } else {
                        usage  = computeUsage(lastStartTime, e.getTime(),  activeSessions);
                        lastStartTime = e.getTime();
                    }
                    activeSessions--;
                } else {
                    sessions++;
                    if (activeSessions != 0) {
                        usage  = computeUsage(lastStartTime, e.getTime(),  activeSessions);
                    }
                    activeSessions++;
                    lastStartTime = e.getTime();
                }
                seconds+=usage;
            }
            if (activeSessions != 0) { // calculating usage of sessions having not End
                int usage;
                usage  = computeUsage(lastStartTime, lastEntryTime,  activeSessions);
                seconds+=usage;
            }
            System.out.println(username + " " + sessions + " " + seconds);
        }

        private int computeUsage(String start, String end, int activeSessions) {
            String[] startTime = start.split(":");
            String[] endTime = end.split(":");
            int usage;
            usage = ((Integer.parseInt(endTime[0]) * 3600)+ (Integer.parseInt(endTime[1]) * 60) + Integer.parseInt(endTime[2]))
                    - ((Integer.parseInt(startTime[0]) * 3600)+ (Integer.parseInt(startTime[1]) * 60) + Integer.parseInt(startTime[2]));
            usage = usage * activeSessions;
            return usage;
        }
    }

    public static class Entry {
        private String time;
        private String username;
        private String command;

        Entry(String time, String username, String command) {
            this.time = time;
            this.username = username;
            this.command = command;
        }

        public String getTime() {
            return time;
        }

        public String getUsername() {
            return username;
        }

        public String getCommand() {
            return command;
        }
    }

    static void populateEntries(String filename) {
        List<Entry> logEntries = new ArrayList<>();

        try {
            File file = new File(filename);
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            StringBuffer stringBuffer = new StringBuffer();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] values = line.split(" ");
                Entry e = new Entry(values[0], values[1], values[2]);
                logEntries.add(e);
                User u = users.get(e.getUsername());
                if (null == u) {
                    u = new User(e.getUsername());
                    users.put(e.getUsername(), u);
                }
                u.addEntry(e);
            }
            fileReader.close();
            firstEntryTime = logEntries.get(0).time;
            lastEntryTime = logEntries.get(logEntries.size() - 1).time;
        } catch (IOException fe) {
            fe.printStackTrace();
        }
    }
}
