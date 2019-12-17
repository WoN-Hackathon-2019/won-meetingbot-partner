package won.bot.meetingbotpartner;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;

public class VenueRequest {

    private String[] categories;
    private double[][] locations;
    private ObjectMapper mapper = new ObjectMapper();



    public String[] getCategories() {
        return categories;
    }

    public void setCategories(String[] categories) {
        this.categories = categories;
    }

    public double[][] getLocations() {
        return locations;
    }

    public void setLocations(double[][] locations) {
        this.locations = locations;
    }

    @JsonIgnore
    public String stringify() {
        try {
            String s = mapper.writeValueAsString(this);
            s = s.replace("\"", "\\\"");
            return "/json \"" + s + "\"";
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
