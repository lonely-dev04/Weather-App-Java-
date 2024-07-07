package com.lonelydev.myweatherapp;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class DataModel {
    @SerializedName("location")
    private Location location;

    @SerializedName("current")
    private Current current;

    // Getter methods
    public Location getLocation() {
        return location;
    }

    public Current getCurrent() {
        return current;
    }

    public static class Location {
        @SerializedName("name")
        private String name;

        @SerializedName("region")
        private String region;

        @SerializedName("country")
        private String country;

        // Getter methods
        public String getName() {
            return name;
        }

        public String getRegion() {
            return region;
        }

        public String getCountry() {
            return country;
        }
    }

    public static class Current {
        @SerializedName("temp_c")
        private float tempC;

        @SerializedName("condition")
        private Condition condition;

        // Getter methods
        public float getTempC() {
            return tempC;
        }

        public Condition getCondition() {
            return condition;
        }

        public static class Condition {
            @SerializedName("text")
            private String text;

            @SerializedName("icon")
            private String icon;

            // Getter methods
            public String getText() {
                return text;
            }

            public String getIcon() {
                return icon;
            }
        }
    }
}

class PexelsResponse {
    @SerializedName("page")
    private int page;

    @SerializedName("per_page")
    private int perPage;

    @SerializedName("photos")
    private List<Photo> photos;

    public int getPage() {
        return page;
    }

    public int getPerPage() {
        return perPage;
    }

    public List<Photo> getPhotos() {
        return photos;
    }
}

class Photo {
    @SerializedName("id")
    private long id;

    @SerializedName("width")
    private int width;

    @SerializedName("height")
    private int height;

    @SerializedName("url")
    private String url;

    @SerializedName("src")
    private Src src;

    public String getUrl() {
        return url;
    }

    public Src getSrc() {
        return src;
    }
}

class Src {
    @SerializedName("medium")
    private String medium;

    public String getMedium() {
        return medium;
    }
}


