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

    public TrendingFilter() {
    }

    public TrendingFilter(TrendingReport trendingReport, Date from, Date to) {
        this.trendingReport = trendingReport;
        this.from = from;
        this.to = to;
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
        return queryMap;
    }

}
