package dz.usthb.eclipseworkspace.workspace.service;

import java.sql.Date;
import java.util.concurrent.TimeUnit;

public class MinMaxDate {

    private Date minDate;
    private Date maxDate;

    public MinMaxDate(Date minDate, Date maxDate) {
        this.minDate = minDate;
        this.maxDate = maxDate;
    }

    public Date getMinDate() {
        return minDate;
    }

    public Date getMaxDate() {
        return maxDate;
    }

    public void setMinDate(Date minDate) {
        this.minDate = minDate;
    }

    public void setMaxDate(Date maxDate) {
        this.maxDate = maxDate;
    }

    public int getDuration() {
        long diffInMillies = Math.abs(maxDate.getTime() - minDate.getTime());
        long days = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
        return (int) days + 1;
    }

    public int getAnyDuration(Date date1, Date date2) {
        long diffInMillies = Math.abs(date2.getTime() - date1.getTime());
        long days = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
        return (int) days + 1;
    }
}
