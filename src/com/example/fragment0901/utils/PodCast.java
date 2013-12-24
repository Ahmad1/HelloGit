package com.example.fragment0901.utils;

public class PodCast {


	private String title;
	private String summary;
	private String link;
	private String duration;
	private String date;
    private String shareLink;


	
	public PodCast(String title, String summary, String link, String duration, String date, String shareLink) {
		this.title = title;
		this.summary = summary;
		this.link = link;
		this.duration = duration;
		this.date= date;
        this.shareLink = shareLink;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}


	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public String getshareLink() {
		return shareLink;
	}

	public void setShareLink(String shareLink) {
		this.shareLink = shareLink;
	}
    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }
	public String getDuration() {
		return duration;
	}

	public void setDuration(String duration) {
		this.duration = duration;
	}

	@Override
	public String toString() {
		return "TITLE: " + title + ", SUMMARY: " + summary + "TIME: "+ duration +"LINK: "+ link;
	}

}
