package com.wpcreative.skycast.model;

import java.util.List;

public class ForecastResponse {
    public String cod;
    public int message;
    public int cnt;
    public List<ForecastItem> list;
    public City city;

    public static class ForecastItem {
        public long dt;
        public Main main;
        public List<Weather> weather;
        public Clouds clouds;
        public Wind wind;
        public int visibility;
        public double pop; // Probability of precipitation
        public Sys sys;
        public String dt_txt;
    }

    public static class Main {
        public double temp;
        public double feels_like;
        public double temp_min;
        public double temp_max;
        public int pressure;
        public int sea_level;
        public int grnd_level;
        public int humidity;
        public double temp_kf;
    }

    public static class Weather {
        public int id;
        public String main;
        public String description;
        public String icon;
    }

    public static class Clouds {
        public int all;
    }

    public static class Wind {
        public double speed;
        public int deg;
        public double gust;
    }

    public static class Sys {
        public String pod; // Part of day (d/n)
    }

    public static class City {
        public int id;
        public String name;
        public Coord coord;
        public String country;
        public int population;
        public int timezone;
        public long sunrise;
        public long sunset;
    }

    public static class Coord {
        public double lat;
        public double lon;
    }
}
