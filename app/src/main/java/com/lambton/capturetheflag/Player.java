package com.lambton.capturetheflag;

class Player {

    String playerId;
    String playerName;
    String playerTeam;
    Double latitude;
    Double longitude;
    Boolean flagValue;

    public Player(String playerId, String playerName, String playerTeam, Double latitude, Double longitude, Boolean flagValue) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.playerTeam = playerTeam;
        this.latitude = latitude;
        this.longitude = longitude;
        this.flagValue = flagValue;
    }

    public Player(double latitude, double longitude) {
    }

    public Player() {
    }

    public String getPlayerId() {
        return playerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getPlayerTeam() {
        return playerTeam;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }


}