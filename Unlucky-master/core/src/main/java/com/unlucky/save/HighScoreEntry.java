package com.unlucky.save;

/**
 * Represents a single high score entry with survival time and score
 * Used for top 6 high scores system
 *
 * @author Ming Li
 */
public class HighScoreEntry implements Comparable<HighScoreEntry> {
    
    public float survivalTime; // Thời gian sống (giây)
    public int score;          // Điểm số
    public String date;        // Ngày đạt được (optional)
    
    public HighScoreEntry(float survivalTime, int score) {
        this.survivalTime = survivalTime;
        this.score = score;
        this.date = getCurrentDate();
    }
    
    public HighScoreEntry(float survivalTime, int score, String date) {
        this.survivalTime = survivalTime;
        this.score = score;
        this.date = date;
    }
    
    /**
     * Compare by survival time (descending - longest survival first)
     */
    @Override
    public int compareTo(HighScoreEntry other) {
        // Sắp xếp theo thời gian sống giảm dần (lâu nhất trên đầu)
        return Float.compare(other.survivalTime, this.survivalTime);
    }
    
    /**
     * Format survival time as MM:SS
     */
    public String getFormattedTime() {
        int minutes = (int) (survivalTime / 60);
        int seconds = (int) (survivalTime % 60);
        return String.format("%02d:%02d", minutes, seconds);
    }
    
    /**
     * Get current date string
     */
    private String getCurrentDate() {
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM");
            return sdf.format(new java.util.Date());
        } catch (Exception e) {
            return "??/??";
        }
    }
    
    /**
     * Convert to string for file storage
     * Format: survivalTime,score,date
     */
    public String toFileString() {
        return survivalTime + "," + score + "," + date;
    }
    
    /**
     * Create from file string
     */
    public static HighScoreEntry fromFileString(String line) {
        try {
            String[] parts = line.split(",");
            float time = Float.parseFloat(parts[0]);
            int score = Integer.parseInt(parts[1]);
            String date = parts.length > 2 ? parts[2] : "??/??";
            return new HighScoreEntry(time, score, date);
        } catch (Exception e) {
            return null;
        }
    }
    
    @Override
    public String toString() {
        return String.format("Time: %s, Score: %d (%s)", getFormattedTime(), score, date);
    }
}
