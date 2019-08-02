package com.ilife.iliferobot.model.bean;

public class CleanningRobot {
    private int img;
    private String name;
    private String subdomain;
    private long subdomainId;

    public int getImg() {
        return img;
    }

    public void setImg(int img) {
        this.img = img;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSubdomain() {
        return subdomain;
    }

    public void setSubdomain(String subdomain) {
        this.subdomain = subdomain;
    }

    public long getSubdomainId() {
        return subdomainId;
    }

    public void setSubdomainId(int subdomainId) {
        this.subdomainId = subdomainId;
    }

    public CleanningRobot(int img, String name, String subdomain, long subdomainId) {
        this.img = img;
        this.name = name;
        this.subdomain = subdomain;
        this.subdomainId = subdomainId;
    }
}
