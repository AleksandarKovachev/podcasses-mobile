package com.podcasses.model.request;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by aleksandar.kovachev.
 */
public class TrendingFilter {

    private TrendingReport trendingReport;

    private Date from;

    private Date to;

    private Integer categoryId;

    private Integer languageId;

    public TrendingFilter() {
    }

    public TrendingFilter(TrendingReport trendingReport, Date from, Date to, Integer categoryId, Integer languageId) {
        this.trendingReport = trendingReport;
        this.from = from;
        this.to = to;
        this.categoryId = categoryId;
        this.languageId = languageId;
    }

    public TrendingReport getTrendingReport() {
        return trendingReport;
    }

    public void setTrendingReport(TrendingReport trendingReport) {
        this.trendingReport = trendingReport;
    }

    public Date getFrom() {
        return from;
    }

    public void setFrom(Date from) {
        this.from = from;
    }

    public Date getTo() {
        return to;
    }

    public void setTo(Date to) {
        this.to = to;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public Integer getLanguageId() {
        return languageId;
    }

    public void setLanguageId(Integer languageId) {
        this.languageId = languageId;
    }

    public Map<String, Object> toQueryMap() {
        Map<String, Object> queryMap = new HashMap<>();
        if (trendingReport != null) {
            queryMap.put("trendingReport", trendingReport);
        }

        if (from != null) {
            queryMap.put("from", from.getTime());
        }

        if (to != null) {
            queryMap.put("to", to.getTime());
        }

        if (categoryId != null) {
            queryMap.put("categoryId", categoryId);
        }

        if (languageId != null) {
            queryMap.put("languageId", languageId);
        }
        return queryMap;
    }

}
